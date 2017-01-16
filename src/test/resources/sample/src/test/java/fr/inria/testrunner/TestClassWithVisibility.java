package fr.inria.testrunner;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestClassWithVisibility {

    @Test
    public void test() throws Exception {
        assertEquals(1, new ClassWithVisibility().method());
    }

    @Test
    public void testp() throws Exception {
        assertEquals(1, new ClassWithVisibility().methodp());
    }

    @Test
    public void testFromCommonsLang() throws Exception {
        final ClassWithVisibility style = new ClassWithVisibility();
        style.setSummaryObjectStartText(null);
        assertEquals("", style.getSummaryObjectStartText());
    }
}