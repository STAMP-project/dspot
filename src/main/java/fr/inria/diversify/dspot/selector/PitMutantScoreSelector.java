package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpotUtils;
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

    private Map<CtMethod, List<PitResult>> mutantKilledPerTestCase;

    private List<CtMethod> testAlreadyRun;

    private List<CtMethod> testAlreadyAdded;

    public PitMutantScoreSelector() {

    }

    //TODO That the configuration is well set up and well used.
    //TODO maybe we can do it one time for the whole project.
    @Override
    public void init(InputConfiguration configuration) {
        this.configuration = configuration;
        this.mutantKilledPerTestCase = new HashMap<>();
        this.testAlreadyRun = new ArrayList<>();
        this.testAlreadyAdded = new ArrayList<>();
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
        this.currentClassTestToBeAmplified = null;
        this.mutantKilledPerTestCase.clear();
        this.testAlreadyRun.clear();
        this.testAlreadyAdded.clear();
    }

    @Override
    public List<CtMethod> selectToAmplify(List<CtMethod> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
            this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();
        }
        List<CtMethod> selectedTest = testsToBeAmplified.stream().filter(test -> !this.testAlreadyRun.contains(test)).collect(Collectors.toList());
        this.testAlreadyRun.addAll(selectedTest);
        return selectedTest;
    }

    @Override
    public List<CtMethod> selectToKeep(List<CtMethod> amplifiedTestToBeKept) {
        if (amplifiedTestToBeKept.isEmpty()) {
            return amplifiedTestToBeKept;
        }
        long time = System.currentTimeMillis();
        CtType clone = this.currentClassTestToBeAmplified.clone();
        clone.setParent(this.currentClassTestToBeAmplified.getParent());
        ((Set<CtMethod>)this.currentClassTestToBeAmplified.getMethods()).forEach(clone::removeMethod);
        amplifiedTestToBeKept.forEach(clone::addMethod);

        try {
            PrintClassUtils.printJavaFile(new File(this.program.getAbsoluteTestSourceCodeDir()), clone);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<PitResult> results = PitRunner.run(this.program, this.configuration, clone);

        Set<CtMethod> amplifiedTestToKeep = new HashSet<>();
        if (results != null) {
            if (results.stream()
                    .filter(result -> result.getStateOfMutant() == PitResult.State.KILLED)
                    .count() > this.mutantKilledPerTestCase.size()) {
                results.stream()
                        .filter(result -> result.getStateOfMutant() == PitResult.State.KILLED)
                        .forEach(result -> amplifiedTestToKeep.add(result.getTestCaseMethod()));
            }
            results.forEach(result ->
                    this.mutantKilledPerTestCase.put(result.getTestCaseMethod(), results.stream()
                            .filter(filterResults ->
                                    filterResults.getTestCaseMethod() != null &&
                                            filterResults.getTestCaseMethod().equals(result.getTestCaseMethod()))
                            .collect(Collectors.toList()))
            );
        }
        Log.debug("Time to run pit mutation coverage {} ms", System.currentTimeMillis() - time);

        try {
            PrintClassUtils.printJavaFile(new File(this.program.getAbsoluteTestSourceCodeDir()), this.currentClassTestToBeAmplified);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<CtMethod> finalList = new ArrayList<>(amplifiedTestToKeep.stream().filter(test ->  !this.testAlreadyAdded.contains(test)).collect(Collectors.toList()));
        this.testAlreadyAdded.addAll(finalList);
        return finalList;
    }

    @Override
    public void update() {
        //
    }

    @Override
    public void report() {
        //
    }

}
