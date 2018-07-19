package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/07/18
 */
public class PerformanceTest extends AbstractTest {

    @Ignore
    @Test
    public void test() throws Exception {

        /*
         * This test aims at measuring the time execution of multiple applications of Amplifiers.
         * This test is meant to be run manually
         */

        final int numberOfIteration = 3;

        final CtClass testClass = Utils.findClass("fr.inria.ampl.ToBeAmplifiedLiteralTest");
        final CtMethod originalTest = Utils.findMethod(testClass, "testInt");
        List<CtMethod> amplifiedTestMethod1 = Collections.singletonList(originalTest);
        List<CtMethod> amplifiedTestMethod2 = Collections.singletonList(originalTest);

        Amplifier testDataMutator = new TestDataMutator();
        testDataMutator.reset(testClass);

        Amplifier allLiteralAmplifiers = new AllLiteralAmplifiers();
        allLiteralAmplifiers.reset(testClass);

        long start = System.currentTimeMillis();
        for (int i = 0 ; i < numberOfIteration ; i ++) {
            amplifiedTestMethod1 = amplifiedTestMethod1.stream().flatMap(testMethod -> allLiteralAmplifiers.amplify(testMethod, 0)).collect(Collectors.toList());
            System.out.println("("+ i +")Number of Amplification:" + amplifiedTestMethod1.size());
        }
        final long timeAllLiteral = System.currentTimeMillis() - start;
        System.out.println(timeAllLiteral + "ms");

        long start2 = System.currentTimeMillis();
        for (int i = 0 ; i < numberOfIteration ; i ++) {
            amplifiedTestMethod2 = amplifiedTestMethod2.stream().flatMap(testMethod -> testDataMutator.amplify(testMethod, 0)).collect(Collectors.toList());
            System.out.println("("+ i +")Number of Amplification:" + amplifiedTestMethod2.size());
        }
        final long timeTestDataMutator = System.currentTimeMillis() - start2;
        System.out.println(timeTestDataMutator + "ms");

        assertTrue(timeTestDataMutator > timeAllLiteral);
        assertTrue(amplifiedTestMethod2.size() > amplifiedTestMethod1.size());
    }
}
