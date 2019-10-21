package eu.stamp_project.dspot.input_ampl_distributor;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.amplifier.IterationDecoratorAmplifier;
import eu.stamp_project.dspot.amplifier.MethodAdderOnExistingObjectsAmplifier;
import eu.stamp_project.dspot.amplifier.NumberLiteralAmplifier;
import eu.stamp_project.dspot.amplifier.ReturnValueAmplifier;
import eu.stamp_project.test_framework.TestFramework;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/07/18
 */
public class SimpleInputAmplDistributorTest extends AbstractTestOnSample {

    @Test
    public void test() throws Exception {

        /*
            Test the simple input_ampl_distributor with different amplifier.
            The SimpleBudget should always provide a specific number of test Methods
         */

        final CtClass<?> testClass = findClass("fr.inria.statementadd.TestClassTargetAmplify");
        List<CtMethod<?>> ctMethods = TestFramework.getAllTest(testClass);
        final SimpleInputAmplDistributor simpleBudgetizer = new SimpleInputAmplDistributor(
                6, Arrays.asList(
                        new IterationDecoratorAmplifier(new ReturnValueAmplifier(), 3),
                        new IterationDecoratorAmplifier(new MethodAdderOnExistingObjectsAmplifier(), 2),
                        new NumberLiteralAmplifier()
                )
        );
        for (int i = 0 ; i < 3 ; i++) {
            ctMethods = simpleBudgetizer.inputAmplify(ctMethods, i); // !
        }

        assertEquals(6, ctMethods.size());
    }
}
