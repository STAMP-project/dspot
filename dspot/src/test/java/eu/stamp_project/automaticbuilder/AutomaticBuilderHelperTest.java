package eu.stamp_project.automaticbuilder;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.program.InputConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import static org.junit.Assert.assertEquals;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/04/19
 */
public class AutomaticBuilderHelperTest extends AbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        Utils.reset();
        super.setUp();
    }

    @Test
    public void testGetFilter() {

        /*
            Test that getFilter returns the setted filter in the InputConfiguration
         */
        assertEquals("fr.inria.sample.*", AutomaticBuilderHelper.getFilter());
    }

    @Test
    public void testGetFilterNoFilterSpecified() {

        /*
            Test that getFilter compute on the fly the filter when no filter is specified in the InputConfiguration
         */

        InputConfiguration.get().setFilter("");
        assertEquals("fr.inria.*", AutomaticBuilderHelper.getFilter());
    }

    @Test
    public void testGetFilterNoFilterSpecifiedAndClassInDefaultPackage() {
        /*
            Test that getFilter compute on the fly the filter when no filter is specified in the InputConfiguration
                and there is at least on class in the default package, i.e. no package
         */

        InputConfiguration.get().setFilter("");
        InputConfiguration.get().getFactory().createClass("FakeClass");
        assertEquals("FakeClass,fr.inria.*", AutomaticBuilderHelper.getFilter());
    }

    @After
    public void tearDown() throws Exception {
        final CtType<?> fakeClass = InputConfiguration.get().getFactory().Type().get("FakeClass");
        if (fakeClass != null) {
            InputConfiguration.get().getFactory().Package().getRootPackage().removeType(fakeClass);
        }
    }
}
