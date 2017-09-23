package fr.inria.diversify.dspot.amplifier.value;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.utils.AmplificationHelper;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collections;

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

		final CtTypeReference<ArrayList> reference = factory.Type().createReference(ArrayList.class);
		reference.setActualTypeArguments(Collections.singletonList(factory.Type().createReference(String.class)));

		assertEquals("java.util.ArrayList<java.lang.String> vc_0 = " +
						"new java.util.ArrayList<java.lang.String>(" +
						"java.util.Collections.singletonList(\"mEp_A=f&o(QqZ.%_r;?r\")" +
						")",
				ValueCreator.createRandomLocalVar(reference).toString());
		assertEquals("java.util.ArrayList<java.lang.String> vc_1 = new java.util.ArrayList<java.lang.String>()",
				ValueCreator.createRandomLocalVar(reference).toString());
		assertEquals("java.util.ArrayList<java.lang.String> vc_2 = new java.util.ArrayList<java.lang.String>(-1016235803)",
				ValueCreator.createRandomLocalVar(reference).toString());
		assertEquals("java.util.ArrayList<java.lang.String> vc_3 = new java.util.ArrayList<java.lang.String>(java.util.Collections.emptyList())",
				ValueCreator.createRandomLocalVar(reference).toString());
		assertEquals("java.util.ArrayList<java.lang.String> vc_4 = new java.util.ArrayList<java.lang.String>(java.util.Collections.emptyList())",
				ValueCreator.createRandomLocalVar(reference).toString());
		assertEquals("java.util.ArrayList<java.lang.String> vc_5 = new java.util.ArrayList<java.lang.String>(java.util.Collections.singletonList(\"`)}.clE+=T zM>*v:Zvh\"))",
				ValueCreator.createRandomLocalVar(reference).toString());
		assertEquals("java.util.ArrayList<java.lang.String> vc_6 = new java.util.ArrayList<java.lang.String>(1240296032)",
				ValueCreator.createRandomLocalVar(reference).toString());
		assertEquals("java.util.ArrayList<java.lang.String> vc_7 = new java.util.ArrayList<java.lang.String>()",
				ValueCreator.createRandomLocalVar(reference).toString());
		assertEquals("java.util.ArrayList<java.lang.String> vc_8 = new java.util.ArrayList<java.lang.String>(java.util.Collections.singletonList(\"k?IJ8_ubM+qAn$Xwf>7M\"))",
				ValueCreator.createRandomLocalVar(reference).toString());
	}

	@Test
	public void testCreateRandomLocalVar() throws Exception, InvalidSdkException {

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

		assertEquals("vc_" + count, randomLocalVar.getSimpleName());
		assertEquals(factory.Type().INTEGER_PRIMITIVE, randomLocalVar.getType());
		assertEquals(-1150482841, ((CtLiteral) randomLocalVar.getDefaultExpression()).getValue());

		randomLocalVar = ValueCreator.createRandomLocalVar(factory.Type().createArrayReference("int"));
		count++;

		assertEquals("vc_" + count, randomLocalVar.getSimpleName());
		assertEquals(factory.Type().createArrayReference("int"), randomLocalVar.getType());

		randomLocalVar = ValueCreator.createRandomLocalVar(factory.Type().createReference("fr.inria.mutation.ClassUnderTest"));
		count++;

		assertEquals("vc_" + count, randomLocalVar.getSimpleName());
		assertEquals(factory.Type().createReference("fr.inria.mutation.ClassUnderTest"), randomLocalVar.getType());
	}

	@Test
	public void testCreateNull() throws Exception, InvalidSdkException {
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
