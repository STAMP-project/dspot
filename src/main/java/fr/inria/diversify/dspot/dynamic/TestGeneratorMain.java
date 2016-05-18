package fr.inria.diversify.dspot.dynamic;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AssertGenerator;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.testRunner.TestRunner;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.PrintClassUtils;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * User: Simon
 * Date: 24/03/16
 * Time: 15:40
 */
public class TestGeneratorMain {
    protected final InputProgram inputProgram;
    protected DiversityCompiler compiler;
    DiversifyClassLoader applicationClassLoader;
    File resultDir;

    public TestGeneratorMain(InputConfiguration inputConfiguration) throws InvalidSdkException, Exception {
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);

        resultDir = new File(inputConfiguration.getProperty("result"));

        InitUtils.initDependency(inputConfiguration);
        InitUtils.addApplicationClassesToClassPath(inputProgram);
        compiler = DSpotUtils.initDiversityCompiler(inputProgram, false);

        applicationClassLoader = DSpotUtils.initClassLoader(inputProgram, inputConfiguration);
    }

    public void testGenerator(String logFile) throws IOException {
        TestRunner testRunner = new TestRunner(inputProgram, applicationClassLoader, compiler);

        TestGenerator testGenerator = new TestGenerator(inputProgram.getFactory(), testRunner, new AssertGenerator(inputProgram, compiler, applicationClassLoader));
        Collection<CtType> testClasses = testGenerator.generateTestClasses(logFile);

        if(!resultDir.exists()) {
            resultDir.mkdirs();
        }
        for(CtType test : testClasses) {
            PrintClassUtils.printJavaFile(resultDir, test);
        }
    }

    public void clean() throws IOException {
        FileUtils.forceDelete(compiler.getSourceOutputDirectory());
        FileUtils.forceDelete(compiler.getBinaryOutputDirectory());
    }

    protected static void kill() throws IOException {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        Runtime r = Runtime.getRuntime();
        r.exec("kill "+pid);
    }

    public static void main(String[] args) throws Exception, InvalidSdkException {
        InputConfiguration inputConfiguration = new InputConfiguration(args[0]);
        TestGeneratorMain testGeneratorMain = new TestGeneratorMain(inputConfiguration);
        testGeneratorMain.testGenerator(args[1]);
        testGeneratorMain.clean();
        kill();
    }
}
