package fr.inria.statementadd;

/**
 * Created by bdanglot on 11/30/16.
 */
public class ClassParameterAmplify {

    private int i;

    public ClassParameterAmplify(Integer i) {
        this.i = i;
    }

    public ClassParameterAmplify(ClassParameterAmplify parameterAmplify) {
        this.i = parameterAmplify.i;
    }

    public ClassParameterAmplify(ClassParameterAmplify parameterAmplify, Integer i) {
        this.i = parameterAmplify.i + i;
    }

    public void method1() {
        System.out.println("");
    }

    private void method2() {
        System.out.println("");
    }

    protected void method3() {
        System.out.println("");
    }

}
