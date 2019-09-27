package eu.stamp_project.dspot;

import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.utils.report.output.Output;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.test_finder.TestFinder;
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

    public DSpot(TestFinder testFinder,
                 DSpotCompiler compiler,
                 TestSelector testSelector,
                 InputAmplDistributor inputAmplDistributor,
                 Output output,
                 int numberOfIterations) {
        this.testSelector = testSelector;
        this.inputAmplDistributor = inputAmplDistributor;
        this.numberOfIterations = numberOfIterations;
        this.testFinder = testFinder;
        this.compiler = compiler;
        this.output = output;
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
        }
        return amplifiedTestClasses;
    }


    private CtType<?> amplify(Amplification testAmplification,
                              CtType<?> testClassToBeAmplified,
                              List<CtMethod<?>> testMethodsToBeAmplified) {
        Counter.reset();
        if (InputConfiguration.get().shouldGenerateAmplifiedTestClass()) {
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
