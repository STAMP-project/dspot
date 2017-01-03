package fr.inria.testrunner;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestClassToBeTested {

    @Test
    public void test() throws Exception {
        assertEquals(1, new ClassToBeTested().method());
    }

    @Test
    public void testp() throws Exception {
        assertEquals(1, new ClassToBeTested().methodp());
    }

    @Test
    public void testFromCommonsLang() throws Exception {
        final ClassToBeTested style = new ClassToBeTested();
        style.setSummaryObjectStartText(null);
        assertEquals("", style.getSummaryObjectStartText());
    }
}