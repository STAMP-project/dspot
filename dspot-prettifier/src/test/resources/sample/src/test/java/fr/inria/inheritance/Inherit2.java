package fr.inria.inheritance;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class Inherit2 extends fr.inria.inheritance.Inherited {

    public int getValueToBeAsserted() {
        return 0;
    }

    @Test
    public void myTest() {
        assertTrue(true);
    }

}