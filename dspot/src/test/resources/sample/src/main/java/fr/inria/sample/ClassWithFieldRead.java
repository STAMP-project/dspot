package fr.inria.sample;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/07/18
 */
public class ClassWithFieldRead {

    public Double getInfinity() {
        return Double.NEGATIVE_INFINITY;
    }

    public Double getNaN() {
        return Double.NaN;
    }

    public Integer getMax_VALUE() {
        return Integer.MAX_VALUE;
    }

    public double[] getDoubles() {
        return new double[]{Double.NaN, 0.0F, Double.POSITIVE_INFINITY, 1.0D};
    }

}
