package fr.inria.diversify.dspot.amp;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.dspot.value.ValueCreator;
import fr.inria.diversify.dspot.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.factory.Factory;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/8/16
 */
public class TestValueCreator extends AbstractTest {

    @Test
    public void testCreateRandomLocalVar() throws Exception, InvalidSdkException {

        /*
            Test the value created randomly by the value creator.
                - one primitive
                - one array
                - one object
         */

        AmplificationHelper.setSeedRandom(23L);
        final ValueCreator valueCreator = new ValueCreator();
        Factory factory = Utils.getFactory();

        int count = 0;

        CtLocalVariable randomLocalVar = valueCreator.createRandomLocalVar(factory.Type().INTEGER_PRIMITIVE);

        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(factory.Type().INTEGER_PRIMITIVE, randomLocalVar.getType());
        assertEquals(1434614297, ((CtLiteral)randomLocalVar.getDefaultExpression()).getValue());

        randomLocalVar = valueCreator.createRandomLocalVar(factory.Type().createArrayReference("int"));
        count++;

        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(factory.Type().createArrayReference("int"), randomLocalVar.getType());

        randomLocalVar = valueCreator.createRandomLocalVar(factory.Type().createReference("mutation.ClassUnderTest"));
        count++;

        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(factory.Type().createReference("mutation.ClassUnderTest"), randomLocalVar.getType());
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
        final ValueCreator valueCreator = new ValueCreator();
        Factory factory = Utils.getFactory();

        int count = 0;

        CtLocalVariable randomLocalVar = valueCreator.createNull(factory.Type().INTEGER_PRIMITIVE);


        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(factory.Type().INTEGER_PRIMITIVE, randomLocalVar.getType());
        assertEquals("(int)null", randomLocalVar.getDefaultExpression().toString());

        randomLocalVar = valueCreator.createNull(factory.Type().createArrayReference("int"));
        count++;

        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(factory.Type().createArrayReference("int"), randomLocalVar.getType());
        assertEquals("(int[])null", randomLocalVar.getDefaultExpression().toString());

        randomLocalVar = valueCreator.createNull(factory.Type().createReference("mutation.ClassUnderTest"));
        count++;

        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(factory.Type().createReference("mutation.ClassUnderTest"), randomLocalVar.getType());
        assertEquals("(mutation.ClassUnderTest)null", randomLocalVar.getDefaultExpression().toString());
    }

}
