package eu.stamp_project.dspot;

import eu.stamp_project.Main;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.TestSelector;
<<<<<<< HEAD
=======
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.options.BudgetizerEnum;
import eu.stamp_project.utils.options.InputConfiguration;
>>>>>>> refactor: put everything in InputConfiguration and move it to program subpackage
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.report.output.Output;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.compilation.DSpotCompiler;
<<<<<<< HEAD
import eu.stamp_project.utils.test_finder.TestFinder;
=======
import eu.stamp_project.utils.json.ClassTimeJSON;
import eu.stamp_project.utils.json.ProjectTimeJSON;
>>>>>>> refactor: put everything in InputConfiguration and move it to program subpackage
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.*;

/**
 * User: Simon
 * Date: 08/06/15
 * Time: 17:36
 */
public class DSpot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSpot.class);

    private TestFinder testFinder;

    private DSpotCompiler compiler;

    private TestSelector testSelector;

    private InputAmplDistributor inputAmplDistributor;

    private Output output;

    private int numberOfIterations;

    private boolean shouldGenerateAmplifiedTestClass;

    public DSpot(TestFinder testFinder,
                 DSpotCompiler compiler,
                 TestSelector testSelector,
                 InputAmplDistributor inputAmplDistributor,
                 Output output,
                 int numberOfIterations,
                 boolean shouldGenerateAmplifiedTestClass) {
        this.testSelector = testSelector;
        this.inputAmplDistributor = inputAmplDistributor;
        this.numberOfIterations = numberOfIterations;
        this.testFinder = testFinder;
        this.compiler = compiler;
        this.output = output;
        this.shouldGenerateAmplifiedTestClass = shouldGenerateAmplifiedTestClass;
    }

    public CtType<?> amplify(CtType<?> testClassToBeAmplified) {
        return this.amplify(Collections.singletonList(testClassToBeAmplified), Collections.emptyList()).get(0);
    }

    public CtType<?> amplify(CtType<?> testClassToBeAmplified, String testMethodToBeAmplifiedAsString) {
        return this.amplify(Collections.singletonList(testClassToBeAmplified), Collections.singletonList(testMethodToBeAmplifiedAsString)).get(0);
    }

    public CtType<?> amplify(CtType<?> testClassToBeAmplified, List<String> testMethodsToBeAmplifiedAsString) {
        return this.amplify(Collections.singletonList(testClassToBeAmplified), testMethodsToBeAmplifiedAsString).get(0);
    }

    public List<CtType<?>> amplify(List<CtType<?>> testClassesToBeAmplified, String testMethodToBeAmplifiedAsString) {
        return this.amplify(testClassesToBeAmplified, Collections.singletonList(testMethodToBeAmplifiedAsString));
    }

    public List<CtType<?>> amplify(List<CtType<?>> testClassesToBeAmplified) {
        return this.amplify(testClassesToBeAmplified, Collections.emptyList());
    }

