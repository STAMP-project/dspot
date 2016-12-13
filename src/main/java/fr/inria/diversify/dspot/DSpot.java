package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.amp.*;
import fr.inria.diversify.dspot.assertGenerator.AssertGenerator;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * User: Simon
 * Date: 08/06/15
 * Time: 17:36
 */
public class DSpot {

    private List<Amplifier> amplifiers;
    private int numberOfIterations;
    private DiversityCompiler compiler;
    private InputProgram inputProgram;
    private DiversifyClassLoader applicationClassLoader;
    private AssertGenerator assertGenerator;

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
        compiler = DSpotUtils.initDiversityCompiler(inputProgram, true);
        DSpotUtils.compileTests(inputProgram, mavenHome, mavenLocalRepository);

        assertGenerator = new AssertGenerator(inputProgram, compiler, applicationClassLoader);
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
        Launcher launcher = new Launcher();
        launcher.addInputResource(this.inputProgram.getAbsoluteTestSourceCodeDir());
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        final List<CtType> amplifiedClassTest = new ArrayList<>();
        launcher.getFactory().Class().getAll().forEach(classTest -> {
                    try {
                        amplifiedClassTest.add(amplifyTest(classTest.getQualifiedName()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        return amplifiedClassTest;
    }

    public CtType amplifyTest(String fullName) throws InterruptedException, IOException, ClassNotFoundException {
        return amplifyTest(inputProgram.getFactory().Type().get(fullName));
    }

    public CtType amplifyTest(CtType test) throws IOException, InterruptedException, ClassNotFoundException {
        File logDir = new File(inputProgram.getProgramDir() + "/log");
        Amplification testAmplification = new Amplification(inputProgram, compiler, applicationClassLoader, this.amplifiers, logDir);
        List<CtMethod> ampTests = testAmplification.amplification(test, numberOfIterations);
        return assertGenerator.generateAsserts(test, ampTests, AmplificationHelper.getAmpTestToParent());
    }

    public CtType amplifyTest(List<CtMethod> tests, CtType testClass) throws IOException, InterruptedException, ClassNotFoundException {
        File logDir = new File(inputProgram.getProgramDir() + "/log");
        Amplification testAmplification = new Amplification(inputProgram, compiler, applicationClassLoader, this.amplifiers, logDir);
        List<CtMethod> ampTests = testAmplification.amplification(testClass, tests, numberOfIterations);
        return assertGenerator.generateAsserts(testClass, ampTests, AmplificationHelper.getAmpTestToParent());
    }

    public void clean() throws IOException {
        FileUtils.forceDelete(compiler.getSourceOutputDirectory());
        FileUtils.forceDelete(compiler.getBinaryOutputDirectory());
        FileUtils.forceDelete(new File(inputProgram.getProgramDir()));
    }

    public InputProgram getInputProgram() {
        return inputProgram;
    }

    protected static void kill() throws IOException {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        Runtime r = Runtime.getRuntime();
        r.exec("kill " + pid);
    }

    public static void main(String[] args) throws Exception, InvalidSdkException {
        InputConfiguration inputConfiguration = new InputConfiguration(args[0]);
        String testClass = inputConfiguration.getProperty("testClass");

        DSpot dspot = new DSpot(inputConfiguration);
        CtType ampTest = dspot.amplifyTest(dspot.inputProgram.getFactory().Type().get(testClass));
        PrintClassUtils.printJavaFile(new File(inputConfiguration.getProperty("result")), ampTest);
        dspot.kill();
    }

}
