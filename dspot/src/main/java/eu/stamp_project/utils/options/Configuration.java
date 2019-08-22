package eu.stamp_project.utils.options;

import eu.stamp_project.dspot.amplifier.FastLiteralAmplifier;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.utils.options.check.Checker;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.collector.CollectorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 28/05/19
 */
public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    static void configureExample() {
        try {
            InputConfiguration.initialize("src/test/resources/test-projects/test-projects.properties");
            InputConfiguration.get().setNbIteration(1);
            InputConfiguration.get().setAmplifiers(Collections.singletonList(new FastLiteralAmplifier()));
            InputConfiguration.get().setSelector(new JacocoCoverageSelector());
            InputConfiguration.get().setBudgetizer(BudgetizerEnum.RandomBudgetizer);
            InputConfiguration.get().setTestClasses(Collections.singletonList("example.TestSuiteExample"));
            InputConfiguration.get().setTestClasses(Collections.emptyList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void configure(final String pathToPropertiesFile,
                                 final List<String> amplifiers,
                                 final String selector,
                                 final String budgetizer,
                                 final String pitOutputFormat,
                                 final String pathPitResult,
                                 final String automaticBuilder,
                                 final String output,
                                 final int iteration,
                                 final long randomSeed,
                                 final int timeOut,
                                 final int maxTestAmplified,
                                 final boolean clean,
                                 final boolean verbose,
                                 final boolean workingDirectory,
                                 final boolean comment,
                                 final boolean generateNewTestClass,
                                 final boolean keepOriginalTestMethods,
                                 final boolean gregor,
                                 final boolean descartes,
                                 final boolean useMavenToExecuteTest,
                                 final boolean targetOneTestClass,
                                 final boolean allowPathInAssertion,
                                 final boolean executeTestsInParallel,
                                 final int numberParallelExecutionProcessors,
                                 final List<String> testClasses,
                                 final List<String> testCases,
                                 final String fullClasspath) {

        Checker.checkPathToPropertiesValue(pathToPropertiesFile);
        final Properties properties = loadProperties(pathToPropertiesFile);
        configure(
                properties,
                amplifiers,
                selector,
                budgetizer,
                pitOutputFormat,
                pathPitResult,
                automaticBuilder,
                output,
                iteration,
                randomSeed,
                timeOut,
                maxTestAmplified,
                clean,
                verbose,
                workingDirectory,
                comment,
                generateNewTestClass,
                keepOriginalTestMethods,
                gregor,
                descartes,
                useMavenToExecuteTest,
                targetOneTestClass,
                allowPathInAssertion,
                executeTestsInParallel,
                numberParallelExecutionProcessors,
                testClasses,
                testCases,
                fullClasspath
        );
    }

    public static void configure(Properties properties,
                                 final List<String> amplifiers,
                                 final String selector,
                                 final String budgetizer,
                                 final String pitOutputFormat,
                                 final String pathPitResult,
                                 final String automaticBuilder,
                                 final String output,
                                 final int iteration,
                                 final long randomSeed,
                                 final int timeOut,
                                 final int maxTestAmplified,
                                 final boolean clean,
                                 final boolean verbose,
                                 final boolean workingDirectory,
                                 final boolean comment,
                                 final boolean generateNewTestClass,
                                 final boolean keepOriginalTestMethods,
                                 final boolean gregor,
                                 final boolean descartes,
                                 final boolean useMavenToExecuteTest,
                                 final boolean targetOneTestClass,
                                 final boolean allowPathInAssertion,
                                 final boolean executeTestsInParallel,
                                 final int numberParallelExecutionProcessors,
                                 final List<String> testClasses,
                                 final List<String> testCases,
                                 final String fullClasspath) {
        // pit output format
        PitMutantScoreSelector.OutputFormat consecutiveFormat;
        if (pitOutputFormat.toLowerCase().equals("xml")) {
            consecutiveFormat = PitMutantScoreSelector.OutputFormat.XML;
        } else if (pitOutputFormat.toLowerCase().equals("csv")) {
            consecutiveFormat = PitMutantScoreSelector.OutputFormat.CSV;
        } else {
            LOGGER.warn("You specified an invalid format. Forcing the Pit output format to XML.");
            consecutiveFormat = PitMutantScoreSelector.OutputFormat.XML;
        }

        Checker.preChecking(
                amplifiers,
                selector,
                budgetizer,
                properties
        );

        // expert test selector mode
        TestSelector testCriterion;
        if (pathPitResult != null) {
            if (!"PitMutantScoreSelector".equals(selector)) {
                LOGGER.warn("You specified a path to a mutations file but you did not specify the right test-criterion");
                LOGGER.warn("Forcing the Selector to PitMutantScoreSelector");
            }
            PitMutantScoreSelector.OutputFormat originalFormat;
            if (pathPitResult.toLowerCase().endsWith(".xml")) {
                originalFormat = PitMutantScoreSelector.OutputFormat.XML;
            } else if (pathPitResult.toLowerCase().endsWith(".csv")) {
                originalFormat = PitMutantScoreSelector.OutputFormat.CSV;
            } else {
                LOGGER.warn("You specified the wrong Pit format. Skipping expert mode.");
                originalFormat = PitMutantScoreSelector.OutputFormat.XML;
            }
            testCriterion = new PitMutantScoreSelector(pathPitResult, originalFormat, consecutiveFormat);
        } else {
            testCriterion = SelectorEnum.valueOf(selector).buildSelector();
        }
        //ExecuteTestsInParallel needs to be setup before initializing InputConfiguration
        //because it is required to compute the classpath of the MavenAutomaticBuilder
        InputConfiguration.initialize(properties, automaticBuilder, executeTestsInParallel, fullClasspath);

        if (InputConfiguration.get().getOutputDirectory().isEmpty()) {

            InputConfiguration.get().setOutputDirectory(output);
        }

        Checker.postChecking(properties);

        InputConfiguration.setUp(
                amplifiers,
                budgetizer,
                testCriterion,
                testClasses,
                testCases,
                iteration,
                randomSeed,
                timeOut,
                maxTestAmplified,
                clean,
                verbose,
                workingDirectory,
                comment,
                generateNewTestClass,
                keepOriginalTestMethods,
                gregor,
                descartes,
                useMavenToExecuteTest,
                targetOneTestClass,
                allowPathInAssertion,
                executeTestsInParallel,
                numberParallelExecutionProcessors
        );
    }

    public static void configure(final String pathToPropertiesFile,
                                 final List<String> amplifiers,
                                 final String selector,
                                 final String budgetizer,
                                 final String pitOutputFormat,
                                 final String pathPitResult,
                                 final String automaticBuilder,
                                 final String output,
                                 final int iteration,
                                 final long randomSeed,
                                 final int timeOut,
                                 final int maxTestAmplified,
                                 final boolean clean,
                                 final boolean verbose,
                                 final boolean workingDirectory,
                                 final boolean comment,
                                 final boolean generateNewTestClass,
                                 final boolean keepOriginalTestMethods,
                                 final boolean gregor,
                                 final boolean descartes,
                                 final boolean useMavenToExecuteTest,
                                 final boolean targetOneTestClass,
                                 final boolean allowPathInAssertion,
                                 final boolean executeTestsInParallel,
                                 final int numberParallelExecutionProcessors,
                                 final List<String> testClasses,
                                 final List<String> testCases,
                                 final String fullClasspath,
                                 final String collector,
                                 final String mongoUrl) {

        Checker.checkPathToPropertiesValue(pathToPropertiesFile);
        final Properties properties = loadProperties(pathToPropertiesFile);
        configure(properties,
                    amplifiers,
                    selector,
                    budgetizer,
                    pitOutputFormat,
                    pathPitResult,
                    automaticBuilder,
                    output,
                    iteration,
                    randomSeed,
                    timeOut,
                    maxTestAmplified,
                    clean,
                    verbose,
                    workingDirectory,
                    comment,
                    generateNewTestClass,
                    keepOriginalTestMethods,
                    gregor,
                    descartes,
                    useMavenToExecuteTest,
                    targetOneTestClass,
                    allowPathInAssertion,
                    executeTestsInParallel,
                    numberParallelExecutionProcessors,
                    testClasses,
                    testCases,
                    fullClasspath);

        CollectorConfig collectorConfig = CollectorConfig.getInstance();

        collectorConfig.setMongoUrl(mongoUrl);
        collectorConfig.setInformationCollector(collector);
    }

    public static Properties loadProperties(String pathToPropertiesFile) {
        try {
            Properties properties = new Properties();
            if (pathToPropertiesFile == null || "".equals(pathToPropertiesFile)) {
                LOGGER.warn("You did not specify any path for the properties file. Using only default values.");
            } else {
                properties.load(new FileInputStream(pathToPropertiesFile));
            }
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
