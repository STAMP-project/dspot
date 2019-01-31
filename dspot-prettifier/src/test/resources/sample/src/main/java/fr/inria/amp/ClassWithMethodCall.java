package fr.inria.amp;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/08/18
 */
public class ClassWithMethodCall {

    public void method(Integer a, int b) {
        // empty
    }

    public void methodCall() {
        Integer i = 1;
        int z = 1;
        this.method(1, 1);
    }

}
