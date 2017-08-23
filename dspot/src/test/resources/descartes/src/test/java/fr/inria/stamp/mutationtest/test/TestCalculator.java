package fr.inria.stamp.mutationtest.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/03/17
 */
public class TestCalculator {

    @Test
    public void Integraltypestest() throws Exception {
        final Calculator calculator = new Calculator();
        assertEquals((byte)0, calculator.getByte());
        assertEquals((short) 0, calculator.getShort());
        assertEquals(0,  calculator.getCeiling());
        assertEquals(0L, calculator.getSquare());
        assertEquals(0, calculator.getLastOperatorSymbol());
    }

    @Test
    public void Floatingpointtypestest() throws Exception {
        final Calculator calculator = new Calculator();
        assertEquals(0.0F, calculator.getSomething(), 0.0F);
        assertEquals(23.0F, calculator.add(23F), 0.0F);
        assertEquals(748.11F, calculator.getSomething(), 0.1F);
    }
}
