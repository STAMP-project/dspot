package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.RandomHelper;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/07/18
 */
public class ReturnValueAmplifierTest extends AbstractTest {

    @Test
    public void testStatementAddOnArrayObjects() throws Exception {
        final String packageName = "fr.inria.statementaddarray";
        final Factory factory = Utils.getFactory();
        RandomHelper.setSeedRandom(32L);
        ReturnValueAmplifier amplifier = new ReturnValueAmplifier();
        amplifier.reset(factory.Class().get(packageName + ".ClassTargetAmplify"));
        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTargetAmplify"), "test");
        List<CtMethod> amplifiedMethods = amplifier.amplify(ctMethod, 0).collect(Collectors.toList());
        assertEquals(1, amplifiedMethods.size());
        List<String> expectedCalledMethod = Collections.singletonList("method1");
        assertTrue(amplifiedMethods.stream()
                .allMatch(amplifiedMethod ->
                        amplifiedMethod.filterChildren(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                            @Override
                            public boolean matches(CtInvocation<?> element) {
                                return expectedCalledMethod.contains(element.getExecutable().getSimpleName());
                            }
                        }).first() != null
                ));
    }

    @Test
    public void testStatementAdd() throws Exception {

        /*
            Test the StatementAdd amplifier. It reuse existing object to add method call of accessible method.
            It can reuse return value to add method call. It results here with 7 new test cases.
         */

        final String packageName = "fr.inria.statementadd";
        final Factory factory = Utils.getFactory();
        RandomHelper.setSeedRandom(42L);
        ReturnValueAmplifier amplifier = new ReturnValueAmplifier();
        amplifier.reset(factory.Class().get(packageName + ".TestClassTargetAmplify"));
        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTargetAmplify"), "test");
        List<CtMethod> amplifiedMethods = amplifier.amplify(ctMethod, 0).collect(Collectors.toList());
        System.out.println(amplifiedMethods);
        assertEquals(1, amplifiedMethods.size());
        List<String> expectedCalledMethod = Collections.singletonList("method1");
        assertTrue(amplifiedMethods.stream()
                .allMatch(amplifiedMethod ->
                        amplifiedMethod.filterChildren(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                            @Override
                            public boolean matches(CtInvocation<?> element) {
                                return expectedCalledMethod.contains(element.getExecutable().getSimpleName());
                            }
                        }).first() != null
                ));
    }

}
