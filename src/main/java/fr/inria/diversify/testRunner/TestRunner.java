package fr.inria.diversify.testRunner;

import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

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
        final CtTypeReference reference = testClass.getReference();
        List<CtType<?>> subClasses = testClass.getFactory().Class().getAll()
                .stream()
                .filter(ctClass -> reference.equals(ctClass.getSuperclass()))
                .collect(Collectors.toList());
        if (subClasses.isEmpty()) {
            return run(testClass, tests, classpath);
        } else {
            return subClasses.stream()
                    .reduce(new JunitResult(),
                            (acc, current) -> acc.add(run(current, tests, classpath)),
                            JunitResult::add);
        }
    }

    private static JunitResult run(CtType testClass, List<CtMethod<?>> tests, String classpath) {
        final JunitRunner junitRunner = new JunitRunner(classpath);
        return junitRunner.runTestClass(testClass.getQualifiedName(),
                tests.stream()
                        .filter(ctMethod -> testClass.equals(ctMethod.getDeclaringType()))
                        .map(CtNamedElement::getSimpleName)
                        .collect(Collectors.toList()));
    }


}
