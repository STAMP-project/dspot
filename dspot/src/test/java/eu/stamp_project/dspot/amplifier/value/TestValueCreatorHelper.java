package eu.stamp_project.dspot.amplifier.value;

import eu.stamp_project.Utils;
import eu.stamp_project.AbstractTest;
import org.junit.Test;
import spoon.reflect.factory.Factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/10/17
 */
public class TestValueCreatorHelper extends AbstractTest {

    @Test
    public void testCanGenerateValueFor() throws Exception {
        final Factory factory = Utils.getFactory();
        assertTrue(ValueCreatorHelper.canGenerateAValueForType(factory.Class().get(Integer.class).getReference()));
        assertTrue(ValueCreatorHelper.canGenerateAValueForType(factory.Type().createReference(List.class)));
        assertTrue(ValueCreatorHelper.canGenerateAValueForType(factory.Class().get("fr.inria.inheritance.InheritanceSource").getReference()));
        assertFalse(ValueCreatorHelper.canGenerateAValueForType(factory.Class().get("fr.inria.inheritance.Inherited").getReference()));
        assertFalse(ValueCreatorHelper.canGenerateAValueForType(factory.Type().createReference(Iterator.class)));
        ArrayList<? extends Object> list = new ArrayList<>();
        assertTrue(ValueCreatorHelper.canGenerateAValueForType(factory.Type().createReference(list.getClass())));
    }
}
