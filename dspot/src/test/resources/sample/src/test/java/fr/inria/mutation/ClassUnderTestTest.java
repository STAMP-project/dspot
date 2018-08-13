package fr.inria.mutation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassUnderTestTest {

    @Test
    public void testLit() {
        ClassUnderTest underTest = new ClassUnderTest();
        assertEquals(1, underTest.plusOne(0));
    }

    @Test
    public void testAddCall() throws Exception {
        ClassUnderTest underTest = new ClassUnderTest();
        underTest.plusOne(0);
        underTest.minusOne(1);
    }

    @Test
    public void testWithIf() throws Exception {
        ClassUnderTest underTest = new ClassUnderTest();
        if (underTest.getBoolean()) {
            assertTrue(true);
        }
        if (! (underTest.getBoolean())) {
            assertTrue(true);
        }
    }
}