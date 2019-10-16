package eu.stamp_project;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.AssertionGeneratorUtils;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.*;
import eu.stamp_project.utils.collector.Collector;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.utils.collector.CollectorFactory;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.compilation.TestCompiler;
import eu.stamp_project.utils.options.AmplifierEnum;
import eu.stamp_project.utils.options.check.Checker;
import eu.stamp_project.utils.options.check.InputErrorException;
import eu.stamp_project.utils.report.output.Output;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.GlobalReport;
import eu.stamp_project.utils.report.error.ErrorReportImpl;
import eu.stamp_project.utils.report.output.OutputReportImpl;
import eu.stamp_project.utils.report.output.selector.TestSelectorReportImpl;
import eu.stamp_project.utils.smtp.EmailSender;
import eu.stamp_project.utils.test_finder.TestFinder;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.stamp_project.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * Created by Benjamin DANGLOT benjamin.danglot@inria.fr on 2/9/17
 */
public class Main {

    public static final GlobalReport GLOBAL_REPORT =
            new GlobalReport(new OutputReportImpl(), new ErrorReportImpl(), new TestSelectorReportImpl());

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static boolean verbose;

    public static void main(String[] args) {
        InputConfiguration inputConfiguration = new InputConfiguration();
        final CommandLine commandLine = new CommandLine(inputConfiguration);
        commandLine.setUsageHelpWidth(120);
        try {
            commandLine.parseArgs(args);
        } catch (Exception e) {
            e.printStackTrace();
            commandLine.usage(System.err);
            return;
        }
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        }
        if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }
        if (inputConfiguration.shouldRunExample()) {
            inputConfiguration.configureExample();
        }
        try {
            Checker.preChecking(inputConfiguration);
        } catch (InputErrorException e) {
            e.printStackTrace();
            commandLine.usage(System.err);
            return;
        }
        Main.verbose = inputConfiguration.isVerbose();
        run(inputConfiguration);
    }

    public static void run(InputConfiguration inputConfiguration) {
        final long startTime = System.currentTimeMillis();
        final TestFinder testFinder = new TestFinder(
                Arrays.stream(inputConfiguration.getExcludedClasses().split(",")).collect(Collectors.toList()),
                Arrays.stream(inputConfiguration.getExcludedTestCases().split(",")).collect(Collectors.toList())
        );
        if (!inputConfiguration.getPathToTestListCSV().isEmpty()) {
            inputConfiguration.initTestsToBeAmplified();
        }
        final AutomaticBuilder automaticBuilder = inputConfiguration.getBuilderEnum().getAutomaticBuilder(inputConfiguration);
        final String dependencies = completeDependencies(inputConfiguration, automaticBuilder);
        final DSpotCompiler compiler = DSpotCompiler.createDSpotCompiler(
                inputConfiguration,
                dependencies
        );
        inputConfiguration.setFactory(compiler.getLauncher().getFactory());
        initHelpers(inputConfiguration);
        final EmailSender emailSender = new EmailSender(
                inputConfiguration.getSmtpUsername(),
                inputConfiguration.getSmtpPassword(),
                inputConfiguration.getSmtpHost(),
                inputConfiguration.getSmtpPort(),
                inputConfiguration.isSmtpAuth(),
                inputConfiguration.getSmtpTls()
        );
        final Collector collector = CollectorFactory.build(inputConfiguration, emailSender);
        collector.reportInitInformation(
                inputConfiguration.getAmplifiers(),
                inputConfiguration.getSelector(),
                inputConfiguration.getNbIteration(),
                inputConfiguration.isGregorMode(),
                !inputConfiguration.isGregorMode(),
                inputConfiguration.getNumberParallelExecutionProcessors()
        );
        final List<CtType<?>> testClassesToBeAmplified = testFinder.findTestClasses(inputConfiguration.getTestClasses());
        final List<String> testMethodsToBeAmplifiedNames = inputConfiguration.getTestCases();
        final TestSelector testSelector =
                inputConfiguration.getSelector().buildSelector(automaticBuilder, inputConfiguration);
        final List<Amplifier> amplifiers = inputConfiguration
                .getAmplifiers()
                .stream()
                .map(AmplifierEnum::getAmplifier)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        final InputAmplDistributor inputAmplDistributor = inputConfiguration
                .getInputAmplDistributor()
                .getInputAmplDistributor(inputConfiguration.getMaxTestAmplified(), amplifiers);
        final Output output = new Output(
                inputConfiguration.getAbsolutePathToProjectRoot(),
                inputConfiguration.getOutputDirectory(),
                collector

        );
        final DSpot dspot = new DSpot(
                inputConfiguration.getDelta(),
                testFinder,
                compiler,
                testSelector,
                inputAmplDistributor,
                output,
                inputConfiguration.getNbIteration(),
                inputConfiguration.shouldGenerateAmplifiedTestClass(),
                automaticBuilder
        );

        Checker.postChecking(inputConfiguration);

        // starting amplification
        final List<CtType<?>> amplifiedTestClasses = dspot.amplify(testClassesToBeAmplified, testMethodsToBeAmplifiedNames);
        LOGGER.info("Amplification {}.", amplifiedTestClasses.isEmpty() ? "failed" : "succeed");
        final long elapsedTime = System.currentTimeMillis() - startTime;
        LOGGER.info("Elapsed time {} ms", elapsedTime);
        // global report handling
        Main.GLOBAL_REPORT.output(inputConfiguration.getOutputDirectory());
        DSpotCache.reset();
        Main.GLOBAL_REPORT.reset();
        AmplificationHelper.reset();
        DSpotPOMCreator.delete();
        // Send info collected.
        collector.sendInfo();
    }

    private static void initHelpers(InputConfiguration configuration){
        TestFramework.init(configuration.getFactory());
        AmplificationHelper.init(
                configuration.getTimeOutInMs(),
                configuration.shouldGenerateAmplifiedTestClass(),
                configuration.shouldKeepOriginalTestMethods()
        );
        RandomHelper.setSeedRandom(configuration.getSeed());
        createOutputDirectories(configuration);
        DSpotCache.init(configuration.getCacheSize());
        TestCompiler.init(
                configuration.getNumberParallelExecutionProcessors(),
                configuration.shouldExecuteTestsInParallel(),
                configuration.getAbsolutePathToProjectRoot(),
                configuration.getClasspathClassesProject(),
                configuration.getTimeOutInMs(),
                configuration.getPreGoalsTestExecution(),
                configuration.shouldUseMavenToExecuteTest()
        );
        DSpotUtils.init(
                configuration.withComment(),
                configuration.getOutputDirectory(),
                configuration.getFullClassPathWithExtraDependencies(),
                configuration.getAbsolutePathToProjectRoot()
        );
        initSystemProperties(configuration.getSystemProperties());
        AssertionGeneratorUtils.init(configuration.shouldAllowPathInAssertion());
        CloneHelper.init(configuration.shouldExecuteTestsInParallel());
    }

    private static void initSystemProperties(String systemProperties) {
        if (!systemProperties.isEmpty()) {
            Arrays.stream(systemProperties.split(","))
                    .forEach(systemProperty -> {
                        String[] keyValueInArray = systemProperty.split("=");
                        System.getProperties().put(keyValueInArray[0], keyValueInArray[1]);
                    });
        }
    }

    public static String completeDependencies(InputConfiguration configuration,
                                               AutomaticBuilder automaticBuilder) {
        String dependencies = configuration.getDependencies();
        final String additionalClasspathElements = configuration.getAdditionalClasspathElements();
        final String absolutePathToProjectRoot = configuration.getAbsolutePathToProjectRoot();
        if (dependencies.isEmpty()) {
            dependencies = automaticBuilder.compileAndBuildClasspath();
            configuration.setDependencies(dependencies);
        }
//      TODO checks this. Since we support different Test Support, we may not need to add artificially junit in the classpath
        if (!dependencies.contains("junit" + File.separator + "junit" + File.separator + "4")) {
            dependencies = Test.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getFile() +
                    AmplificationHelper.PATH_SEPARATOR + dependencies;
        }
        if (!additionalClasspathElements.isEmpty()) {
            String pathToAdditionalClasspathElements = additionalClasspathElements;
            if (!Paths.get(additionalClasspathElements).isAbsolute()) {
                pathToAdditionalClasspathElements =
                        DSpotUtils.shouldAddSeparator.apply(absolutePathToProjectRoot + additionalClasspathElements);
            }
            dependencies += PATH_SEPARATOR + pathToAdditionalClasspathElements;
        }
        return dependencies;
    }

    public static void createOutputDirectories(InputConfiguration configuration) {
        final File outputDirectory = new File(configuration.getOutputDirectory());
        try {
            if (configuration.shouldClean() && outputDirectory.exists()) {
                FileUtils.forceDelete(outputDirectory);
            }
            if (!outputDirectory.exists()) {
                FileUtils.forceMkdir(outputDirectory);
            }
        } catch (IOException ignored) {
            // ignored
        }
    }

}