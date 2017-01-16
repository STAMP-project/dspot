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
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
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

    private Map<CtMethod, Set<PitResult>> testThatKilledMutants;

    public PitMutantScoreSelector() {
        this.testThatKilledMutants = new HashMap<>();
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

//            if (this.originalPitResults == null) {
//                List<PitResult> pitResults = PitRunner.runAll(this.program, this.configuration);
//                this.originalPitResults = pitResults.stream()
//                        .filter(result -> result.getStateOfMutant() == PitResult.State.KILLED)
//                        .collect(Collectors.toList());
//                Log.debug("The original test suite kill {} / {}", this.originalPitResults.size(), pitResults.size());
//            }

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
            List<PitResult> pitResults = PitRunner.run(this.program, this.configuration, this.currentClassTestToBeAmplified);
            this.originalPitResults = pitResults.stream()
                    .filter(result -> result.getStateOfMutant() == PitResult.State.KILLED)
                    .collect(Collectors.toList());
            Log.debug("The original test suite kill {} / {}", this.originalPitResults.size(), pitResults.size());
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
        Set<CtMethod> selectedTests = new HashSet<>();
        if (results != null) {
            results.stream()
                    .filter(result -> result.getStateOfMutant() == PitResult.State.KILLED)
                    .filter(result -> !this.originalPitResults.contains(result))
                    .forEach(result -> {
                        if (!testThatKilledMutants.containsKey(result.getTestCaseMethod())) {
                            testThatKilledMutants.put(result.getTestCaseMethod(), new HashSet<>());
                        }
                        testThatKilledMutants.get(result.getTestCaseMethod()).add(result);
                        selectedTests.add(result.getTestCaseMethod());
                    });
        }

        selectedTests.forEach(test ->
                Log.debug("{} kills {} ", test.getSimpleName(), this.testThatKilledMutants.get(test).size())
        );

        try {
            PrintClassUtils.printJavaFile(new File(this.program.getAbsoluteTestSourceCodeDir()), this.currentClassTestToBeAmplified);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ArrayList<>(selectedTests);
    }

    @Override
    public void update() {
        // empty
    }

    @Override
    public void report() {
        reportStdout();
//        createAmplifiedClass();
        reportJSONMutants();
        //clean up for the next class
        this.currentClassTestToBeAmplified = null;
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

        File reportDir = new File(this.configuration.getOutputDirectory());
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }
        try (FileWriter writer = new FileWriter(this.configuration.getOutputDirectory() + "/"+
                this.currentClassTestToBeAmplified.getQualifiedName() + "_mutants_report.txt", false)) {
            writer.write(string.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    private static final String nl = System.getProperty("line.separator");
    private static final String tab = "\t";

    private void reportJSONMutants() {
        try (FileWriter writer = new FileWriter(this.configuration.getOutputDirectory() + "/"+
                this.currentClassTestToBeAmplified.getQualifiedName() + "_mutants_killed.json", false)) {
            writer.write("{" + nl);
            writer.write(tab + "\"" +
                    this.currentClassTestToBeAmplified.getQualifiedName() + "\": {" + nl);
            List<CtMethod> keys = new ArrayList<>(this.testThatKilledMutants.keySet());
            keys.forEach(amplifiedTest -> {
                        final StringBuilder string = new StringBuilder();
                        string.append(tab).append(tab)
                                .append("\"").append(amplifiedTest.getSimpleName()).append("\":{").append(nl)
                                .append(tab).append(tab).append(tab)
                                .append("\"#AssertionAdded\":").append(Counter.getAssertionOfSinceOrigin(amplifiedTest)).append(",").append(nl)
                                .append(tab).append(tab).append(tab)
                                .append("\"#InputAdded\":").append(Counter.getInputOfSinceOrigin(amplifiedTest)).append(",").append(nl)
                                .append(tab).append(tab).append(tab)
                                .append("\"#MutantKilled\":").append(this.testThatKilledMutants.get(amplifiedTest).size()).append(",").append(nl)
                                .append(tab).append(tab).append(tab)
                                .append("\"MutantsKilled\":[").append(nl)
                                .append(buildListOfIdMutantKilled(amplifiedTest))
                                .append(tab).append(tab).append(tab).append("]").append(nl)
                                .append(tab).append(tab).append("}");
                        if (keys.indexOf(amplifiedTest) != keys.size() - 1)
                            string.append(",");
                        string.append(nl);
                        try {
                            writer.write(string.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            writer.write(nl + tab + "}");
            writer.write(nl + "}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private StringBuilder buildListOfIdMutantKilled(CtMethod amplifiedTest) {
        List<PitResult> pitResults = new ArrayList<>(this.testThatKilledMutants.get(amplifiedTest));
        return pitResults.stream()
                .reduce(new StringBuilder(),
                        (builder, element) -> {
                            if (pitResults.indexOf(element) ==
                                    pitResults.size() - 1) {
                                return builder.append(getLineOfMutantKilled(element));
                            } else {
                                return builder.append(getLineOfMutantKilled(element)).append(",").append(nl);
                            }
                        }, StringBuilder::append);
    }

    private StringBuilder getLineOfMutantKilled(PitResult element) {
        return new StringBuilder().append(tab).append(tab).append(tab).append("{").append(nl)
                .append(tab).append(tab).append(tab).append(tab)
                .append("\"ID\":").append("\"").append(element.getFullQualifiedNameMutantOperator()).append("\",").append(nl)
                .append(tab).append(tab).append(tab).append(tab)
                .append("\"lineNumber\":\"").append(element.getLineNumber()).append("\",").append(nl)
                .append(tab).append(tab).append(tab).append(tab)
                .append("\"location\":\"").append(element.getLocation()).append("\"").append(nl)
                .append(tab).append(tab).append(tab).append("}").append(nl);
    }
}
