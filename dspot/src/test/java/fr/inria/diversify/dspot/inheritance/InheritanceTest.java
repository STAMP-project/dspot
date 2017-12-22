package fr.inria.diversify.dspot.inheritance;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/6/17
 */
public class InheritanceTest {

    @Before
    public void setUp() throws Exception {
        Utils.reset();
    }

    @Test
    public void testInheritanceMethod() throws Exception {
        final InputConfiguration configuration = new InputConfiguration("src/test/resources/sample/sample.properties");
        DSpot dspot = new DSpot(configuration, 3, Collections.singletonList(new TestDataMutator()));
        CtType<?> ctType = dspot.amplifyTest("fr.inria.inheritance.Inherited").get(0);
        assertEquals(1, ctType.getMethods().size());
    }

}
