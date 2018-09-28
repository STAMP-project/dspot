package eu.stamp_project.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.selector.json.mutant.MutantJSON;
import eu.stamp_project.dspot.selector.json.mutant.TestCaseJSON;
import eu.stamp_project.dspot.selector.json.mutant.TestClassJSON;
import eu.stamp_project.mutant.pit.PitResult;
import eu.stamp_project.mutant.pit.PitResultParser;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 15/12/17
 */
@Deprecated
public class ExecutedMutantSelector extends TakeAllSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(PitMutantScoreSelector.class);

    private List<PitResult> originalMutantExecuted;

    private Map<CtMethod<?>, Set<PitResult>> mutantExecutedPerAmplifiedTestMethod;

    public ExecutedMutantSelector() {
        this.mutantExecutedPerAmplifiedTestMethod = new HashMap<>();
    }

    public ExecutedMutantSelector(String pathToInitialResults) {
        this.originalMutantExecuted = PitResultParser.parse(new File(pathToInitialResults));
        this.mutantExecutedPerAmplifiedTestMethod = new HashMap<>();
    }

    @Override
    public void init(InputConfiguration configuration) {
        super.init(configuration);
        if (this.originalMutantExecuted == null) {
            LOGGER.info("Computing executed mutants by the original test suite...");
            final AutomaticBuilder automaticBuilder = InputConfiguration.get().getBuilder();
            automaticBuilder.runPit(this.configuration.getAbsolutePathToProjectRoot());
            this.originalMutantExecuted =
                    PitResultParser.parseAndDelete(
                            this.configuration.getAbsolutePathToProjectRoot() + automaticBuilder.getOutputDirectoryPit()
                    ).stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED ||
                            pitResult.getStateOfMutant() == PitResult.State.SURVIVED)
                            .collect(Collectors.toList());
        }
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(CtType<?> classTest, List<CtMethod<?>> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
            this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();
            this.mutantExecutedPerAmplifiedTestMethod.clear();
            this.selectedAmplifiedTest.clear();
        }
        return testsToBeAmplified;
    }

    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
        if (amplifiedTestToBeKept.isEmpty()) {
            return amplifiedTestToBeKept;
        }

        // construct a test classes with only amplified tests
        CtType clone = this.currentClassTestToBeAmplified.clone();
        clone.setParent(this.currentClassTestToBeAmplified.getParent());
        this.currentClassTestToBeAmplified.getMethods().stream()
                .filter(AmplificationChecker::isTest)
                .forEach(clone::removeMethod);
        amplifiedTestToBeKept.forEach(clone::addMethod);

        // pretty print it
        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.PATH_TO_AMPLIFIED_TEST_SRC));

        // then compile
        final String classpath = this.configuration.getDependencies()
                + AmplificationHelper.PATH_SEPARATOR +
                this.configuration.getClasspathClassesProject()
                + DSpotUtils.getAbsolutePathToDSpotDependencies();

        DSpotCompiler.compile(configuration, DSpotCompiler.PATH_TO_AMPLIFIED_TEST_SRC, classpath,
                new File(this.configuration.getAbsolutePathToTestClasses()));

        InputConfiguration.get().getBuilder()
                .runPit(this.configuration.getAbsolutePathToProjectRoot(), clone);
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(this.configuration.getAbsolutePathToProjectRoot() +
                InputConfiguration.get().getBuilder().getOutputDirectoryPit());
        final int numberOfSelectedAmplifiedTest = pitResults.stream()
                .filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED ||
                        pitResult.getStateOfMutant() == PitResult.State.SURVIVED)
                .filter(pitResult -> !this.originalMutantExecuted.contains(pitResult))
                .map(pitResult -> {
                    final CtMethod amplifiedTestThatExecuteMoreMutants = pitResult.getMethod(clone);
                    if (!this.mutantExecutedPerAmplifiedTestMethod.containsKey(amplifiedTestThatExecuteMoreMutants)) {
                        this.mutantExecutedPerAmplifiedTestMethod.put(amplifiedTestThatExecuteMoreMutants, new HashSet<>());
                    }
                    this.mutantExecutedPerAmplifiedTestMethod.get(amplifiedTestThatExecuteMoreMutants).add(pitResult);
                    this.selectedAmplifiedTest.add(amplifiedTestThatExecuteMoreMutants);
                    return amplifiedTestThatExecuteMoreMutants;
                }).collect(Collectors.toSet()).size();
        LOGGER.info("{} has been selected to amplify the test suite", numberOfSelectedAmplifiedTest);
        return amplifiedTestToBeKept;
    }

    @Override
    public void report() {
        reportStdout();
        reportJSONMutants();
        //clean up for the next class
        this.currentClassTestToBeAmplified = null;
    }

    private long getNbTotalNewMutantKilled() {
        return this.mutantExecutedPerAmplifiedTestMethod.keySet()
                .stream()
                .flatMap(method -> this.mutantExecutedPerAmplifiedTestMethod.get(method).stream())
                .distinct()
                .count();
    }

    private void reportStdout() {
        final StringBuilder string = new StringBuilder();
        final String nl = System.getProperty("line.separator");
        long nbOfTotalMutantKilled = getNbTotalNewMutantKilled();
        string.append(nl).append("======= REPORT =======").append(nl);
        string.append("PitMutantScoreSelector: ").append(nl);
        string.append("The original test suite executes ").append(this.originalMutantExecuted.size()).append(" mutants").append(nl);
        string.append("The amplification results with ").append(
                this.selectedAmplifiedTest.size()).append(" new tests").append(nl);
        string.append("it executes ")
                .append(nbOfTotalMutantKilled).append(" more mutants").append(nl);
        System.out.println(string.toString());

        File reportDir = new File(this.configuration.getOutputDirectory());
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }
        if (this.currentClassTestToBeAmplified != null) {
            try (FileWriter writer = new FileWriter(this.configuration.getOutputDirectory() + "/" +
                    this.currentClassTestToBeAmplified.getQualifiedName() + "_mutants_report.txt", false)) {
                writer.write(string.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void reportJSONMutants() {
        if (this.currentClassTestToBeAmplified == null) {
            return;
        }
        TestClassJSON testClassJSON;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(this.configuration.getOutputDirectory() + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "_mutants_executed.json");
        if (file.exists()) {
            try {
                testClassJSON = gson.fromJson(new FileReader(file), TestClassJSON.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            testClassJSON = new TestClassJSON(getNbMutantExecutedOriginally(this.currentClassTestToBeAmplified.getQualifiedName()),
                    this.currentClassTestToBeAmplified.getQualifiedName(),
                    this.currentClassTestToBeAmplified.getMethods()
                            .stream()
                            .filter(AmplificationChecker::isTest)
                            .count());
        }
        List<CtMethod> keys = new ArrayList<>(this.mutantExecutedPerAmplifiedTestMethod.keySet());
        keys.forEach(amplifiedTest -> {
                    List<PitResult> pitResults = new ArrayList<>(this.mutantExecutedPerAmplifiedTestMethod.get(amplifiedTest));
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
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(testClassJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private int getNbMutantExecutedOriginally(String qualifiedName) {
        return (int) this.originalMutantExecuted.stream()
                .filter(pitResult ->
                        qualifiedName.equals(pitResult.getFullQualifiedNameOfKiller())
                ).count();
    }

}
