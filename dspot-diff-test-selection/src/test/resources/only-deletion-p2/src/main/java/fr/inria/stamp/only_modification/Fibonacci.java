package fr.inria.stamp.only_modification;

public class Fibonacci {

    /**
     * Credit: http://www.rosettacode.org/wiki/Fibonacci_sequence#Java
     * @param n bound for the computation
     * @return the number of Fibonacci for the given bound
     */
    public static long itFibN(int n)
    {
        if (n < 2) {
            return n;
        }
        long ans = 0;
        long n1 = 0;
        long n2 = 1;
        for(n--; n > 0; n--)
        {
            ans = n1 + n2;
            n1 = n2;
            n2 = ans;
        }
        return ans;
    }

}
