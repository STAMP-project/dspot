package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.amplifier.*;
import fr.inria.diversify.dspot.assertGenerator.AssertGenerator;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 08/06/15
 * Time: 17:36
 */
public class DSpot {

    private List<Amplifier> amplifiers;
    private int numberOfIterations;

    public DSpotCompiler getCompiler() {
        return compiler;
    }

    private DSpotCompiler compiler;
    private InputProgram inputProgram;
    private DiversifyClassLoader applicationClassLoader;

    public DSpot(InputConfiguration inputConfiguration) throws InvalidSdkException, Exception {
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);
        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();
        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
        inputProgram.setProgramDir(outputDirectory);
        InitUtils.initDependency(inputConfiguration);
        String mavenHome = inputConfiguration.getProperty("maven.home", null);
        String mavenLocalRepository = inputConfiguration.getProperty("maven.localRepository", null);
        DSpotUtils.compile(inputProgram, mavenHome, mavenLocalRepository);
        applicationClassLoader = DSpotUtils.initClassLoader(inputProgram, inputConfiguration);
        DSpotUtils.addBranchLogger(inputProgram);

        compiler = DSpotCompiler.buildCompiler(inputProgram, true);
        DSpotUtils.compileTests(inputProgram, mavenHome, mavenLocalRepository);

        InitUtils.initLogLevel(inputConfiguration);
        numberOfIterations = 3;

        amplifiers = new ArrayList<>();
        this.amplifiers.add(new TestDataMutator());
        this.amplifiers.add(new TestMethodCallAdder());
        this.amplifiers.add(new TestMethodCallRemover());
        this.amplifiers.add(new StatementAdderOnAssert());
    }

    public DSpot(InputConfiguration configuration, int numberOfIterations) throws InvalidSdkException, Exception {
        this(configuration);
        this.numberOfIterations = numberOfIterations;
    }

    public DSpot(InputConfiguration configuration, int numberOfIterations, List<Amplifier> amplifiers) throws InvalidSdkException, Exception {
        this(configuration);
        this.amplifiers = amplifiers;
        this.numberOfIterations = numberOfIterations;
    }

    public List<CtType> amplifiyAllTests() throws InterruptedException, IOException, ClassNotFoundException {
        return inputProgram.getFactory().Class().getAll().stream()
                .filter(ctClass ->
                        ctClass.getMethods().stream()
                                .filter(method ->
                                        AmplificationChecker.isTest(method, inputProgram.getRelativeTestSourceCodeDir()))
                                .count() > 0)
                .map(this::amplifyTest)
                .collect(Collectors.toList());
    }

    public CtType amplifyTest(String fullName) throws InterruptedException, IOException, ClassNotFoundException {
        CtType<Object> clone = inputProgram.getFactory().Type().get(fullName).clone();
        clone.setParent(inputProgram.getFactory().Type().get(fullName).getParent());
        return amplifyTest(clone);
    }

    public CtType amplifyTest(CtType test) {
        try {
            File logDir = new File(inputProgram.getProgramDir() + "/log");
            Amplification testAmplification = new Amplification(inputProgram, compiler, applicationClassLoader, this.amplifiers, logDir);
            return testAmplification.amplification(test, numberOfIterations);
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void clean() throws IOException {
        FileUtils.forceDelete(compiler.getSourceOutputDirectory());
        FileUtils.forceDelete(compiler.getBinaryOutputDirectory());
        FileUtils.forceDelete(new File(inputProgram.getProgramDir()));
    }

    public InputProgram getInputProgram() {
        return inputProgram;
    }

}
