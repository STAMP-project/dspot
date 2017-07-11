package fr.inria.diversify.dspot.inheritance;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.util.Collections;

import static org.junit.Assert.assertEquals;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/6/17
 */
public class InheritanceTest extends AbstractTest {

    //TODO
    @Test
    public void testInheritanceMethod() throws Exception, InvalidSdkException {
        CtClass<?> classInherit = Utils.getFactory().Class().get("fr.inria.inheritance.Inherited");
        DSpot dspot = new DSpot(Utils.getInputConfiguration(), 3, Collections.singletonList(new TestDataMutator()));
        CtType ctType = dspot.amplifyTest(classInherit);
        assertEquals(2, ctType.getMethods().size());
    }

}
