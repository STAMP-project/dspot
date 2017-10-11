package fr.inria.diversify.utils;

import fr.inria.diversify.Utils;
import fr.inria.diversify.utils.AmplificationChecker;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/04/17
 */
public class AmplificationCheckerTest {

    @Test
    public void testIsTest() throws Exception {
        Utils.init("src/test/resources/sample/sample.properties");
        CtClass<Object> classTest = Utils.getFactory().Class().get("fr.inria.helper.ClassWithInnerClass");
        final CtMethod<?> test = classTest.getMethodsByName("test").get(0);
        assertTrue(AmplificationChecker.isTest(test));
        assertTrue(AmplificationChecker.isTest(classTest.getMethodsByName("testWithDeepCallToAssertion").get(0)));
        assertTrue(AmplificationChecker.isTest(classTest.getMethodsByName("notATestBecauseTooDeepCallToAssertion").get(0)));
        assertFalse(AmplificationChecker.isTest(classTest.getMethodsByName("notATestBecauseParameters").get(0)));
//        assertFalse(AmplificationChecker.isTest(classTest.getMethodsByName("notATestBecauseMixinJunit3AndJunit4").get(0))); TODO
        assertFalse(AmplificationChecker.isTest(classTest.getMethodsByName("notATestBecauseParameters").get(0)));

        classTest = Utils.getFactory().Class().get("fr.inria.helper.ClassJunit3");
        assertTrue(AmplificationChecker.isTest(classTest.getMethodsByName("test").get(0)));
    }

}
