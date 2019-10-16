package eu.stamp_project.utils.execution;

import eu.stamp_project.Main;
import eu.stamp_project.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.dspot.AmplificationException;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;

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
 *
 * This class is a proxy for EntryPoint.
 *
 */
public class TestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunner.class);

    private String absolutePathToProjectRoot;

    private String preGoals;

    private boolean shouldUseMavenToExecuteTest;

    public TestRunner(String absolutePathToProjectRoot, String preGoals, boolean shouldUseMavenToExecuteTest) {
        this.absolutePathToProjectRoot = absolutePathToProjectRoot;
        this.preGoals = preGoals;
        this.shouldUseMavenToExecuteTest = shouldUseMavenToExecuteTest;
        EntryPoint.verbose = Main.verbose;
    }

    public TestResult runSubClassesForAbstractTestClass(CtType<?> testClass, List<CtMethod<?>> testsToRun, String classPath) throws AmplificationException {
        try {
            return testClass.getFactory().Type()
                    .getAll()
                    .stream()
                    .filter(ctType -> ctType.getSuperclass() != null && testClass.getReference().equals(ctType.getSuperclass()))
                    .map(CtType::getQualifiedName)
                    .map(testClassName -> {
                        try {
                            return EntryPoint.runTests(
                                    classPath + AmplificationHelper.PATH_SEPARATOR + DSpotUtils.getAbsolutePathToDSpotDependencies(),
                                    testClassName,
                                    testsToRun.stream()
                                            .map(CtMethod::getSimpleName)
                                            .toArray(String[]::new));
                        } catch (TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                    }).reduce(TestResult::aggregate)
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

    public TestResult runGivenTestMethods(CtType<?> testClass, List<CtMethod<?>> testsToRun, String classPath) throws AmplificationException {
        try {
            return this.run(classPath + AmplificationHelper.PATH_SEPARATOR + DSpotUtils.getAbsolutePathToDSpotDependencies(),
                    absolutePathToProjectRoot,
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

    public TestResult run(String classpath, String rootPath, String fullQualifiedName, String... testToRun) throws TimeoutException {
        if (shouldUseMavenToExecuteTest) {
            EntryPoint.workingDirectory = new File(rootPath);
            eu.stamp_project.testrunner.maven.EntryPoint.preGoals = preGoals;
            return eu.stamp_project.testrunner.maven.EntryPoint.runTestsSpecificPom(
                    rootPath,
                    fullQualifiedName,
                    DSpotPOMCreator.getPOMName(),
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
