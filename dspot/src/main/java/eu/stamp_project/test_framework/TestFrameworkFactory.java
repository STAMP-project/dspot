package eu.stamp_project.test_framework;

import eu.stamp_project.test_framework.junit.JUnit3Support;
import eu.stamp_project.test_framework.junit.JUnit4Support;
import eu.stamp_project.test_framework.junit.JUnit5Support;
import eu.stamp_project.utils.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtClass;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class TestFrameworkFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestFrameworkFactory.class);

    /**
     * This method detects the test framework used by the project and return the corresponding {@link TestFrameworkSupport}
     * @param testClass the test class for which we need to get the test framework support
     * @return an instance of the corresponding {@link TestFrameworkSupport}
     */
    public static TestFrameworkSupport getTestFrameworkSupport(CtClass<?> testClass) throws Throwable {
        List<TestFrameworkSupport> testFrameworkSupportList = Arrays.asList(
                new JUnit3Support(),
                new JUnit4Support(),
                new JUnit5Support()
        );
        return testFrameworkSupportList.stream()
                .filter(testFrameworkSupport -> testFrameworkSupport.isMyTestFramework(testClass))
                .findFirst()
                .orElseThrow(
                        () -> {
                            LOGGER.error("Could not find any test framework support for {}", testClass.getQualifiedName());
                            LOGGER.error("Current supported test framework are:");
                            LOGGER.error(testFrameworkSupportList.stream().map(Object::toString).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)));
                            LOGGER.error("DSpot analyzes the type reference used in the given test class.");
                            throw new IllegalArgumentException(String.format("Could not find any test framework support for %s", testClass.getQualifiedName()));
                        }
                );
    }
}
