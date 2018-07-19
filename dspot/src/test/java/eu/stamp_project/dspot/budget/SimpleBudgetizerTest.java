package eu.stamp_project.dspot.budget;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.amplifier.IterationDecoratorAmplifier;
import eu.stamp_project.dspot.amplifier.MethodGeneratorAmplifier;
import eu.stamp_project.dspot.amplifier.NumberLiteralAmplifier;
import eu.stamp_project.dspot.amplifier.ReturnValueAmplifier;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/07/18
 */
public class SimpleBudgetizerTest extends AbstractTest {

    @Test
    public void test() throws Exception {

        /*
            Test the simple budget with different amplifier.
            The SimpleBudget should always provide a specific number of test Methods
         */

        InputConfiguration.get().setAmplifiers(
                Arrays.asList(
                        new IterationDecoratorAmplifier(new ReturnValueAmplifier(), 3),
                        new IterationDecoratorAmplifier(new MethodGeneratorAmplifier(), 2),
                        new NumberLiteralAmplifier()
                )
        );
        final CtClass<?> testClass = Utils.findClass("fr.inria.statementadd.TestClassTargetAmplify");
        List<CtMethod<?>> ctMethods = AmplificationHelper.getAllTest(testClass);
        final SimpleBudgetizer simpleBudgetizer = new SimpleBudgetizer(6);
        for (int i = 0 ; i < 7 ; i++) {
            ctMethods = simpleBudgetizer.inputAmplify(ctMethods, i); // !
        }
        verifyCount(2, ctMethods, "null");
        verifyCount(2, ctMethods, "mg");
    }

    private void verifyCount(int count, List<CtMethod<?>> ctMethods, String suffix) {
        assertTrue(count <= ctMethods.stream()
                .filter(ctMethod ->
                        ctMethod.getSimpleName()
                                .replaceAll("[0-9]*", "")
                                .endsWith(suffix)
                ).count()
        );
    }
}
