package fr.inria.diversify.utils;

import fr.inria.AbstractTest;
import fr.inria.Utils;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/04/17
 */
public class AmplificationCheckerTest extends AbstractTest {

    @Test
    public void testIsTest() throws Exception {
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


    @Test
    public void testIsAssert() throws Exception {
        /*
			isAssert method should be match all the kind of assertions:
				For now, the supported types are:
					assert*** (from org.junit)
					assertThat (from google.truth)

			see src/test/resources/sample/src/test/java/fr/inria/helper/TestWithMultipleAsserts.java
			Also, the isAssert method will math invocation on methods that contain assertions
		 */

        CtClass<?> classTest = Utils.getFactory().Class().get("fr.inria.helper.TestWithMultipleAsserts");
        final List<CtInvocation> invocations = classTest.getMethodsByName("test")
                .get(0)
                .getElements(new TypeFilter<>(CtInvocation.class));
        final List<CtInvocation> collect = invocations.stream().filter(AmplificationChecker::isAssert).collect(Collectors.toList());
        assertEquals(11, collect.size());
    }

}
