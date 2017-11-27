package fr.inria.filter.failing;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/10/17
 */
public class FailingTest {

    @Test
    public void testAssertionError() throws Exception {
        assertFalse(true);
    }
}
