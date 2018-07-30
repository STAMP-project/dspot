package fr.inria.sample;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 10:58
 */
public class TestClassWithAssertOld {

    @Test
    public void anOldTest() throws Exception {
        ClassWithBoolean cl = new ClassWithBoolean();
        assertTrue(cl.getTrue());
        assertTrue(true);
    }
}
