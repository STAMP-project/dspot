package eu.stamp_project.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.selector.pitmutantscoreselector.*;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.CloneHelper;
import eu.stamp_project.dspot.common.report.error.Error;
import eu.stamp_project.dspot.common.report.error.ErrorEnum;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReportImpl;
import eu.stamp_project.dspot.common.report.output.selector.mutant.json.MutantJSON;
import eu.stamp_project.dspot.common.report.output.selector.mutant.json.TestCaseJSON;
import eu.stamp_project.dspot.common.report.output.selector.mutant.json.TestClassJSON;
import eu.stamp_project.dspot.common.miscellaneous.Counter;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public class PitMutantScoreSelector extends TakeAllSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(PitMutantScoreSelector.class);

    private int numberOfMutant;

    private List<AbstractPitResult> originalKilledMutants;

    private List<AbstractPitResult> baselineKilledMutants;

    private Map<CtMethod, Set<AbstractPitResult>> testThatKilledMutants;

    private List<AbstractPitResult> mutantNotTestedByOriginal;

    private AbstractParser parser;

    public enum OutputFormat {XML, CSV}

    private TestSelectorElementReport lastReport;

    private CtClass<?> testClassTargetOne;

    private boolean shouldTargetOneTestClass;

    private String absolutePathToProjectRoot;

    private List<String> mutationsScoreResults;

    public PitMutantScoreSelector(AutomaticBuilder automaticBuilder,
                                  UserInput configuration) {
        super(automaticBuilder, configuration);
        this.mutationsScoreResults = new ArrayList<>();
        this.absolutePathToProjectRoot = configuration.getAbsolutePathToProjectRoot();
        this.shouldTargetOneTestClass = configuration.shouldTargetOneTestClass();
        this.testClassTargetOne =
                configuration.getTestClasses() == null || configuration.getTestClasses().isEmpty() ? null :
                        configuration.getFactory().Class().get(configuration.getTestClasses().get(0));
        this.testThatKilledMutants = new HashMap<>();
        this.parser = new PitXMLResultParser();
        final String pathPitResult = configuration.getPathPitResult();
        if (pathPitResult == null || pathPitResult.isEmpty()) {
            return;
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
        AbstractParser originalResultParser;
        switch (originalFormat) {
            case CSV:
                parser = originalResultParser = new PitCSVResultParser();
                break;
            default:
                parser = originalResultParser = new PitXMLResultParser();
                break;
        }
        this.mutationsScoreResults.add(this.readFileContentAsString(pathPitResult));
        initOriginalPitResult(originalResultParser.parse(new File(pathPitResult)));
    }

    @Override
    public boolean init() {
        if (this.originalKilledMutants == null) {
            if (shouldTargetOneTestClass) {
                this.automaticBuilder.runPit(testClassTargetOne);
            } else {
                try {
                    this.automaticBuilder.runPit();
                } catch (Throwable e) {
                    LOGGER.error(ErrorEnum.ERROR_ORIGINAL_MUTATION_SCORE.getMessage());
                    DSpotState.GLOBAL_REPORT.addError(new Error(ErrorEnum.ERROR_ORIGINAL_MUTATION_SCORE, e));
                    return false;
                }
            }
            this.mutationsScoreResults.add(
                    this.readFileContentAsString(
                            this.parser.getPathOfMutationsFile(this.absolutePathToProjectRoot + this.automaticBuilder.getOutputDirectoryPit()).getAbsolutePath()
                    )
            );
            initOriginalPitResult(parser.parseAndDelete(this.absolutePathToProjectRoot + this.automaticBuilder.getOutputDirectoryPit()));
        } else {
            this.baselineKilledMutants = new ArrayList<>();
            for (AbstractPitResult r : this.originalKilledMutants) {
                this.baselineKilledMutants.add(r.clone());
            }
        }
        return true;
    }

    private void initOriginalPitResult(List<AbstractPitResult> results) {
        this.numberOfMutant = results.size();
        this.mutantNotTestedByOriginal = results.stream()
                .filter(result -> result.getStateOfMutant() != AbstractPitResult.State.KILLED)
                .filter(result -> result.getStateOfMutant() != AbstractPitResult.State.SURVIVED)
                .filter(result -> result.getStateOfMutant() != AbstractPitResult.State.NO_COVERAGE)
                .collect(Collectors.toList());
        this.originalKilledMutants = results.stream()
                .filter(result -> result.getStateOfMutant() == AbstractPitResult.State.KILLED)
                .collect(Collectors.toList());
        LOGGER.info("The original test suite kill {} / {}", this.originalKilledMutants.size(), results.size());
        this.baselineKilledMutants = new ArrayList<>();
        for (AbstractPitResult r : this.originalKilledMutants) {
            this.baselineKilledMutants.add(r.clone());
        }
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(CtType<?> classTest, List<CtMethod<?>> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
            this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();

        }
        return testsToBeAmplified;
    }

    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
        if (amplifiedTestToBeKept.isEmpty()) {
            return amplifiedTestToBeKept;
        }

        // prepare clone of the test class
        CtType clone = this.currentClassTestToBeAmplified.clone();
        clone.setParent(this.currentClassTestToBeAmplified.getParent());

        // remove test methods from clone that are in original test class and add all amplified methods
        this.currentClassTestToBeAmplified.getMethods().stream()
                .filter(TestFramework.get()::isTest)
                .forEach(clone::removeMethod);
        amplifiedTestToBeKept.forEach(clone::addMethod);

        // print clone to file and run pit on it
        //PIT cannot be executed on test classes containing parallel execution annotations
        CloneHelper.removeParallelExecutionAnnotation(clone, amplifiedTestToBeKept);
        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.getPathToAmplifiedTestSrc()));
        final String classpath = this.automaticBuilder
                .buildClasspath()
                + AmplificationHelper.PATH_SEPARATOR +
                this.targetClasses
                + AmplificationHelper.PATH_SEPARATOR + DSpotUtils.getAbsolutePathToDSpotDependencies();
        DSpotCompiler.compile(
                DSpotCompiler.getPathToAmplifiedTestSrc(),
                classpath,
                new File(this.absolutePathToProjectRoot + "/" + this.pathToTestClasses)
        );
        this.automaticBuilder.runPit(clone);
        this.mutationsScoreResults.add(this.readFileContentAsString(
                parser.getPathOfMutationsFile(this.absolutePathToProjectRoot + automaticBuilder.getOutputDirectoryPit()).getAbsolutePath())
        );
        final List<AbstractPitResult> results = parser.parseAndDelete(this.absolutePathToProjectRoot + automaticBuilder.getOutputDirectoryPit());
        Set<CtMethod<?>> selectedTests = new HashSet<>();
        if (results != null) {
            LOGGER.info("{} mutants has been generated ({})", results.size(), this.numberOfMutant);
            if (results.size() != this.numberOfMutant) {
                LOGGER.warn("Number of generated mutant is different than the original one.");
            }

            // keep results where amplified tests kill a mutant not killed (but tested) by original test
            results.stream()
                    .filter(result -> result.getStateOfMutant() == AbstractPitResult.State.KILLED &&
                            !this.baselineKilledMutants.contains(result) &&
                            !this.mutantNotTestedByOriginal.contains(result))
                    .forEach(result -> {
                        CtMethod method = result.getMethod(clone);

                        // keep methods that kill mutants not killed before
                        if (killsNewMutant(result)) {
                            if (!testThatKilledMutants.containsKey(method)) {
                                testThatKilledMutants.put(method, new HashSet<>());
                            }
                            testThatKilledMutants.get(method).add(result);
                            if (method == null) {

                                // output of pit test does not allow us to know which test case kill new mutants... we keep them all...
                                selectedTests.addAll(amplifiedTestToBeKept);
                            } else {
                                selectedTests.add(method);
                            }
                        }
                    });
        }
        this.selectedAmplifiedTest.addAll(selectedTests);
        selectedTests.forEach(selectedTest ->
                LOGGER.info("{} kills {} more mutants",
                        selectedTest == null ?
                                this.currentClassTestToBeAmplified.getSimpleName() : selectedTest.getSimpleName(),
                        this.testThatKilledMutants.containsKey(selectedTest) ?
                                this.testThatKilledMutants.get(selectedTest).size() : this.testThatKilledMutants.get(null))
        );
        return new ArrayList<>(selectedTests);
    }

    private String readFileContentAsString(final String pathname) {
        try (BufferedReader buffer = new BufferedReader(new FileReader(pathname))) {
            return buffer.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean killsNewMutant(AbstractPitResult result) {
        if (baselineKilledMutants.contains(result)) {
            return false;
        }

        // add result to baseline to prohibit selection of identical amplified tests
        baselineKilledMutants.add(result);
        return true;
    }

    @Override
    public TestSelectorElementReport report() {
        if (currentClassTestToBeAmplified == null) {
            return lastReport;
        }
        final String reportStdout = reportStdout();
        final TestClassJSON testClassJSON = reportJSONMutants();

        //clean up for the next class
        this.currentClassTestToBeAmplified = null;
        this.testThatKilledMutants.clear();
        this.selectedAmplifiedTest.clear();

        this.lastReport = new TestSelectorElementReportImpl(
                reportStdout,
                testClassJSON,
                this.mutationsScoreResults,
                this.parser instanceof PitXMLResultParser ? ".xml" : ".csv"
        );
        return lastReport;
    }

    private String reportStdout() {
        return "Test class that has been amplified: " + this.currentClassTestToBeAmplified.getQualifiedName() +
                AmplificationHelper.LINE_SEPARATOR +
                "The original test suite kills " +
                this.originalKilledMutants.size() +
                " mutants" + AmplificationHelper.LINE_SEPARATOR +
                "The amplification results with " +
                this.testThatKilledMutants.size() +
                " new tests" + AmplificationHelper.LINE_SEPARATOR +
                "it kills " + getNbTotalNewMutantKilled() +
                " more mutants" + AmplificationHelper.LINE_SEPARATOR;
    }

    private long getNbTotalNewMutantKilled() {
        return this.testThatKilledMutants.keySet()
                .stream()
                .flatMap(method -> this.testThatKilledMutants.get(method).stream())
                .distinct()
                .count();
    }

    private TestClassJSON reportJSONMutants() {
        if (this.currentClassTestToBeAmplified == null) {
            LOGGER.warn("The current test class is null.");
            return new TestClassJSON(getNbMutantKilledOriginally(this.currentClassTestToBeAmplified.getQualifiedName()),
                    "unknown",
                    this.currentClassTestToBeAmplified.getMethods()
                            .stream()
                            .filter(TestFramework.get()::isTest)
                            .count());
        }
        TestClassJSON testClassJSON;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(this.outputDirectory + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "report.json");
        if (file.exists()) {
            try {
                testClassJSON = gson.fromJson(new FileReader(file), TestClassJSON.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            testClassJSON = new TestClassJSON(getNbMutantKilledOriginally(this.currentClassTestToBeAmplified.getQualifiedName()),
                    this.getNbTotalNewMutantKilled(),
                    this.currentClassTestToBeAmplified.getQualifiedName(),
                    this.currentClassTestToBeAmplified.getMethods()
                            .stream()
                            .filter(TestFramework.get()::isTest)
                            .count());
        }
        List<CtMethod> keys = new ArrayList<>(this.testThatKilledMutants.keySet());
        keys.forEach(amplifiedTest -> {
                    List<AbstractPitResult> pitResults = new ArrayList<>(this.testThatKilledMutants.get(amplifiedTest));
                    final List<MutantJSON> mutantsJson = new ArrayList<>();
                    pitResults.forEach(pitResult -> mutantsJson.add(new MutantJSON(
                            pitResult.getFullQualifiedNameMutantOperator(),
                            pitResult.getLineNumber(),
                            pitResult.getNameOfMutatedMethod()
                    )));
                    if (amplifiedTest == null) {
                        testClassJSON.addTestCase(new TestCaseJSON(
                                this.currentClassTestToBeAmplified.getSimpleName(),
                                Counter.getAllAssertions(),
                                Counter.getAllInput(),
                                mutantsJson
                        ));
                    } else {
                        testClassJSON.addTestCase(new TestCaseJSON(
                                amplifiedTest.getSimpleName(),
                                Counter.getAssertionOfSinceOrigin(amplifiedTest),
                                Counter.getInputOfSinceOrigin(amplifiedTest),
                                mutantsJson
                        ));
                    }
                }
        );
        return testClassJSON;
    }

    private int getNbMutantKilledOriginally(String qualifiedName) {
        return (int) this.originalKilledMutants.stream()
                .filter(pitResult ->
                        qualifiedName.equals(pitResult.getFullQualifiedNameOfKiller())
                )
                .count();
    }

}
