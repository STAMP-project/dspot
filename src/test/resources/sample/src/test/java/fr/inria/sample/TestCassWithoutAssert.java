package fr.inria.sample;

import org.junit.Test;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 13:39
 */
public class TestCassWithoutAssert {

    @Test
    public void test1() {
        ClassWithBoolean cl = new ClassWithBoolean();
        cl.getFalse();
        cl.getBoolean();
        boolean var = cl.getTrue();
    }

    @Test
    public void test1_withAssert() {
        ClassWithBoolean cl = new ClassWithBoolean();
        junit.framework.Assert.assertFalse(((ClassWithBoolean)cl).getFalse());
        junit.framework.Assert.assertTrue(((ClassWithBoolean)cl).getTrue());
        boolean o_test1_withoutAssert__3 = cl.getFalse();
        junit.framework.Assert.assertFalse(o_test1_withoutAssert__3);
        cl.getBoolean();
        boolean var = cl.getTrue();
        junit.framework.Assert.assertTrue(var);
    }
}
