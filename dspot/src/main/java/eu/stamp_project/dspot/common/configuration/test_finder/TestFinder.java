package eu.stamp_project.dspot.common.configuration.test_finder;

import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.testrunner.test_framework.TestFramework;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.report.error.Error;
import eu.stamp_project.dspot.common.report.error.ErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/09/19
 */
public class TestFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestFinder.class);

    private List<String> excludedTestClasses;

    private List<String> excludedTestCases;

    public TestFinder(List<String> excludedClasses, List<String> excludedTestCases) {
        this.excludedTestClasses = excludedClasses;
        this.excludedTestCases = excludedTestCases;
    }

    /**
     * Predicate that returns either the given ctType should be excluded or not.
     */
    public final Predicate<CtType> isNotExcluded = ctType ->
            this.excludedTestClasses.isEmpty() ||
                    this.excludedTestClasses.stream()
                            .map(Pattern::compile)
                            .map(pattern -> pattern.matcher(ctType.getQualifiedName()))
                            .noneMatch(Matcher::matches);

    public List<CtType<?>> findTestClasses(List<String> testClassNames) {
        if (testClassNames.isEmpty() || "all".equals(testClassNames.get(0))) {
            return TestFramework.getAllTestClasses();
        }
        final Map<String, List<CtType<?>>> namesMatchedToTypes =
                testClassNames.stream().collect(Collectors.toMap(
                            Function.identity(),
                            testClassName -> this.findTestClasses(testClassName).collect(Collectors.toList())
                        )
                );
        if (checkIfNoneTestClassMatched(testClassNames, namesMatchedToTypes)) {
            return Collections.emptyList();
        }
        final List<CtType<?>> matchingTypesWithoutIgnore = namesMatchedToTypes.values().stream()
                .flatMap(Collection::stream)
                .filter(ctType -> {
                            boolean isIgnored = TestFramework.get().isIgnored(ctType);
                            if (isIgnored) {
                                LOGGER.info("Skipping test suite {} since it is annotated as ignored/disabled", ctType.getSimpleName());
                            }
                            return !isIgnored;
                        }
                ).collect(Collectors.toList());
        if (matchingTypesWithoutIgnore.isEmpty()) {
            LOGGER.warn("All the matching test classes are ignored/disabled.");
        }
        return matchingTypesWithoutIgnore;
    }

    public List<CtMethod<?>> findTestMethods(CtType<?> testClass, List<String> testMethodNames) {
        if (testMethodNames.isEmpty()) {
            return testClass.getMethods()
                    .stream()
                    .filter(TestFramework.get()::isTest)
                    .collect(Collectors.toList());
        } else {
            return testMethodNames.stream()
                    .flatMap(pattern ->
                        testClass.getMethods().stream()
                               .filter(ctMethod -> Pattern.compile(pattern).matcher(ctMethod.getSimpleName()).matches())
                               .collect(Collectors.toList()).stream()
                    .filter(testMethod -> !this.excludedTestCases.contains(testMethod.getSimpleName()))
            ).collect(Collectors.toList());
        }
    }

    private boolean checkIfNoneTestClassMatched(List<String> testClassNames,
                                                Map<String, List<CtType<?>>> collect) {
        final List<String> keys = collect.keySet()
                .stream()
                .filter(testClassName -> {
                    if (collect.get(testClassName).isEmpty()) {
                        DSpotState.GLOBAL_REPORT.addError(
                                new Error(ErrorEnum.ERROR_NO_TEST_COULD_BE_FOUND_MATCHING_REGEX,
                                        String.format("Your input:%n\t%s", testClassName)
                                )
                        );
                        return false;
                    } else {
                        return true;
                    }
                }).collect(Collectors.toList());
        if (keys.isEmpty()) {
            LOGGER.error("Could not find any test classes to be amplified.");
            LOGGER.error("No one of the provided target test classes could find candidate:");
            final String testClassToBeAmplifiedJoined = String.join(AmplificationHelper.LINE_SEPARATOR, testClassNames);
            LOGGER.error("\t{}", testClassToBeAmplifiedJoined);
            LOGGER.error("DSpot will stop here, please checkEnum your inputs.");
            LOGGER.error("In particular, you should look at the values of following options:");
            LOGGER.error("\t (-t | --test) should be followed by correct Java regular expression.");
            LOGGER.error("\t Please, refer to the Java documentation of java.util.regex.Pattern.");
            LOGGER.error("\t (-c | --cases) should be followed by correct method name,");
            LOGGER.error("\t that are contained in the test classes that match the previous option, i.e. (-t | --test).");
            DSpotState.GLOBAL_REPORT.addError(
                    new Error(ErrorEnum.ERROR_NO_TEST_COULD_BE_FOUND,
                            String.format("Your input:%n\t%s", testClassToBeAmplifiedJoined)
                    )
            );
            return true;
        }
        return false;
    }


    private Stream<CtType<?>> findTestClasses(String targetTestClasses) {
        if (!targetTestClasses.contains("\\")) {
            // here, we make more usable, but maybe less reliable, dspot.
            // we replace every * with .*, since in java.util.regex Pattern class
            // the star (*) is just a quantifier (0, or infini) and the dot (.) is a wildcard
            targetTestClasses = targetTestClasses.replaceAll("\\.", "\\\\\\.").replaceAll("\\*", ".*");
        }
        Pattern pattern = Pattern.compile(targetTestClasses);
        return TestFramework.getAllTestClassesAsStream()
                .filter(ctType -> pattern.matcher(ctType.getQualifiedName()).matches())
                .filter(this.isNotExcluded);
    }

    public static TestFinder get() {
        return new TestFinder(Collections.emptyList(), Collections.emptyList());
    }

    public static TestFinder get(UserInput configuration) {
        return new TestFinder(
                Arrays.stream(configuration.getExcludedClasses().split(",")).collect(Collectors.toList()),
                Arrays.stream(configuration.getExcludedTestCases().split(",")).collect(Collectors.toList())
        );
    }
}
