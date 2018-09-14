package eu.stamp_project.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.dspot.selector.json.mutant.MutantJSON;
import eu.stamp_project.dspot.selector.json.mutant.TestCaseJSON;
import eu.stamp_project.dspot.selector.json.mutant.TestClassJSON;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.mutant.pit.PitResult;
import eu.stamp_project.mutant.pit.PitResultParser;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.minimization.Minimizer;
import eu.stamp_project.minimization.PitMutantMinimizer;
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

    private List<PitResult> originalKilledMutants;

    private Map<CtMethod, Set<PitResult>> testThatKilledMutants;

    private List<PitResult> mutantNotTestedByOriginal;

    public PitMutantScoreSelector() {
        this.testThatKilledMutants = new HashMap<>();
    }

    public PitMutantScoreSelector(String pathToOriginalResultOfPit) {
        this();
        initOriginalPitResult(PitResultParser.parse(new File(pathToOriginalResultOfPit)));
    }

    @Override
    public void init(InputConfiguration configuration) {
        super.init(configuration);
        /*
        if (!configuration.getPitVersion().isEmpty()) {
            pitVersion = configuration.getPitVersion();
        } else if (descartesMode) {
            pitVersion = "1.4.0";
        }
        */
        if (this.originalKilledMutants == null) {
            final AutomaticBuilder automaticBuilder = InputConfiguration.get().getBuilder();
            automaticBuilder.runPit(this.configuration.getAbsolutePathToProjectRoot());
            initOriginalPitResult(PitResultParser.parseAndDelete(this.configuration.getAbsolutePathToProjectRoot() + automaticBuilder.getOutputDirectoryPit()) );
        }
    }

    private void initOriginalPitResult(List<PitResult> results) {
        this.numberOfMutant = results.size();
        this.mutantNotTestedByOriginal = results.stream()
                .filter(result -> result.getStateOfMutant() != PitResult.State.KILLED)
                .filter(result -> result.getStateOfMutant() != PitResult.State.SURVIVED)
                .filter(result -> result.getStateOfMutant() != PitResult.State.NO_COVERAGE)
                .collect(Collectors.toList());
        this.originalKilledMutants = results.stream()
                .filter(result -> result.getStateOfMutant() == PitResult.State.KILLED)
                .collect(Collectors.toList());
        LOGGER.info("The original test suite kill {} / {}", this.originalKilledMutants.size(), results.size());
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(List<CtMethod<?>> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
            this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();
            this.testThatKilledMutants.clear();
            this.selectedAmplifiedTest.clear();
        }
        return testsToBeAmplified;
    }

    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
        if (amplifiedTestToBeKept.isEmpty()) {
            return amplifiedTestToBeKept;
        }
        CtType clone = this.currentClassTestToBeAmplified.clone();
        clone.setParent(this.currentClassTestToBeAmplified.getParent());
        this.currentClassTestToBeAmplified.getMethods().stream()
                .filter(AmplificationChecker::isTest)
                .forEach(clone::removeMethod);
        amplifiedTestToBeKept.forEach(clone::addMethod);

        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.getPathToAmplifiedTestSrc()));
        final AutomaticBuilder automaticBuilder = InputConfiguration.get().getBuilder();
        final String classpath = InputConfiguration.get().getBuilder()
                .buildClasspath()
                + AmplificationHelper.PATH_SEPARATOR +
                this.configuration.getClasspathClassesProject()
                + AmplificationHelper.PATH_SEPARATOR + DSpotUtils.getAbsolutePathToDSpotDependencies();

        DSpotCompiler.compile(this.configuration, DSpotCompiler.getPathToAmplifiedTestSrc(), classpath,
                new File(this.configuration.getAbsolutePathToTestClasses()));

        InputConfiguration.get().getBuilder().runPit(this.configuration.getAbsolutePathToProjectRoot(), clone);
        final List<PitResult> results = PitResultParser.parseAndDelete(this.configuration.getAbsolutePathToProjectRoot() + automaticBuilder.getOutputDirectoryPit());

        Set<CtMethod<?>> selectedTests = new HashSet<>();
        if (results != null) {
            LOGGER.info("{} mutants has been generated ({})", results.size(), this.numberOfMutant);
            if (results.size() != this.numberOfMutant) {
                LOGGER.warn("Number of generated mutant is different than the original one.");
            }
            results.stream()
                    .filter(result -> result.getStateOfMutant() == PitResult.State.KILLED &&
                            !this.originalKilledMutants.contains(result) &&
                            !this.mutantNotTestedByOriginal.contains(result))
                    .forEach(result -> {
                        CtMethod method = result.getMethod(clone);
                        if (killsMoreMutantThanParents(method, result)) {
                            if (!testThatKilledMutants.containsKey(method)) {
                                testThatKilledMutants.put(method, new HashSet<>());
                            }
                            testThatKilledMutants.get(method).add(result);
                            if (method == null) {
                                selectedTests.addAll(amplifiedTestToBeKept);// output of pit test does not allow us to know which test case kill new mutants... we keep them all...
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

    private boolean killsMoreMutantThanParents(CtMethod test, PitResult result) {
        CtMethod parent = AmplificationHelper.getAmpTestParent(test);
        while (parent != null) {
            if (this.testThatKilledMutants.get(parent) != null &&
                    this.testThatKilledMutants.get(parent).contains(result)) {
                return false;
            }
            parent = AmplificationHelper.getAmpTestParent(parent);
        }
        return true;
    }

    @Override
    public void report() {
        reportStdout();
        reportJSONMutants();
        //clean up for the next class
        this.currentClassTestToBeAmplified = null;
    }

    private void reportStdout() {
        final StringBuilder string = new StringBuilder();
        final String nl = System.getProperty("line.separator");
        long nbOfTotalMutantKilled = getNbTotalNewMutantKilled();
        string.append(nl).append("======= REPORT =======").append(nl);
        string.append("PitMutantScoreSelector: ").append(nl);
        string.append("The original test suite kills ").append(this.originalKilledMutants.size()).append(" mutants").append(nl);
        string.append("The amplification results with ").append(
                this.testThatKilledMutants.size()).append(" new tests").append(nl);
        string.append("it kills ")
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

    private long getNbTotalNewMutantKilled() {
        return this.testThatKilledMutants.keySet()
                .stream()
                .flatMap(method -> this.testThatKilledMutants.get(method).stream())
                .distinct()
                .count();
    }

    private void reportJSONMutants() {
        if (this.currentClassTestToBeAmplified == null) {
            return;
        }
        TestClassJSON testClassJSON;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(this.configuration.getOutputDirectory() + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "_mutants_killed.json");
        if (file.exists()) {
            try {
                testClassJSON = gson.fromJson(new FileReader(file), TestClassJSON.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            testClassJSON = new TestClassJSON(getNbMutantKilledOriginally(this.currentClassTestToBeAmplified.getQualifiedName()),
                    this.currentClassTestToBeAmplified.getQualifiedName(),
                    this.currentClassTestToBeAmplified.getMethods()
                            .stream()
                            .filter(AmplificationChecker::isTest)
                            .count());
        }
        List<CtMethod> keys = new ArrayList<>(this.testThatKilledMutants.keySet());
        keys.forEach(amplifiedTest -> {
                    List<PitResult> pitResults = new ArrayList<>(this.testThatKilledMutants.get(amplifiedTest));
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

    private int getNbMutantKilledOriginally(String qualifiedName) {
        return (int) this.originalKilledMutants.stream()
                .filter(pitResult ->
                        qualifiedName.equals(pitResult.getFullQualifiedNameOfKiller())
                )
                .count();
    }

    @Override
    public Minimizer getMinimizer() {
        return new PitMutantMinimizer(this.currentClassTestToBeAmplified, this.configuration, this.testThatKilledMutants);
    }
}
