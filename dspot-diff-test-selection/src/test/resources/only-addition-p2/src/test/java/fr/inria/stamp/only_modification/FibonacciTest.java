package fr.inria.stamp.only_modification;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FibonacciTest {

    @Test
    public void hittingTest() {
        final long l = Fibonacci.itFibN(1);
        assertEquals(1, l);
    }

    @Test
    public void test() {
        final long l = Fibonacci.itFibN(30);
        assertEquals(832040, l);
    }
}