<<<<<<< HEAD
    public List<CtType<?>> amplify(List<CtType<?>> testClassesToBeAmplified, List<String> testMethodsToBeAmplifiedAsString) {
        final List<CtType<?>> amplifiedTestClasses = new ArrayList<>();
        for (CtType<?> testClassToBeAmplified : testClassesToBeAmplified) {
            inputAmplDistributor.resetAmplifiers(testClassToBeAmplified);
            Amplification testAmplification = new Amplification(
                    this.compiler,
                    this.testSelector,
                    this.inputAmplDistributor,
                    this.numberOfIterations
            );
            final List<CtMethod<?>> testMethodsToBeAmplified =
                    testFinder.findTestMethods(testClassToBeAmplified, testMethodsToBeAmplifiedAsString);
            final CtType<?> amplifiedTestClass = this.amplify(testAmplification, testClassToBeAmplified, testMethodsToBeAmplified);
            amplifiedTestClasses.add(amplifiedTestClass);
            cleanAfterAmplificationOfOneTestClass(compiler, testClassToBeAmplified);
=======
    /**
     * Amplify the given test methods of the given test classes.
     *
     * @param testClassesToBeAmplified the list of test classes to be amplified. This list can be a list of java regex.
     * @param testMethods              the list of test methods to be amplified. This list can be a list of java regex.
     * @return a list of amplified test classes with amplified test methods.
     */
    public List<CtType<?>> amplifyTestClassesTestMethods(List<String> testClassesToBeAmplified, List<String> testMethods) {
        final Map<String, List<CtType<?>>> collect = testClassesToBeAmplified.stream()
                .collect(Collectors.toMap(
                            Function.identity(),
                            testClassName -> this.findTestClasses(testClassName).collect(Collectors.toList())
                        )
                );
        final List<String> keys = collect.keySet()
                .stream()
                .filter(testClassName -> {
                    if (collect.get(testClassName).isEmpty()) {
//                        Main.GLOBAL_REPORT.addError(
//                                new Error(ErrorEnum.ERROR_NO_TEST_COULD_BE_FOUND_MATCHING_REGEX,
//                                        String.format("Your input:%n\t%s", testClassName)
//                                )
//                        );
                        return false;
                    } else {
                        return true;
                    }
                }).collect(Collectors.toList());
        if (keys.isEmpty()) {
            LOGGER.error("Could not find any test classes to be amplified.");
            LOGGER.error("No one of the provided target test classes could find candidate:");
            final String testClassToBeAmplifiedJoined = String.join(AmplificationHelper.LINE_SEPARATOR, testClassesToBeAmplified);
            LOGGER.error("\t{}", testClassToBeAmplifiedJoined);
            LOGGER.error("DSpot will stop here, please checkEnum your inputs.");
            LOGGER.error("In particular, you should look at the values of following options:");
            LOGGER.error("\t (-t | --test) should be followed by correct Java regular expression.");
            LOGGER.error("\t Please, refer to the Java documentation of java.util.regex.Pattern.");
            LOGGER.error("\t (-c | --cases) should be followed by correct method name,");
            LOGGER.error("\t that are contained in the test classes that match the previous option, i.e. (-t | --test).");
//            Main.GLOBAL_REPORT.addError(
//                    new Error(ErrorEnum.ERROR_NO_TEST_COULD_BE_FOUND,
//                            String.format("Your input:%n\t%s", testClassToBeAmplifiedJoined)
//                    )
//            );
            return Collections.emptyList();
>>>>>>> refactor: put everything in InputConfiguration and move it to program subpackage
        }
        return amplifiedTestClasses;
    }


    private CtType<?> amplify(Amplification testAmplification,
                              CtType<?> testClassToBeAmplified,
                              List<CtMethod<?>> testMethodsToBeAmplified) {
        Counter.reset();
        if (this.shouldGenerateAmplifiedTestClass) {
            testClassToBeAmplified = AmplificationHelper.renameTestClassUnderAmplification(testClassToBeAmplified);
        }
        long time = System.currentTimeMillis();

        // Amplification of the given test methods of the given test class
        final List<CtMethod<?>> amplifiedTestMethods =
                testAmplification.amplification(testClassToBeAmplified, testMethodsToBeAmplified);

        final long elapsedTime = System.currentTimeMillis() - time;
        LOGGER.info("elapsedTime {}", elapsedTime);
        this.output.addClassTimeJSON(testClassToBeAmplified.getQualifiedName(), elapsedTime);

        //Optimization: this object is not required anymore
        //and holds a dictionary with large number of cloned CtMethods.
        testAmplification = null;
        //but it is clear before iterating again for next test class
        LOGGER.debug("OPTIMIZATION: GC invoked");
        System.gc(); //Optimization: cleaning up heap before printing the amplified class

        InputConfiguration.get().getBuilder().reset();
        try {
            Main.GLOBAL_REPORT.addTestSelectorReportForTestClass(testClassToBeAmplified, this.testSelector.report());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Something bad happened during the report fot test-criterion.");
            LOGGER.error("Dspot might not have output correctly!");
        }

        return this.output.output(testClassToBeAmplified, amplifiedTestMethods);
    }

    private static void cleanAfterAmplificationOfOneTestClass(DSpotCompiler compiler, CtType<?> testClassToBeAmplified) {
        /* Cleaning modified source directory by DSpot */
        try {
            FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
        } catch (Exception exception) {
            exception.printStackTrace();
            LOGGER.warn("Something went wrong when trying to cleaning temporary sources directory: {}", compiler.getSourceOutputDirectory());
        }
        /* Cleaning binary generated by Dspot */
        try {
            String pathToDotClass = compiler.getBinaryOutputDirectory().getAbsolutePath() + "/" +
                    testClassToBeAmplified.getQualifiedName().replaceAll("\\.", "/") + ".class";
            FileUtils.forceDelete(new File(pathToDotClass));
        } catch (IOException ignored) {
            //ignored
        }
    }
}
