package fr.inria.removebadtest;

import org.junit.Test;
import junit.framework.TestCase;


public class TestClassToBeTested {

    @Test
    public void testKeep() throws Exception {
        ClassToBeTested classToBeTested = new ClassToBeTested();
        org.junit.Assert.assertEquals(1, classToBeTested.methodInt());
        org.junit.Assert.assertTrue(classToBeTested.methodBoolean());
        try {
            classToBeTested.methodException();
            TestCase.fail();
        } catch (Exception e) {
            //success;
        }
    }

    @Test
    public void testRemove1() throws Exception {
        ClassToBeTested classToBeTested = new ClassToBeTested();
        org.junit.Assert.assertEquals(2, classToBeTested.methodInt());
    }

    @Test
    public void testRemove2() throws Exception {
        ClassToBeTested classToBeTested = new ClassToBeTested();
        org.junit.Assert.assertFalse(classToBeTested.methodBoolean());
    }

    @Test
    public void testRemove3() throws Exception {
        ClassToBeTested classToBeTested = new ClassToBeTested();
        classToBeTested.methodException();
    }

}