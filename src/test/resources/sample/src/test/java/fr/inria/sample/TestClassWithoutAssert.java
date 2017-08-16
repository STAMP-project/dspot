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
        java.io.File file = new java.io.File("");
        boolean var = cl.getTrue();
    }

    @Test
    public void test2() {
        ClassWithBoolean cl = new ClassWithBoolean();
        cl.getFalse();
        cl.getFalse();
        cl.getFalse();
    }
}
