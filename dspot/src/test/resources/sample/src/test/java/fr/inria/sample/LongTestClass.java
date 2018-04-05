package fr.inria.sample;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/04/18
 */
public class LongTestClass {

    @Test
    public void test() throws Exception {
        for (int i = 0 ; i < 10E6 ; i++) {
            assertTrue(true);
        }
    }
}
