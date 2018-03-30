package fr.inria.diversify.dspot.amplifier.value;

import fr.inria.Utils;
import fr.inria.AbstractTest;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/8/16
 */
public class TestValueCreator extends AbstractTest {

	@Test
	public void testCreateRandomLocalVarOfArrayListString() throws Exception {

		AmplificationHelper.setSeedRandom(72L);
		ValueCreator.count = 0;
		Factory factory = Utils.getFactory();

		final CtTypeReference<?> reference = factory.Class().get(new ArrayList<String>().getClass()).getReference();
		reference.addActualTypeArgument(factory.Type().createReference(String.class));

		assertEquals("java.util.ArrayList<java.lang.String> __DSPOT_vc_0 = new java.util.ArrayList<java.lang.String>(java.util.Collections.emptyList())",
				ValueCreator.createRandomLocalVar(reference).toString());
	}

	@Test
	public void testCreateRandomLocalVar() throws Exception {

        /*
			Test the value created randomly by the value creator.
                - one primitive
                - one array
                - one object
         */

		AmplificationHelper.setSeedRandom(23L);
		ValueCreator.count = 0;
		Factory factory = Utils.getFactory();

		int count = 0;

		CtLocalVariable randomLocalVar = ValueCreator.createRandomLocalVar(factory.Type().INTEGER_PRIMITIVE);

		assertEquals("__DSPOT_vc_" + count, randomLocalVar.getSimpleName());
		assertEquals(factory.Type().INTEGER_PRIMITIVE, randomLocalVar.getType());
		assertEquals(-1150482841, ((CtLiteral) randomLocalVar.getDefaultExpression()).getValue());

		randomLocalVar = ValueCreator.createRandomLocalVar(factory.Type().createArrayReference("int"));
		count++;

		assertEquals("__DSPOT_vc_" + count, randomLocalVar.getSimpleName());
		assertEquals(factory.Type().createArrayReference("int"), randomLocalVar.getType());

		randomLocalVar = ValueCreator.createRandomLocalVar(factory.Type().createReference("fr.inria.mutation.ClassUnderTest"));
		count++;

		assertEquals("__DSPOT_vc_" + count, randomLocalVar.getSimpleName());
		assertEquals(factory.Type().createReference("fr.inria.mutation.ClassUnderTest"), randomLocalVar.getType());
	}

	@Test
	public void testCreateNull() throws Exception {
		/*
			Test the value created randomly by the value creator.
                - one primitive
                - one array
                - one object
         */

		AmplificationHelper.setSeedRandom(23L);
		ValueCreator.count = 0;
		Factory factory = Utils.getFactory();

		int count = 0;

		CtLocalVariable randomLocalVar = ValueCreator.generateNullValue(factory.Type().INTEGER_PRIMITIVE);


		assertEquals("vc_" + count, randomLocalVar.getSimpleName());
		assertEquals(factory.Type().INTEGER_PRIMITIVE, randomLocalVar.getType());
		assertEquals("((int) (null))", randomLocalVar.getDefaultExpression().toString());

		randomLocalVar = ValueCreator.generateNullValue(factory.Type().createArrayReference("int"));
		count++;

		assertEquals("vc_" + count, randomLocalVar.getSimpleName());
		assertEquals(factory.Type().createArrayReference("int"), randomLocalVar.getType());
		assertEquals("((int[]) (null))", randomLocalVar.getDefaultExpression().toString());

		randomLocalVar = ValueCreator.generateNullValue(factory.Type().createReference("mutation.ClassUnderTest"));
		count++;

		assertEquals("vc_" + count, randomLocalVar.getSimpleName());
		assertEquals(factory.Type().createReference("mutation.ClassUnderTest"), randomLocalVar.getType());
		assertEquals("((mutation.ClassUnderTest) (null))", randomLocalVar.getDefaultExpression().toString());
	}

}
