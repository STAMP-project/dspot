package fr.inria.helper;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassWithInnerClass {

    class MyInnerClass {
        int value;
    }

    @Test
    public void test() {
        MyInnerClass innerClass = new MyInnerClass();
        innerClass.value = 4;
        assertEquals(4, innerClass.value);
    }

    @Test
    public void notATestBecauseEmpty() {

    }

    @Test
    public void notATestBecauseTooDeepCallToAssertion() throws Exception {
        methodIntermediate1();
    }

    @Test
    public void testWithDeepCallToAssertion() throws Exception {
        methodIntermediate2();
    }

    private void methodIntermediate1() {
        methodIntermediate2();
    }

    private void methodIntermediate2() {
        methodIntermediate3();
    }

    private void methodIntermediate3() {
        methodIntermediate4();
    }

    private void methodIntermediate4() {
        assertTrue(true);
    }

    public void notATestBecauseMixinJunit3AndJunit4() {
        assertTrue(true);
    }

    @Test
    public void notATestBecauseParameters(int a) {
        assertTrue(true);
    }
}