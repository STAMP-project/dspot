package fr.inria.sample;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 10:58
 */
public class TestClassWithAssert extends TestClassWithAssertOld {

    @Test
    public void testWithCatchVariable() throws Exception {
        try {
            System.out.println("");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    @Test
    public void test1() {
        ClassWithBoolean cl = new ClassWithBoolean();
        assertTrue(cl.getTrue());
        assertTrue(true);
    }

    @Test
    public void test1_withoutAssert() {
        ClassWithBoolean cl = new ClassWithBoolean();
        Object o_3_0 = cl.getTrue();
    }

    @Test
    public void test2() {
        ClassWithBoolean cl = new ClassWithBoolean();
        assertTrue(cl.getTrue());
        assertTrue(cl.getFalse());
    }

    @Test
    public void test2_RemoveFailAssert() {
        ClassWithBoolean cl = new ClassWithBoolean();
        assertTrue(cl.getTrue());
        Object o_5_0 = cl.getFalse();
    }

    @Test
    public void test3() throws Exception {
        ClassThrowException cl = new ClassThrowException();
        cl.throwException();
    }

    @Test
    public void test3_exceptionCatch() throws Exception {
        try {
            ClassThrowException cl = new ClassThrowException();
            cl.throwException();
            junit.framework.TestCase.fail("test3 should have thrown Exception");
        } catch (java.lang.Exception eee) {
        }
    }

    @Test
    public void testWithArray() throws Exception {
        assertEquals(new Integer[]{1,2}, new Integer[]{1,2});
    }
}
