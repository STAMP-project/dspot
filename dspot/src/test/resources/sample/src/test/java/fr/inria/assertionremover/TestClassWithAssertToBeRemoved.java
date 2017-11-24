package fr.inria.assertionremover;

import org.junit.Test;
import fr.inria.sample.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 10:58
 */
public class TestClassWithAssertToBeRemoved {

    public static boolean getNegation(boolean value) {
        return !value;
    }

    @Test
    public void test1() {
        ClassWithBoolean cl = new ClassWithBoolean();
        assertTrue(cl.getTrue());
        assertTrue(true);
        int one = 1;
        switch (one) {
            case 1:
                assertTrue(getNegation(cl.getFalse()));
                break;
        }
    }

    @Test
    public void test2() throws Exception {
        int a = -1;
        assertEquals(-1, a);
        int b = 1;
        assertEquals(1, b);
    }
}
