package fr.inria.diversify.dspot.amp;

import fr.inria.diversify.dspot.Utils;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/8/16
 */
public class TestValueCreator {

    @Test
    public void testCreateRandomLocalVar() throws Exception {

        /*
            Test the value created randomly by the value creator.
                - one primitive
                - one array
                - one object
         */

        AmplifierHelper.setSeedRandom(23L);
        final ValueCreator valueCreator = new ValueCreator();
        Launcher launcher = Utils.buildSpoon(Arrays.asList("src/test/resources/mutation/ClassUnderTestTest.java", "src/test/resources/mutation/ClassUnderTest.java"));

        int count = 0;

        CtLocalVariable randomLocalVar = valueCreator.createRandomLocalVar(launcher.getFactory().Type().INTEGER_PRIMITIVE);

        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(launcher.getFactory().Type().INTEGER_PRIMITIVE, randomLocalVar.getType());
        assertEquals(1434614297, ((CtLiteral)randomLocalVar.getDefaultExpression()).getValue());

        randomLocalVar = valueCreator.createRandomLocalVar(launcher.getFactory().Type().createArrayReference("int"));
        count++;

        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(launcher.getFactory().Type().createArrayReference("int"), randomLocalVar.getType());

        randomLocalVar = valueCreator.createRandomLocalVar(launcher.getFactory().Type().createReference("mutation.ClassUnderTest"));
        count++;

        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(launcher.getFactory().Type().createReference("mutation.ClassUnderTest"), randomLocalVar.getType());
    }

    @Test
    public void testCreateNull() throws Exception {
        /*
            Test the value created randomly by the value creator.
                - one primitive
                - one array
                - one object
         */

        AmplifierHelper.setSeedRandom(23L);
        final ValueCreator valueCreator = new ValueCreator();
        Launcher launcher = Utils.buildSpoon(Arrays.asList("src/test/resources/mutation/ClassUnderTestTest.java", "src/test/resources/mutation/ClassUnderTest.java"));

        int count = 0;

        CtLocalVariable randomLocalVar = valueCreator.createNull(launcher.getFactory().Type().INTEGER_PRIMITIVE);


        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(launcher.getFactory().Type().INTEGER_PRIMITIVE, randomLocalVar.getType());
        assertEquals("(int)null", randomLocalVar.getDefaultExpression().toString());

        randomLocalVar = valueCreator.createNull(launcher.getFactory().Type().createArrayReference("int"));
        count++;

        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(launcher.getFactory().Type().createArrayReference("int"), randomLocalVar.getType());
        assertEquals("(java.lang.reflect.Array)null", randomLocalVar.getDefaultExpression().toString());

        randomLocalVar = valueCreator.createNull(launcher.getFactory().Type().createReference("mutation.ClassUnderTest"));
        count++;

        assertEquals("vc_"+count, randomLocalVar.getSimpleName());
        assertEquals(launcher.getFactory().Type().createReference("mutation.ClassUnderTest"), randomLocalVar.getType());
        assertEquals("(mutation.ClassUnderTest)null", randomLocalVar.getDefaultExpression().toString());
    }
}
