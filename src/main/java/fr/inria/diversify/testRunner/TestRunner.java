package fr.inria.diversify.testRunner;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.JunitRunner;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 05/04/16
 * Time: 10:28
 */
public class TestRunner {
    protected InputProgram inputProgram;

    protected DiversifyClassLoader applicationClassLoader;
    protected DiversityCompiler compiler;

    public TestRunner(InputProgram inputProgram, DiversifyClassLoader applicationClassLoader, DiversityCompiler compiler) {
        this.applicationClassLoader = applicationClassLoader;
        this.inputProgram = inputProgram;
        this.compiler = compiler;
    }

    public JunitResult runTest(CtType testClass, CtMethod test) throws ClassNotFoundException, IOException {
        List<CtMethod> tests = new ArrayList<>(1);
        tests.add(test);
        return runTests(testClass, tests);
    }

    public JunitResult runTests(CtType testClass, Collection<CtMethod> tests) throws ClassNotFoundException, IOException {
        boolean status = writeAndCompile(testClass);

        if(!status) {
            return null;
        }

        JunitRunner junitRunner = new JunitRunner(inputProgram, new DiversifyClassLoader(applicationClassLoader, compiler.getBinaryOutputDirectory().getAbsolutePath()));

        return junitRunner.runTestClass(testClass.getQualifiedName(), tests.stream()
                .map(test-> test.getSimpleName())
                .collect(Collectors.toList()));
    }

//    public List<CtMethod> filterTest(Collection<CtMethod> tests, JunitResult result) {
//        List<String> goodTests = result.goodTests();
//        return tests.stream()
//                .filter(test -> goodTests.contains(test.getSimpleName()))
//                .collect(Collectors.toList());
//    }

    protected boolean writeAndCompile(CtType classInstru) throws IOException {
        FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
        FileUtils.cleanDirectory(compiler.getBinaryOutputDirectory());
        try {
            PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), classInstru);
            compiler.compileFileIn(compiler.getSourceOutputDirectory(), true);
            return true;
        } catch (Exception e) {
            Log.warn("error during compilation",e);
            return false;
        }
    }


}
