package fr.inria.stamp;

import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.dspot.selector.JacocoCoverageSelector;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.diff.SelectorOnDiff;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT benjamin.danglot@inria.fr on 2/9/17
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static boolean verbose = false;

    public static void main(String[] args) throws Exception {
        try {
            FileUtils.forceDelete(new File("target/dspot/"));
        } catch (Exception ignored) {

        }
        final Configuration configuration = JSAPOptions.parse(args);
        if (configuration == null) {
            Main.runExample();
        } else {
            run(configuration);
        }
    }

    public static void run(Configuration configuration, InputConfiguration inputConfiguration) throws Exception {
        AmplificationHelper.setSeedRandom(23L);
        AmplificationHelper.minimize = configuration.minimize;
        InputProgram program = new InputProgram();
        inputConfiguration.setInputProgram(program);
        inputConfiguration.getProperties().setProperty("automaticBuilderName", configuration.automaticBuilderName);
        AmplificationHelper.MAX_NUMBER_OF_TESTS = configuration.maxTestAmplified;
        if (configuration.mavenHome != null) {
            inputConfiguration.getProperties().put("maven.home", configuration.mavenHome);
        }
        if (configuration.pathToOutput != null) {
            inputConfiguration.getProperties().setProperty("outputDirectory", configuration.pathToOutput);
        }
        DSpot dspot = new DSpot(inputConfiguration, configuration.nbIteration, configuration.amplifiers,
                configuration.selector);

        AmplificationHelper.setSeedRandom(configuration.seed);
        AmplificationHelper.setTimeOutInMs(configuration.timeOutInMs);

        createOutputDirectories(inputConfiguration, configuration.clean);

        final long startTime = System.currentTimeMillis();
        final List<CtType> amplifiedTestClasses;
        if ("all".equals(configuration.testClasses.get(0))) {
            amplifiedTestClasses = dspot.amplifyAllTests();
        } else if ("diff".equals(configuration.testClasses.get(0))) {
            final Map<String, List<String>> testMethodsAccordingToADiff = SelectorOnDiff.findTestMethodsAccordingToADiff(inputConfiguration);
            amplifiedTestClasses = testMethodsAccordingToADiff.keySet()
                    .stream()
                    .map(ctType -> dspot.amplifyTest(ctType, testMethodsAccordingToADiff.get(ctType)))
                    .collect(Collectors.toList());
        } else {
            if (configuration.testCases.isEmpty()) {
                amplifiedTestClasses = dspot.amplifyAllTestsNames(configuration.testClasses);
            } else {
                amplifiedTestClasses = configuration.testClasses.stream().map(testClasses ->
                        dspot.amplifyTest(testClasses, configuration.testCases)
                ).collect(Collectors.toList());
            }
        }
        LOGGER.info("Amplification {}.", amplifiedTestClasses.isEmpty() ? "failed" : "succeed");
        final long elapsedTime = System.currentTimeMillis() - startTime;
        LOGGER.info("Elapsed time {} ms", elapsedTime);
    }

    public static void run(Configuration configuration) throws Exception {
        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);
        run(configuration, inputConfiguration);
    }

    public static void createOutputDirectories(InputConfiguration inputConfiguration, boolean clean) {
        final File outputDirectory = new File(inputConfiguration.getOutputDirectory());
        try {
            if (clean && outputDirectory.exists()) {
                FileUtils.forceDelete(outputDirectory);
            }
            if (!outputDirectory.exists()) {
                FileUtils.forceMkdir(outputDirectory);
            }
        } catch (IOException ignored) {
            // ignored
        }
    }

    static void runExample() {
        try {
            InputConfiguration configuration = new InputConfiguration(
                    "src/test/resources/test-projects/test-projects.properties");
            DSpot dSpot = new DSpot(configuration, 1, Collections.singletonList(new TestDataMutator()),
                    new JacocoCoverageSelector());
            dSpot.amplifyTest("example.TestSuiteExample");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}