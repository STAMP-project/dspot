package fr.inria.testframework;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class TestSupportJUnit5 {

    @Test
    public void testJUnit5() {
        assertTrue(true);
    }

    @Test
    public void testExpectAnException() {
        assertTrue(true);
        throwAnException();
    }

    @Test
    public void testExpectAnExceptionAmplified() {
        assertThrows(RuntimeException.class, () -> {
            throwAnException();
        });
    }

    private void throwAnException() {
        throw new RuntimeException();
    }
}
