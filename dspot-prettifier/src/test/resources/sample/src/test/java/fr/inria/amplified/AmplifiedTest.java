package fr.inria.amplified;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class AmplifiedTest {

    @Test
    public void amplifiedTest() throws Exception {
        int __DSPOT_1 = 5;
        assertEquals(5, __DSPOT_1);
    }

    @Test
    public void amplifiedTest2() throws Exception {
        Integer __DSPOT_1 = 5;
        assertEquals(5, __DSPOT_1.intValue());
        System.out.println(__DSPOT_1.intValue());
        assertEquals(5, __DSPOT_1.intValue());
    }

    @Test
    public void amplifiedTest3() throws Exception {
        Integer __DSPOT_1 = 5;
        assertEquals(5, __DSPOT_1.intValue());
        assertEquals(5, __DSPOT_1.intValue());
    }
}
