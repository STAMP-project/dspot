package fr.inria.helper;

import static junit.framework.TestCase.assertEquals;

public class ClassJunit3 {

    class MyInnerClass {
        int value;
    }

    public void test() {
        MyInnerClass innerClass = new MyInnerClass();
        innerClass.value = 4;
        assertEquals(4, innerClass.value);
    }

}