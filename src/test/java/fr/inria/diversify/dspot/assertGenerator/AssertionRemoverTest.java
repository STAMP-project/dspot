package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.utils.logging.AssertionRemover;
import org.junit.Test;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;

import static org.junit.Assert.assertEquals;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/06/17
 */
public class AssertionRemoverTest extends AbstractTest {


	@Test
	public void testRemoveAssertion() throws Exception {

		/*
			Test that the AssertionRemover remove assertions from tests
		 */

		final CtClass aClass = Utils.findClass("fr.inria.assertionremover.TestClassWithAssertToBeRemoved");

		final AbstractProcessor<CtMethod> assertionRemover = new AssertionRemover();
		final Factory factory = Utils.getFactory();
		final ProcessingManager processingManager = new QueueProcessingManager(factory);
		processingManager.addProcessor(assertionRemover);
		processingManager.process(aClass);

		final String expectedMethod = "@org.junit.Test" + nl +
				"public void test1() {" + nl +
				"    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + nl +
				"    java.lang.Object o0 = cl.getTrue();" + nl +
				"    int one = 1;" + nl +
				"    switch (one) {" + nl +
				"        case 1 :" + nl +
				"            java.lang.Object o1 = fr.inria.assertionremover.TestClassWithAssertToBeRemoved.getNegation(cl.getFalse());" + nl +
				"            break;" + nl +
				"    }" + nl +
				"}";
		assertEquals(expectedMethod , aClass.getMethodsByName("test1").get(0).toString());
	}
}
