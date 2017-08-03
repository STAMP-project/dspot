package fr.inria.diversify.dspot;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import org.junit.Before;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/31/17
 */
public abstract class AbstractTest {

    public static final String nl = System.getProperty("line.separator");

    @Before
    public void setUp() throws Exception {
        Utils.init("src/test/resources/sample/sample.properties");
        ValueCreator.count = 0;
    }

}
