package fr.inria.diversify.dspot;

import edu.emory.mathcs.backport.java.util.Collections;
import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.amplifier.TestMethodCallAdder;
import fr.inria.diversify.dspot.selector.JacocoCoverageSelector;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/11/17
 */
public class DSpotTest extends AbstractTest {

    @Test
    public void testExcludedClassesInPropertyFile() throws Exception {
        final DSpot dSpot = new DSpot(Utils.getInputConfiguration(),
                1,
                Collections.singletonList(new TestMethodCallAdder()),
                new JacocoCoverageSelector()
        );
        final List<CtType> ctTypes = dSpot.amplifyTest("fr.inria.filter.*");
        assertEquals(1, ctTypes.size());
    }
}
