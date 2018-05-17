

package descartes.src.test.java.eu.stamp_project.mutationtest.test;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/03/17
 */
public class TestCalculator {
    @org.junit.Test
    public void Floatingpointtypestest() throws java.lang.Exception {
        final eu.stamp_project.stamp.mutationtest.test.Calculator calculator = new eu.stamp_project.stamp.mutationtest.test.Calculator();
        org.junit.Assert.assertEquals(0.0F, calculator.getSomething(), 0.0F);
        org.junit.Assert.assertEquals(23.0F, calculator.add(23.0F), 0.0F);
        org.junit.Assert.assertEquals(748.11F, calculator.getSomething(), 0.1F);
    }

    @org.junit.Test
    public void Integraltypestest() throws java.lang.Exception {
        final eu.stamp_project.stamp.mutationtest.test.Calculator calculator = new eu.stamp_project.stamp.mutationtest.test.Calculator();
        org.junit.Assert.assertEquals(((byte) (0)), calculator.getByte());
        org.junit.Assert.assertEquals(((short) (0)), calculator.getShort());
        org.junit.Assert.assertEquals(0, calculator.getCeiling());
        org.junit.Assert.assertEquals(0L, calculator.getSquare());
        org.junit.Assert.assertEquals(0, calculator.getLastOperatorSymbol());
    }
}

