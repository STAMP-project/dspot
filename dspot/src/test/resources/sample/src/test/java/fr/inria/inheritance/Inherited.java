
package fr.inria.inheritance;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class Inherited {

    public abstract int getValueToBeAsserted();

    @Test
    public void test() {
        assertEquals(1, new fr.inria.inheritance.InheritanceSource().method(getValueToBeAsserted()));
    }

    @Test
    public void test2() {
        assertEquals(1, new fr.inria.inheritance.InheritanceSource().method(getValueToBeAsserted()));
    }

}