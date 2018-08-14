package fr.inria.filter.failing;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void testFailingWithException() throws Exception {
        String nullString = null;
        assertEquals(-1, nullString.length());
        new ArrayList<>().get(-100);
    }

    @Test
    public void testOutOfMemoryError() throws Exception {
        throw new OutOfMemoryError();
    }

    @Test
    public void testStackOverFlowError() throws Exception {
        throw new StackOverflowError();
    }
}
