package fr.inria.filter.passing;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
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

    @Test (expected = NullPointerException.class, timeout = 100)
    public void testNPEExpected() throws Exception {
        String nullString = null;
        assertEquals(-1, nullString.length());
    }
}
