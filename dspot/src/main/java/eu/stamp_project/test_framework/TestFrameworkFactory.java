package eu.stamp_project.test_framework;

import eu.stamp_project.test_framework.junit.JUnit3Support;
import eu.stamp_project.test_framework.junit.JUnit4Support;
import eu.stamp_project.test_framework.junit.JUnit5Support;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.program.InputConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class TestFrameworkFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestFrameworkFactory.class);

    private static String fullQualifiedNameOfCurrentTestClass = null;

    private static TestFrameworkSupport currentTestFrameworkSupport = null;

    public static TestFrameworkSupport getCurrentTestFrameworkSupport() {
        if (currentTestFrameworkSupport == null) {
            LOGGER.warn("No test framework has been instantiated!");
            throw new UnsupportedOperationException();
        } else {
            return currentTestFrameworkSupport;
        }
    }

    /**
     * This method detects the test framework used by the project and return the corresponding {@link TestFrameworkSupport}
     *
     * @param testClass the test class for which we need to get the test framework support
     * @return an instance of the corresponding {@link TestFrameworkSupport}
     */
    public static TestFrameworkSupport getTestFrameworkSupport(CtType<?> testClass) throws IllegalArgumentException {
        if (fullQualifiedNameOfCurrentTestClass == null ||
                !fullQualifiedNameOfCurrentTestClass.equals(testClass.getQualifiedName())) {
            List<TestFrameworkSupport> testFrameworkSupportList = getTestFrameworkSupportList();
            fullQualifiedNameOfCurrentTestClass = testClass.getQualifiedName();
            final Optional<TestFrameworkSupport> frameworkSupport = testFrameworkSupportList.stream()
                    .filter(testFrameworkSupport -> testFrameworkSupport.isMyTestFramework(testClass))
                    .findFirst();
            if (frameworkSupport.isPresent()) {
                currentTestFrameworkSupport = frameworkSupport.get();
            } else {
                LOGGER.error("Could not find any test framework support for {}", testClass.getQualifiedName());
                LOGGER.error("Current supported test framework are:");
                LOGGER.error(testFrameworkSupportList.stream().map(Object::toString).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)));
                LOGGER.error("DSpot analyzes the type reference used in the given test class.");
                throw new IllegalArgumentException(String.format("Could not find any test framework support for %s", testClass.getQualifiedName()));
            }
        }
        return currentTestFrameworkSupport;
    }

    /**
     * This method return the qualified name of all test classes. We consider a class as a
     * test class if at least one {@link TestFrameworkSupport#isMyTestFramework(CtType)} matches.
     *
     * @return an array containing all the full qualified name of all test classes.
     */
    public static List<CtType<?>> getAllTestClasses() {
        List<TestFrameworkSupport> testFrameworkSupportList = getTestFrameworkSupportList();
        return InputConfiguration.get().getFactory().Class().getAll().stream()
                .filter(ctType ->
                        testFrameworkSupportList.stream()
                                .anyMatch(testFrameworkSupport ->
                                        testFrameworkSupport.isMyTestFramework(ctType)
                                )
                ).collect(Collectors.toList());
    }

    /**
     * This method calls {@link TestFrameworkFactory#getAllTestClasses()}, map classes to their name and return them as an array
     * @return an array of the qualified name of test classes.
     */
    public static String[] getAllTestClassesName() {
        return TestFrameworkFactory.getAllTestClasses()
                .stream()
                .map(CtType::getQualifiedName)
                .toArray(String[]::new);
    }

    @NotNull
    private static List<TestFrameworkSupport> getTestFrameworkSupportList() {
        return Arrays.asList(
                new JUnit3Support(),
                new JUnit4Support(),
                new JUnit5Support()
        );
    }
}
