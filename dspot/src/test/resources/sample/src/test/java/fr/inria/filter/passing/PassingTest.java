package fr.inria.filter.passing;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/10/17
 */
public class PassingTest {

    @Test
    public void testAssertion() throws Exception {
        assertFalse(false);
    }

    @Test
    public void failingTestCase() throws Exception {
        assertFalse(true);
    }
}
