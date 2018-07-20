package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.amplifier.NumberLiteralAmplifier;
import eu.stamp_project.program.InputConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 17/07/18
 */
public class PerformanceTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceTest.class);

    @Ignore
    @Test
    public void testPerformance() throws Exception {


        /*
         * This test aims at measuring the time execution of multiple applications of assertion amplification.
         * This test is meant to be run manually
         */

        final CtClass testClass = Utils.findClass("fr.inria.workload.WorkloadTest");

        /*
            add some getters to increase the workload of the generation of assertion
         */

        final CtClass<?> workload = Utils.findClass("fr.inria.workload.WorkloadTest$Workload");
        final CtMethod<?> originalGetA = workload.getMethodsByName("getA").get(0);
        for (int i = 0 ; i < 20 ; i++) {
            final CtMethod<?> clone = originalGetA.clone();
            clone.setSimpleName(originalGetA.getSimpleName() + i);
            workload.addMethod(clone);
        }
        AssertionGeneratorWithTime assertGeneratorWithTime = new AssertionGeneratorWithTime(InputConfiguration.get(), Utils.getCompiler());
        final CtMethod<?> test = Utils.findMethod(testClass, "test");
        List<CtMethod<?>> allTest = Collections.singletonList(test);
        Amplifier amplifier = new NumberLiteralAmplifier();
        int iteration = 3;
        InputConfiguration.get().setMaxTestAmplified(3000);
        for (int i = 0 ; i < iteration ; i++) {
            allTest = allTest.stream().flatMap(testMethod -> amplifier.amplify(testMethod, 0)).collect(Collectors.toList());
            LOGGER.info("I-Ampl ({}) {}", i, allTest.size());
            allTest = assertGeneratorWithTime.assertionAmplification(testClass, allTest);
            LOGGER.info("AssertionRemover:");
            LOGGER.info("timeGetVariableAssertedPerTestMethod: {} ms", assertGeneratorWithTime.assertionRemover.timeGetVariableAssertedPerTestMethod);
            LOGGER.info("timeRemoveAssertionMethod: {} ms", assertGeneratorWithTime.assertionRemover.timeRemoveAssertionMethod);
            LOGGER.info("timeRemoveAssertionInvocation: {} ms", assertGeneratorWithTime.assertionRemover.timeRemoveAssertionInvocation);
            LOGGER.info("MethodAssertGenerator:");
            LOGGER.info("timeInstrumentation: {} ms", assertGeneratorWithTime.methodsAssertGenerator.timeInstrumentation);
            LOGGER.info("timeRunningInstrumentation: {} ms", assertGeneratorWithTime.methodsAssertGenerator.timeRunningInstrumentation);
            LOGGER.info("timeGeneration: {} ms", assertGeneratorWithTime.methodsAssertGenerator.timeGeneration);
            assertGeneratorWithTime.reset();
            LOGGER.info("A-Ampl ({}) {}", i, allTest.size());
        }
    }
}

