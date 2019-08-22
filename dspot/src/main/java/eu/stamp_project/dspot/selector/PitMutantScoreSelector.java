package eu.stamp_project.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.Main;
import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.utils.pit.*;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.report.error.Error;
import eu.stamp_project.utils.report.error.ErrorEnum;
import eu.stamp_project.utils.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.utils.report.output.selector.TestSelectorElementReportImpl;
import eu.stamp_project.utils.report.output.selector.mutant.json.MutantJSON;
import eu.stamp_project.utils.report.output.selector.mutant.json.TestCaseJSON;
import eu.stamp_project.utils.report.output.selector.mutant.json.TestClassJSON;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.collector.CollectorConfig;
import eu.stamp_project.utils.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public PitMutantScoreSelector() {
        this(OutputFormat.XML);
    }

    public PitMutantScoreSelector(OutputFormat format) {
        this.testThatKilledMutants = new HashMap<>();
        switch (format) {
            case XML:
                parser = new PitXMLResultParser();
                break;
            case CSV:
                parser = new PitCSVResultParser();
                break;
        }
    }

    public PitMutantScoreSelector(String pathToOriginalResultOfPit, OutputFormat originalFormat, OutputFormat consecutiveFormat) {
        this(consecutiveFormat);
        AbstractParser originalResultParser;
        switch (originalFormat) {
            case CSV:
                parser = originalResultParser = new PitCSVResultParser();
                break;
            default:
                parser = originalResultParser = new PitXMLResultParser();
                break;
        }
        initOriginalPitResult(originalResultParser.parse(new File(pathToOriginalResultOfPit)));
    }

    @Override
    public boolean init() {
        if (this.originalKilledMutants == null) {
            final AutomaticBuilder automaticBuilder = InputConfiguration.get().getBuilder();
            if (InputConfiguration.get().shouldTargetOneTestClass()) {
                automaticBuilder.runPit(
                        InputConfiguration.get().getFactory().Class().get(InputConfiguration.get().getTestClasses().get(0))
                );
            } else {
                try {
                    automaticBuilder.runPit();
                } catch (Throwable e) {
                    LOGGER.error(ErrorEnum.ERROR_ORIGINAL_MUTATION_SCORE.getMessage());
                    Main.GLOBAL_REPORT.addError(new Error(ErrorEnum.ERROR_ORIGINAL_MUTATION_SCORE, e));
                    return false;
                }
            }
            initOriginalPitResult(parser.parseAndDelete(InputConfiguration.get().getAbsolutePathToProjectRoot() + automaticBuilder.getOutputDirectoryPit()));
        } else {
            baselineKilledMutants = new ArrayList<>();
            for(AbstractPitResult r : originalKilledMutants) {
                baselineKilledMutants.add(r.clone());
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
        baselineKilledMutants = new ArrayList<>();
        for(AbstractPitResult r : originalKilledMutants) {
            baselineKilledMutants.add(r.clone());
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
        final AutomaticBuilder automaticBuilder = InputConfiguration.get().getBuilder();
        final String classpath = InputConfiguration.get().getBuilder()
                .buildClasspath()
                + AmplificationHelper.PATH_SEPARATOR +
                InputConfiguration.get().getClasspathClassesProject()
                + AmplificationHelper.PATH_SEPARATOR + DSpotUtils.getAbsolutePathToDSpotDependencies();
        DSpotCompiler.compile(InputConfiguration.get(), DSpotCompiler.getPathToAmplifiedTestSrc(), classpath,
                new File(InputConfiguration.get().getAbsolutePathToTestClasses()));

        InputConfiguration.get().getBuilder().runPit(clone);
        final List<AbstractPitResult> results = parser.parseAndDelete(InputConfiguration.get().getAbsolutePathToProjectRoot() + automaticBuilder.getOutputDirectoryPit());
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
        if(currentClassTestToBeAmplified == null) {
            return lastReport;
        }
        final String reportStdout = reportStdout();
        final TestClassJSON testClassJSON = reportJSONMutants();

        //clean up for the next class
        this.currentClassTestToBeAmplified = null;
        this.testThatKilledMutants.clear();
        this.selectedAmplifiedTest.clear();
        lastReport = new TestSelectorElementReportImpl(reportStdout, testClassJSON);
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
        final File file = new File(InputConfiguration.get().getOutputDirectory() + "/" +
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
        CollectorConfig.getInstance().getInformationCollector().reportSelectorInformation(testClassJSON.toString());
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
