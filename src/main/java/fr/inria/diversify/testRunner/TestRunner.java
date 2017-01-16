package fr.inria.diversify.testRunner;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.support.DSpotClassLoader;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/15/17
 */
public class TestRunner {

    public static JunitResult runTests(DiversifyClassLoader applicationClassLoader, DSpotCompiler compiler,
                                       String pathToLogDir, String programDir, CtType testClass,
                                       Collection<CtMethod> tests) throws ClassNotFoundException {
        DiversifyClassLoader classLoader = new DiversifyClassLoader(applicationClassLoader,
                compiler.getBinaryOutputDirectory().getAbsolutePath());
        return run(classLoader, pathToLogDir, programDir, testClass, tests);
    }

    public static JunitResult runTests(DiversifyClassLoader applicationClassLoader, DSpotCompiler compiler,
                                       String pathToLogDir, String programDir, CtType testClass,
                                       Collection<CtMethod> tests,
                                       Collection<String> filters,
                                       InputProgram inputProgram) throws ClassNotFoundException {
        DSpotClassLoader classLoader = new DSpotClassLoader(applicationClassLoader,
                Arrays.asList(compiler.getBinaryOutputDirectory().getAbsolutePath(),
                        inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir()));
        classLoader.setClassFilter(filters);
        return run(classLoader, pathToLogDir, programDir, testClass, tests);
    }

    private static JunitResult run(DiversifyClassLoader applicationClassLoader,
                                   String pathToLogDir, String programDir, CtType testClass,
                                   Collection<CtMethod> tests) {
        JunitRunner junitRunner = new JunitRunner(applicationClassLoader);
        Logger.reset();
        Logger.setLogDir(new File(pathToLogDir));
        String currentUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", programDir);
        JunitResult result = junitRunner.runTestClass(testClass.getQualifiedName(), tests.stream()
                .map(test -> test.getSimpleName())
                .collect(Collectors.toList()));
        System.setProperty("user.dir", currentUserDir);
        return result;
    }
}
