

package example;


public class Example {
    /* Return the index char of s
    or the last if index > s.length
    or the first if index < 0
     */
    public char charAt(java.lang.String s, int index) {
        try {
            fr.inria.diversify.logger.Logger.methodIn(Thread.currentThread(),"0");
            fr.inria.diversify.logger.Logger.branch(Thread.currentThread(),"b");
            if (index <= 0) {
                fr.inria.diversify.logger.Logger.branch(Thread.currentThread(),"t0");
                return s.charAt(0);
            }else {
                fr.inria.diversify.logger.Logger.branch(Thread.currentThread(),"e1");
            }
            if (index < (s.length())) {
                fr.inria.diversify.logger.Logger.branch(Thread.currentThread(),"t2");
                return s.charAt(index);
            }else {
                fr.inria.diversify.logger.Logger.branch(Thread.currentThread(),"e3");
            }
            return s.charAt(((s.length()) - 1));
        } finally {
            fr.inria.diversify.logger.Logger.methodOut(Thread.currentThread(),"0");
        }
    }

    public Example() {
        try {
            fr.inria.diversify.logger.Logger.methodIn(Thread.currentThread(),"1");
            fr.inria.diversify.logger.Logger.branch(Thread.currentThread(),"b");
            int variableInsideConstructor;
            variableInsideConstructor = 15;
            index = 2 * variableInsideConstructor;
        } finally {
            fr.inria.diversify.logger.Logger.methodOut(Thread.currentThread(),"1");
        }
    }

    private int index = 419382;

    private static java.lang.String s = "Overloading field name with parameter name";
}

