package fr.inria.helper;

import junit.framework.TestCase;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/04/18
 */
public class SecondClassJUnit3 extends TestCase {

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void test() {
        assertEquals(3, 3);
    }

    public void should() {
        assertTrue(true);
    }

}
