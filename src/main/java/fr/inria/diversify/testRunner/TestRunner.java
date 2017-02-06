package fr.inria.diversify.testRunner;

import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/15/17
 */
public class TestRunner {

    public static JunitResult runTests(CtType testClass,
                                       List<CtMethod<?>> tests,
                                       String classpath,
                                       InputProgram program) {
        Logger.reset();
        Logger.setLogDir(new File(program.getProgramDir() + "/log"));
        JunitRunner junitRunner = new JunitRunner(classpath);
        return junitRunner.runTestClass(testClass.getQualifiedName(), tests.stream()
                .map(CtNamedElement::getSimpleName)
                .collect(Collectors.toList()));
    }
}
