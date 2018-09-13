package eu.stamp_project.utils.compilation;

import eu.stamp_project.dspot.AmplificationException;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.runner.test.TestListener;
import eu.stamp_project.utils.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/06/18
 */
public class TestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunner.class);

    public static TestListener runSubClassesForAbstractTestClass(CtType<?> testClass, List<CtMethod<?>> testsToRun, String classPath) throws AmplificationException {
        try {
            return testClass.getFactory().Type()
                    .getAll()
                    .stream()
                    .filter(ctType -> ctType.getSuperclass() != null && testClass.getReference().equals(ctType.getSuperclass()))
                    .map(CtType::getQualifiedName)
                    .map(testClassName -> {
                        try {
                            return EntryPoint.runTests(
                                    classPath + AmplificationHelper.PATH_SEPARATOR + new File("target/dspot/dependencies/").getAbsolutePath(),
                                    testClassName,
                                    testsToRun.stream()
                                            .map(CtMethod::getSimpleName)
                                            .toArray(String[]::new));
                        } catch (TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                    }).reduce(TestListener::aggregate)
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.warn("Errors during execution of {}: {}",
                    testClass.getQualifiedName(),
                    testsToRun.stream()
                            .map(CtMethod::getSimpleName)
                            .collect(Collectors.joining(","))
            );
            throw new AmplificationException(e);
        }
    }

    public static TestListener runGivenTestMethods(CtType<?> testClass, List<CtMethod<?>> testsToRun, String classPath) throws AmplificationException {
        try {
            return EntryPoint.runTests(
                    classPath + AmplificationHelper.PATH_SEPARATOR + new File("target/dspot/dependencies/").getAbsolutePath(),
                    testClass.getQualifiedName(),
                    testsToRun.stream()
                            .map(CtMethod::getSimpleName)
                            .toArray(String[]::new)
            );
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout during execution of {}: {}",
                    testClass.getQualifiedName(),
                    testsToRun.stream()
                            .map(CtMethod::getSimpleName)
                            .collect(Collectors.joining(","))
            );
            throw new AmplificationException(e);
        }
    }

    public static TestListener run(String classpath, String rootPath, String fullQualifiedName, String... testToRun) throws TimeoutException {
        if (InputConfiguration.get().isUseMavenToExecuteTest()) {
            return eu.stamp_project.testrunner.maven.EntryPoint.runTests(
                    rootPath,
                    fullQualifiedName,
                    testToRun
            );
        } else {
            return EntryPoint.runTests(
                    classpath,
                    fullQualifiedName,
                    testToRun

            );
        }
    }

}
