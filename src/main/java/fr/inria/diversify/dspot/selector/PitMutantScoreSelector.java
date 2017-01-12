package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.dspot.support.Counter;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitRunner;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.PrintClassUtils;
import org.json.JSONObject;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public class PitMutantScoreSelector implements TestSelector {

    private InputProgram program;

    private InputConfiguration configuration;

    private CtType currentClassTestToBeAmplified;

    private List<PitResult> originalPitResults;

    private Map<CtMethod, List<PitResult>> currentMutantsKilledPerTestCase;

    private Map<CtMethod, List<PitResult>> testThatKilledMutants;

    public PitMutantScoreSelector() {
        this.testThatKilledMutants = new HashMap<>();
        this.currentMutantsKilledPerTestCase = new HashMap<>();
        this.originalPitResults = new ArrayList<>();
    }

    //TODO That the configuration is well set up and well used.
    //TODO maybe we can do it one time for the whole project.
    @Override
    public void init(InputConfiguration configuration) {
        this.configuration = configuration;
        try {
            InitUtils.initLogLevel(configuration);
            this.program = InitUtils.initInputProgram(this.configuration);
            String outputDirectory = configuration.getProperty("tmpDir") + "/tmp_pit_" + System.currentTimeMillis();
            FileUtils.copyDirectory(new File(this.program.getProgramDir()), new File(outputDirectory));
            this.program.setProgramDir(outputDirectory);
            InitUtils.initDependency(configuration);
            String mavenHome = configuration.getProperty("maven.home", null);
            String mavenLocalRepository = configuration.getProperty("maven.localRepository", null);
            DSpotUtils.compile(this.program, mavenHome, mavenLocalRepository);
            DSpotUtils.initClassLoader(this.program, configuration);
            DSpotCompiler.buildCompiler(this.program, true);
            DSpotUtils.compileTests(this.program, mavenHome, mavenLocalRepository);
            InitUtils.initLogLevel(configuration);
        } catch (Exception | InvalidSdkException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset() {
        //empty
    }

    @Override
    public List<CtMethod> selectToAmplify(List<CtMethod> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
            this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();
            this.originalPitResults = PitRunner.run(this.program, this.configuration, this.currentClassTestToBeAmplified)
                    .stream()
                    .filter(result -> result.getStateOfMutant() == PitResult.State.KILLED)
                    .collect(Collectors.toList());
        }
        return testsToBeAmplified;
    }

    @Override
    public List<CtMethod> selectToKeep(List<CtMethod> amplifiedTestToBeKept) {
        if (amplifiedTestToBeKept.isEmpty()) {
            return amplifiedTestToBeKept;
        }
        CtType clone = this.currentClassTestToBeAmplified.clone();
        clone.setParent(this.currentClassTestToBeAmplified.getParent());
        ((Set<CtMethod>) this.currentClassTestToBeAmplified.getMethods()).forEach(clone::removeMethod);
        amplifiedTestToBeKept.forEach(clone::addMethod);

        try {
            PrintClassUtils.printJavaFile(new File(this.program.getAbsoluteTestSourceCodeDir()), clone);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<PitResult> results = PitRunner.run(this.program, this.configuration, clone);

        List<CtMethod> selectedTests = new ArrayList<>();
        if (results != null) {
            results.stream()
                    .filter(result -> result.getStateOfMutant() == PitResult.State.KILLED)
                    .filter(result -> !this.originalPitResults.contains(result))
                    .forEach(result -> {
                        if (!this.currentMutantsKilledPerTestCase.containsKey(result.getTestCaseMethod())) {
                            this.currentMutantsKilledPerTestCase.put(result.getTestCaseMethod(), new ArrayList<>());
                        }
                        this.currentMutantsKilledPerTestCase.get(result.getTestCaseMethod()).add(result);
                        selectedTests.add(result.getTestCaseMethod());
                    });
        }

        try {
            PrintClassUtils.printJavaFile(new File(this.program.getAbsoluteTestSourceCodeDir()), this.currentClassTestToBeAmplified);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        selectedTests.forEach(testMethod -> {
            if (!this.testThatKilledMutants.containsKey(testMethod)) {
                this.testThatKilledMutants.put(testMethod, this.currentMutantsKilledPerTestCase.get(testMethod));
            } else {
                this.testThatKilledMutants.get(testMethod).addAll(this.currentMutantsKilledPerTestCase.get(testMethod));
            }
        });

        return selectedTests;
    }

    @Override
    public void update() {
        // empty
    }

    @Override
    public void report() {
        reportStdout();
        reportJSONClass();
        reportJSONMutants();
        //clean up for the next class
        this.currentClassTestToBeAmplified = null;
        this.currentMutantsKilledPerTestCase.clear();
        this.originalPitResults.clear();
    }

    private void reportStdout() {
        final StringBuilder string = new StringBuilder();
        final String nl = System.getProperty("line.separator");
        long nbOfTotalMutantKilled = getNbTotalNewMutantKilled();
        string.append(nl).append("======= REPORT =======").append(nl);
        string.append("PitMutantScoreSelector: ").append(nl);
        string.append("The original test suite kill ").append(this.originalPitResults.size()).append(" mutants").append(nl);
        string.append("The amplification results with ").append(
                this.testThatKilledMutants.size()).append(" new tests").append(nl);
        string.append("it kill ")
                .append(nbOfTotalMutantKilled).append(" more mutants").append(nl);
        System.out.println(string.toString());

        File reportDir = new File("dspot-report");
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }
        try (FileWriter writer = new FileWriter("dspot-report/report.txt", false)) {
            writer.write(string.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getNbTotalNewMutantKilled() {
        return this.testThatKilledMutants.keySet()
                .stream()
                .flatMap(method -> this.testThatKilledMutants.get(method).stream())
                .map(PitResult::getFullQualifiedNameMutantOperator)
                .distinct()
                .count();
    }

    @Deprecated
    private double fitness(CtMethod test) {
        return ((double) this.testThatKilledMutants.get(test).size() / (double) (Counter.getAssertionOfSinceOrigin(test) + Counter.getInputOfSinceOrigin(test)));
    }

    private void reportJSONClass() {
        final JSONObject json = new JSONObject();
        this.testThatKilledMutants.keySet().forEach(amplifiedTest ->
                json.accumulate(this.currentClassTestToBeAmplified.getQualifiedName(),
                        new JSONObject().put(amplifiedTest.getSimpleName(), amplifiedTest.toString()))
        );
        try (FileWriter writer = new FileWriter("dspot-report/generated_test_classes.json", false)) {
            writer.write(json.toString(3));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reportJSONMutants() {
        final JSONObject json = new JSONObject();
        this.testThatKilledMutants.keySet().forEach(amplifiedTest ->
                json.accumulate(this.currentClassTestToBeAmplified.getQualifiedName(),
                        new JSONObject().put(amplifiedTest.getSimpleName(),
                                buildResultTest(amplifiedTest))
                )
        );
        try (FileWriter writer = new FileWriter("dspot-report/mutants_killed.json", false)) {
            writer.write(json.toString(3));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject buildResultTest(CtMethod amplifiedTest) {
        JSONObject json = new JSONObject()
                .put("#AssertionAdded",
                        Counter.getAssertionOfSinceOrigin(amplifiedTest))
                .put("#InputAdded",
                        Counter.getInputOfSinceOrigin(amplifiedTest))
                .put("#MutantKilled",
                        this.testThatKilledMutants.get(amplifiedTest).size());
        this.testThatKilledMutants.get(amplifiedTest).forEach(mutant ->
                json.accumulate("MutantsKilled", getMutantKilled(mutant))
        );
        return json;
    }

    private JSONObject getMutantKilled(PitResult element) {
        JSONObject mutant = new JSONObject();
        return mutant.put("ID", element.getFullQualifiedNameMutantOperator())
                .put("lineNumber", element.getLineNumber())
                .put("location", element.getLocation());
    }

}
