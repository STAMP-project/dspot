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

    public void notATestBecauseMixinJunit3AndJunit4() {
        assertTrue(true);
    }

    @Test
    public void notATestBecauseParameters(int a) {
        assertTrue(true);
    }
}