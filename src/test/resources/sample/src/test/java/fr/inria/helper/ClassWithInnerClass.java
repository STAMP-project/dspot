package fr.inria.helper;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

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

}