package fr.inria.diversify.dspot.selector;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;
import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.amplifier.StatementAdd;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.stamp.EntryPoint;
import org.junit.Test;
import spoon.reflect.declaration.CtType;

import static org.junit.Assert.assertFalse;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/12/17
 */
public class CloverCoverageSelectorTest {

    @Test
    public void test() throws Exception {

        /*
            This selector aims at keeping amplified test that execute new lines in the source code.
         */

        Utils.reset();
        Utils.init("src/test/resources/test-projects/test-projects.properties");
        EntryPoint.verbose = true;

        final DSpot dspot = new DSpot(Utils.getInputConfiguration(),
                1,
                Arrays.asList(new Amplifier[]{new TestDataMutator(), new StatementAdd()}),
                new CloverCoverageSelector()
        );
        final CtType ctType = dspot.amplifyTest(Utils.findClass("example.TestSuiteExample"),
                Collections.singletonList(Utils.findMethod("example.TestSuiteExample", "test2"))
        );
        assertFalse(ctType.getMethodsByName("test2_literalMutationNumber9").isEmpty());
        EntryPoint.verbose = false;
    }
}
