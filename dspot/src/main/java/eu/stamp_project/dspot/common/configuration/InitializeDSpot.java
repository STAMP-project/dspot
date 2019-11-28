package eu.stamp_project.dspot.common.configuration;

import eu.stamp_project.dspot.amplifier.RandomHelper;
import eu.stamp_project.dspot.common.AmplificationHelper;
import eu.stamp_project.dspot.common.CloneHelper;
import eu.stamp_project.dspot.common.DSpotUtils;
import eu.stamp_project.dspot.selector.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.amplifier.amplifiers.Amplifier;
import eu.stamp_project.dspot.assertiongenerator.AssertionGenerator;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.AssertionGeneratorUtils;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.dspot.common.collector.CollectorFactory;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.compilation.TestCompiler;
import eu.stamp_project.utils.options.AmplifierEnum;
import eu.stamp_project.dspot.common.configuration.check.Checker;
import eu.stamp_project.dspot.common.report.output.Output;
import eu.stamp_project.dspot.common.collector.smtp.EmailSender;
import eu.stamp_project.dspot.common.configuration.test_finder.TestFinder;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InitializeDSpot {

    private DSpotState DSpotState;

    public InitializeDSpot() {
    }

    public void init(InputConfiguration inputConfiguration) {
        this.DSpotState = new DSpotState();
        DSpotState.setInputConfiguration(inputConfiguration);
        eu.stamp_project.dspot.common.configuration.DSpotState.verbose = inputConfiguration.isVerbose();
        DSpotState.setStartTime(System.currentTimeMillis());
        DSpotState.setTestFinder(new TestFinder(
                Arrays.stream(inputConfiguration.getExcludedClasses().split(",")).collect(Collectors.toList()),
                Arrays.stream(inputConfiguration.getExcludedTestCases().split(",")).collect(Collectors.toList())
        ));
        DSpotState.setAutomaticBuilder(inputConfiguration.getBuilderEnum().getAutomaticBuilder(inputConfiguration));
        final String dependencies = completeDependencies(inputConfiguration, DSpotState.getAutomaticBuilder());
        DSpotState.setCompiler(DSpotCompiler.createDSpotCompiler(
                inputConfiguration,
                dependencies
        ));
        inputConfiguration.setFactory(DSpotState.getCompiler().getLauncher().getFactory());
        initHelpers(inputConfiguration);
        DSpotState.setTestCompiler(new TestCompiler(
                inputConfiguration.getNumberParallelExecutionProcessors(),
                inputConfiguration.shouldExecuteTestsInParallel(),
                inputConfiguration.getAbsolutePathToProjectRoot(),
                inputConfiguration.getClasspathClassesProject(),
                inputConfiguration.getTimeOutInMs(),
                inputConfiguration.getPreGoalsTestExecution(),
                inputConfiguration.shouldUseMavenToExecuteTest()
        ));
        final EmailSender emailSender = new EmailSender(
                inputConfiguration.getSmtpUsername(),
                inputConfiguration.getSmtpPassword(),
                inputConfiguration.getSmtpHost(),
                inputConfiguration.getSmtpPort(),
                inputConfiguration.isSmtpAuth(),
                inputConfiguration.getSmtpTls()
        );
        DSpotState.setCollector(CollectorFactory.build(inputConfiguration, emailSender));
        DSpotState.getCollector().reportInitInformation(
                inputConfiguration.getAmplifiers(),
                inputConfiguration.getSelector(),
                inputConfiguration.getNbIteration(),
                inputConfiguration.isGregorMode(),
                !inputConfiguration.isGregorMode(),
                inputConfiguration.getNumberParallelExecutionProcessors()
        );
        DSpotState.setTestClassesToBeAmplified(DSpotState.getTestFinder().findTestClasses(inputConfiguration.getTestClasses()));
        DSpotState.setTestMethodsToBeAmplifiedNames(inputConfiguration.getTestCases());
        if (DSpotState.getTestMethodsToBeAmplifiedNames().size() == 1 &&
                DSpotState.getTestMethodsToBeAmplifiedNames().get(0).isEmpty()) {
            DSpotState.getTestMethodsToBeAmplifiedNames().clear();
        }
        DSpotState.setTestSelector(inputConfiguration.getSelector().buildSelector(DSpotState.getAutomaticBuilder(), inputConfiguration));
        final List<Amplifier> amplifiers = inputConfiguration
                .getAmplifiers()
                .stream()
                .map(AmplifierEnum::getAmplifier)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        DSpotState.setInputAmplDistributor(inputConfiguration
                .getInputAmplDistributor()
                .getInputAmplDistributor(inputConfiguration.getMaxTestAmplified(), amplifiers));
        DSpotState.setOutput(new Output(
                inputConfiguration.getAbsolutePathToProjectRoot(),
                inputConfiguration.getOutputDirectory(),
                DSpotState.getCollector()

        ));
        DSpotState.setAssertionGenerator(new AssertionGenerator(inputConfiguration.getDelta(), DSpotState.getCompiler(), DSpotState.getTestCompiler()));
        Checker.postChecking(inputConfiguration);
        DSpotState.setCollectData(true);
        DSpotState.setDelta(inputConfiguration.getDelta());
        DSpotState.setNbIteration(inputConfiguration.getNbIteration());
        eu.stamp_project.dspot.common.configuration.DSpotState.verbose = inputConfiguration.isVerbose();
    }

    public void initHelpers(InputConfiguration configuration) {
        TestFramework.init(configuration.getFactory());
        AmplificationHelper.init(
                configuration.getTimeOutInMs(),
                configuration.shouldGenerateAmplifiedTestClass(),
                configuration.shouldKeepOriginalTestMethods()
        );
        RandomHelper.setSeedRandom(configuration.getSeed());
        createOutputDirectories(configuration);
        DSpotCache.init(configuration.getCacheSize());
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

    private void initSystemProperties(String systemProperties) {
        if (!systemProperties.isEmpty()) {
            Arrays.stream(systemProperties.split(","))
                    .forEach(systemProperty -> {
                        String[] keyValueInArray = systemProperty.split("=");
                        System.getProperties().put(keyValueInArray[0], keyValueInArray[1]);
                    });
        }
    }

    public String completeDependencies(InputConfiguration configuration, AutomaticBuilder automaticBuilder) {
        String dependencies = configuration.getDependencies();
        final String additionalClasspathElements = configuration.getAdditionalClasspathElements();
        final String absolutePathToProjectRoot = configuration.getAbsolutePathToProjectRoot();
        if (dependencies.isEmpty()) {
            dependencies = automaticBuilder.compileAndBuildClasspath();
            configuration.setDependencies(dependencies);
        }
        // TODO checks this. Since we support different Test Support, we may not need to add artificially junit in the classpath
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
            dependencies += AmplificationHelper.PATH_SEPARATOR + pathToAdditionalClasspathElements;
        }
        return dependencies;
    }

    public void createOutputDirectories(InputConfiguration configuration) {
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

    public DSpotState getDSpotState(){
        return DSpotState;
    }
}
