package fr.inria.sample;

import org.junit.Test;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 13:39
 */
public class TestClassWithoutAssert {

    @Test
    public void test1() {
        ClassWithBoolean cl = new ClassWithBoolean();
        cl.getFalse();
        cl.getBoolean();
        boolean var = cl.getTrue();
    }

    @Test
    public void test1_withAssert() {
        fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();
        junit.framework.Assert.assertFalse(((fr.inria.sample.ClassWithBoolean)cl).getFalse());
        junit.framework.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getTrue());
        junit.framework.Assert.assertTrue(((fr.inria.sample.ClassWithBoolean)cl).getBoolean());
        boolean o_test1_withoutAssert__3 = cl.getFalse();
        junit.framework.Assert.assertFalse(o_test1_withoutAssert__3);
        boolean o_test1_withoutAssert__4 = cl.getBoolean();
        junit.framework.Assert.assertTrue(o_test1_withoutAssert__4);
        boolean var = cl.getTrue();
        junit.framework.Assert.assertTrue(var);
    }
}
