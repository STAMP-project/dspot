package fr.inria.stamp;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/9/17
 */
public class Main {

    public static void main(String[] args) throws InvalidSdkException, Exception {
        run(JSAPOptions.parse(args));
        System.exit(0);
    }

    public static void run(Configuration configuration) throws InvalidSdkException, Exception {
        InputConfiguration inputConfiguration = new InputConfiguration(configuration.pathToConfigurationFile);
        AmplificationHelper.setSeedRandom(23L);
        InputProgram program = new InputProgram();
        inputConfiguration.setInputProgram(program);
        if (configuration.mavenHome != null) {
            inputConfiguration.getProperties().put("maven.home", configuration.mavenHome);
        }
        DSpot dspot = new DSpot(inputConfiguration, configuration.nbIteration, configuration.amplifiers, configuration.selector);
        if (configuration.pathToOutput != null) {
            inputConfiguration.getProperties().setProperty("outputDirectory", configuration.pathToOutput);
        }

        AmplificationHelper.setSeedRandom(configuration.seed);
        AmplificationHelper.setTimeOutInMs(configuration.timeOutInMs);

        createOutputDirectories(inputConfiguration);
        if ("all".equals(configuration.testClasses.get(0))) {
            amplifyAll(dspot, inputConfiguration);
        } else {
            configuration.testClasses.forEach(testCase -> {
                if (!configuration.namesOfTestCases.isEmpty()) {
                    amplifyOne(dspot, testCase, configuration.namesOfTestCases);
                } else {
                    amplifyOne(dspot, testCase, Collections.EMPTY_LIST);
                }
            });
        }
        dspot.cleanResources();
    }

    private static void createOutputDirectories(InputConfiguration inputConfiguration) {
        if (!new File(inputConfiguration.getOutputDirectory()).exists()) {

            String[] paths = inputConfiguration.getOutputDirectory().split(System.getProperty("file.separator"));
            if (!new File(paths[0]).exists()) {
                new File(paths[0]).mkdir();
            }
            new File(inputConfiguration.getOutputDirectory()).mkdir();
        }
    }

    private static void amplifyOne(DSpot dspot, String fullQualifiedNameTestClass, List<String> testCases) {
        long time = System.currentTimeMillis();
        try {
            if (testCases.isEmpty()) {
                dspot.amplifyTest(fullQualifiedNameTestClass);
            } else {
                dspot.amplifyTest(fullQualifiedNameTestClass, testCases);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(System.currentTimeMillis() - time + " ms");
    }

    private static void amplifyAll(DSpot dspot, InputConfiguration configuration) {
        long time = System.currentTimeMillis();
        final File outputDirectory = new File(configuration.getOutputDirectory() + "/");
        if (!outputDirectory.exists()) {
            if (!new File("results").exists()) {
                new File("results").mkdir();
            }
            if (!outputDirectory.exists()) {
                outputDirectory.mkdir();
            }
        }
        try {
            dspot.amplifyAllTests();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(System.currentTimeMillis() - time + " ms");
    }

    static void runExample() {
        try {
            InputConfiguration configuration = new InputConfiguration("src/test/resources/test-projects/test-projects.properties");
            DSpot dSpot = new DSpot(configuration, 1);
            dSpot.amplifyTest("example.TestSuiteExample");
        } catch (Exception | InvalidSdkException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }
}
