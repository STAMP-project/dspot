package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.amp.*;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.PrintClassUtils;
import org.apache.commons.io.FileUtils;
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
    protected DiversityCompiler compiler;
    protected InputConfiguration inputConfiguration;
    protected InputProgram inputProgram;
    protected DiversifyClassLoader applicationClassLoader;
    protected AssertGenerator assertGenerator;

    protected static DiversifyClassLoader regressionClassLoader;

    public DSpot(InputConfiguration inputConfiguration) throws InvalidSdkException, Exception {
        this.inputConfiguration = inputConfiguration;
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);

        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();

        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
        inputProgram.setProgramDir(outputDirectory);

        InitUtils.initDependency(inputConfiguration);
        applicationClassLoader = DSpotUtils.initClassLoader(inputProgram, inputConfiguration);
        DSpotUtils.addBranchLogger(inputProgram);
        compiler = DSpotUtils.initDiversityCompiler(inputProgram, true);
        DSpotUtils.compileTests(inputProgram, inputConfiguration.getProperty("mvnHome",null));

        assertGenerator = new AssertGenerator(inputProgram, compiler, applicationClassLoader);
        InitUtils.initLogLevel(inputConfiguration);
    }

    public DSpot(InputConfiguration inputConfiguration, DiversifyClassLoader classLoader) throws Exception, InvalidSdkException {
        this(inputConfiguration);
        regressionClassLoader = classLoader;
    }

    public CtType generateTest(String fullName) throws InterruptedException, IOException, ClassNotFoundException {
        return generateTest(inputProgram.getFactory().Type().get(fullName));
    }

    public CtType generateTest(CtType test) throws IOException, InterruptedException, ClassNotFoundException {
        Amplification testAmplification = new Amplification(inputProgram, compiler, applicationClassLoader, initAmplifiers());

        List<CtMethod> ampTests = testAmplification.amplification(test, 3);
        return assertGenerator.generateAsserts(test, ampTests, AbstractAmp.getAmpTestToParent());
    }

    public CtType generateTest(List<CtMethod> tests, CtType testClass) throws IOException, InterruptedException, ClassNotFoundException {
        Amplification testAmplification = new Amplification(inputProgram, compiler, applicationClassLoader, initAmplifiers());

        List<CtMethod> ampTests = testAmplification.amplification(testClass, tests, 3);
        return assertGenerator.generateAsserts(testClass, ampTests, AbstractAmp.getAmpTestToParent());
    }

    protected List<AbstractAmp> initAmplifiers() {
        List<AbstractAmp> amplifiers = new ArrayList<>();

        amplifiers.add(new TestDataMutator());
        amplifiers.add(new TestMethodCallAdder());
        amplifiers.add(new TestMethodCallRemover());
        amplifiers.add(new StatementAdder());

        return amplifiers;
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
        r.exec("kill "+pid);
    }

    public static void main(String[] args) throws Exception, InvalidSdkException {
        InputConfiguration inputConfiguration = new InputConfiguration(args[0]);
        String testClass = inputConfiguration.getProperty("testClass");

        DSpot dspot = new DSpot(inputConfiguration);
        CtType ampTest = dspot.generateTest(dspot.inputProgram.getFactory().Type().get(testClass));
        PrintClassUtils.printJavaFile(new File(inputConfiguration.getProperty("result")), ampTest);
        dspot.kill();
    }

}
