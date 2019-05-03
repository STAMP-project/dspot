package fr.inria.pit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestClass {

    @Test
    public void test() {
        ClassToBeTested classToBeTested = new ClassToBeTested();
        classToBeTested.aMethod();
        assertEquals(1, classToBeTested.getA());
    }
}
