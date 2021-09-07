package eu.stamp_project.dspot.common.configuration;

import eu.stamp_project.dspot.amplifier.amplifiers.utils.RandomHelper;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.CloneHelper;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.amplifier.amplifiers.Amplifier;
import eu.stamp_project.dspot.assertiongenerator.AssertionGenerator;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.AssertionGeneratorUtils;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.dspot.common.collector.CollectorFactory;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.compilation.TestCompiler;
import eu.stamp_project.dspot.common.configuration.options.AmplifierEnum;
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

    public void init(UserInput userInput) {
        this.DSpotState = new DSpotState();
        DSpotState.setUserInput(userInput);
        DSpotState.setOnlyInputAmplification(userInput.isOnlyInputAmplification());
        DSpotState.setDevFriendlyAmplification(userInput.isDevFriendlyAmplification());
        DSpotState.verbose = userInput.isVerbose();
        DSpotState.setStartTime(System.currentTimeMillis());
        DSpotState.setTestFinder(new TestFinder(
                Arrays.stream(userInput.getExcludedClasses().split(",")).collect(Collectors.toList()),
                Arrays.stream(userInput.getExcludedTestCases().split(",")).collect(Collectors.toList())
        ));
        DSpotState.setAutomaticBuilder(userInput.getBuilderEnum().getAutomaticBuilder(userInput));
        final String dependencies = completeDependencies(userInput, DSpotState.getAutomaticBuilder());
        DSpotState.setCompiler(DSpotCompiler.createDSpotCompiler(
                userInput,
                dependencies
        ));
        userInput.setFactory(DSpotState.getCompiler().getLauncher().getFactory());
        initHelpers(userInput);
        DSpotState.setTestCompiler(new TestCompiler(
                userInput.getNumberParallelExecutionProcessors(),
                userInput.shouldExecuteTestsInParallel(),
                userInput.getAbsolutePathToProjectRoot(),
                userInput.getClasspathClassesProject(),
                userInput.getTimeOutInMs(),
                userInput.getPreGoalsTestExecution(),
                userInput.shouldUseMavenToExecuteTest()
        ));
        final EmailSender emailSender = new EmailSender(
                userInput.getSmtpUsername(),
                userInput.getSmtpPassword(),
                userInput.getSmtpHost(),
                userInput.getSmtpPort(),
                userInput.isSmtpAuth(),
                userInput.getSmtpTls()
        );
        DSpotState.setCollector(CollectorFactory.build(userInput, emailSender));
        DSpotState.getCollector().reportInitInformation(
                userInput.getAmplifiers(),
                userInput.getSelector(),
                userInput.getNbIteration(),
                userInput.isGregorMode(),
                !userInput.isGregorMode(),
                userInput.getNumberParallelExecutionProcessors()
        );
        DSpotState.setTestClassesToBeAmplified(DSpotState.getTestFinder().findTestClasses(userInput.getTestClasses()));
        DSpotState.setTestMethodsToBeAmplifiedNames(userInput.getTestCases());
        if (DSpotState.getTestMethodsToBeAmplifiedNames().size() == 1 &&
                DSpotState.getTestMethodsToBeAmplifiedNames().get(0).isEmpty()) {
            DSpotState.getTestMethodsToBeAmplifiedNames().clear();
        }
        DSpotState.setTestSelector(userInput.getSelector().buildSelector(DSpotState.getAutomaticBuilder(), userInput));
        final List<Amplifier> amplifiers = userInput
                .getAmplifiers()
                .stream()
                .map(AmplifierEnum::getAmplifier)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        DSpotState.setInputAmplDistributor(userInput
                .getInputAmplDistributor()
                .getInputAmplDistributor(userInput.getMaxTestAmplified(), amplifiers));
        DSpotState.setOutput(new Output(
                userInput.getAbsolutePathToProjectRoot(),
                userInput.getOutputDirectory(),
                DSpotState.getCollector()

        ));
        DSpotState.setAssertionGenerator(new AssertionGenerator(userInput.getDelta(), DSpotState.getCompiler(), DSpotState.getTestCompiler(), DSpotState.isDevFriendlyAmplification()));
        Checker.postChecking(userInput);
        DSpotState.setCollectData(true);
        DSpotState.setDelta(userInput.getDelta());
        DSpotState.setNbIteration(userInput.getNbIteration());
        DSpotState.verbose = userInput.isVerbose();
    }

    public void initHelpers(UserInput configuration) {
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

    public String completeDependencies(UserInput configuration, AutomaticBuilder automaticBuilder) {
        String dependencies = configuration.getDependencies();
        final String additionalClasspathElements = configuration.getAdditionalClasspathElements();
        final String absolutePathToProjectRoot = configuration.getAbsolutePathToProjectRoot();
        if (dependencies.isEmpty()) {
            dependencies = automaticBuilder.compileAndBuildClasspath();
            configuration.setDependencies(dependencies);
        }
        // TODO checks this. Since we support different Test Support, we may not need to add artificially junit in the classpath
//        if (!dependencies.contains("junit" + File.separator + "junit" + File.separator + "4")) {
//            dependencies = Test.class
//                    .getProtectionDomain()
//                    .getCodeSource()
//                    .getLocation()
//                    .getFile() +
//                    AmplificationHelper.PATH_SEPARATOR + dependencies;
//            System.out.println("dependencies at end of junit block: " + dependencies);
//        }
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

    public void createOutputDirectories(UserInput configuration) {
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
