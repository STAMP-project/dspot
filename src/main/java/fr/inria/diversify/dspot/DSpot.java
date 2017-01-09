package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.amplifier.*;
import fr.inria.diversify.dspot.selector.BranchCoverageTestSelector;
import fr.inria.diversify.dspot.selector.TestSelector;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
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
    private TestSelector testSelector;
    private InputConfiguration inputConfiguration;
    private DSpotCompiler compiler;
    private InputProgram inputProgram;
    private DiversifyClassLoader applicationClassLoader;

    public DSpot(InputConfiguration inputConfiguration) throws InvalidSdkException, Exception {
        this(inputConfiguration, 3, Arrays.asList(
                new TestDataMutator(),
                new TestMethodCallAdder(),
                new TestMethodCallRemover(),
                new StatementAdderOnAssert()),
                new BranchCoverageTestSelector(10));
    }

    public DSpot(InputConfiguration configuration, int numberOfIterations) throws InvalidSdkException, Exception {
        this(configuration, numberOfIterations, Arrays.asList(
                new TestDataMutator(),
                new TestMethodCallAdder(),
                new TestMethodCallRemover(),
                new StatementAdderOnAssert())
        );
    }

    public DSpot(InputConfiguration configuration, TestSelector testSelector) throws InvalidSdkException, Exception {
        this(configuration, 3, Arrays.asList(
                new TestDataMutator(),
                new TestMethodCallAdder(),
                new TestMethodCallRemover(),
                new StatementAdderOnAssert()), testSelector
        );
    }

    public DSpot(InputConfiguration configuration, List<Amplifier> amplifiers) throws InvalidSdkException, Exception {
        this(configuration, 3, amplifiers);
    }

    public DSpot(InputConfiguration inputConfiguration, int numberOfIterations, List<Amplifier> amplifiers) throws InvalidSdkException, Exception {
        this(inputConfiguration, numberOfIterations, amplifiers, new BranchCoverageTestSelector(10));
    }

    public DSpot(InputConfiguration inputConfiguration, int numberOfIterations, List<Amplifier> amplifiers, TestSelector testSelector) throws InvalidSdkException, Exception {
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);
        inputConfiguration.setInputProgram(inputProgram);
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

        this.inputConfiguration = inputConfiguration;
        this.amplifiers = new ArrayList<>(amplifiers);
        this.numberOfIterations = numberOfIterations;
        this.testSelector = testSelector;
        this.testSelector.init(this.inputConfiguration);
    }

    public void addAmplifier(Amplifier amplifier) {
        this.amplifiers.add(amplifier);
    }

    public List<CtType> amplifyAllTests() throws InterruptedException, IOException, ClassNotFoundException {
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
            Amplification testAmplification = new Amplification(inputProgram, this.inputConfiguration, compiler, applicationClassLoader, this.amplifiers, this.testSelector, logDir);
            CtType amplification = testAmplification.amplification(test, numberOfIterations);
            testSelector.report();
            return amplification;
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

    public DSpotCompiler getCompiler() {
        return compiler;
    }

    public InputConfiguration getInputConfiguration() {
        return inputConfiguration;
    }

}
