package fr.inria.diversify.testRunner;

import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.runner.InputProgram;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/15/17
 */
public class TestRunner {

    private static List<String> blackListImplementation = new ArrayList<>();

    public static JunitResult runTests(CtType testClass,
                                       List<CtMethod<?>> tests,
                                       String classpath,
                                       InputProgram program) {
        Logger.reset();
        Logger.setLogDir(new File(program.getProgramDir() + "/log"));
        final JunitRunner junitRunner = new JunitRunner(classpath);
        final CtTypeReference reference = testClass.getReference();

        List<CtType<?>> subClasses = testClass.getFactory().Class().getAll()
                .stream()
                .filter(ctClass -> reference.equals(ctClass.getSuperclass()))
                .filter(ctClass -> !blackListImplementation.contains(ctClass.getSimpleName()))
                .collect(Collectors.toList());

        final RunWith annotation = testClass.getAnnotation(RunWith.class);
        if ( annotation != null && annotation.value().equals(Parameterized.class)) {
            return junitRunner.runTestClass(testClass.getQualifiedName(), Collections.EMPTY_LIST);
        } else if (subClasses.isEmpty()) {
            return junitRunner.runTestClass(testClass.getQualifiedName(),
                    tests.stream()
                            .map(CtNamedElement::getSimpleName)
                            .collect(Collectors.toList()));
        } else {
            List<String> fullNameSubClasses = subClasses.stream()
                    .map(CtType::getQualifiedName)
                    .collect(Collectors.toList());
            JunitResult result = junitRunner.runTestClasses(
                    fullNameSubClasses,
                    tests.stream()
                            .map(CtNamedElement::getSimpleName)
                            .collect(Collectors.toList()));
            blackListImplementation.addAll(result.getFailures().stream()
                    .filter(failure -> fullNameSubClasses.contains(failure.getDescription().getDisplayName()))
                    .map(failure -> failure.getDescription().getDisplayName())
                    .collect(Collectors.toList()));
            return result;
        }
    }
}
