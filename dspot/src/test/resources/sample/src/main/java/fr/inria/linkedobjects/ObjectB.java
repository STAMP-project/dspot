package fr.inria.linkedobjects;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/04/19
 */
public class ObjectB {

    private String s;

    public ObjectB(String s) {
        this.s = s;
    }

    public ObjectB(ObjectA a) {
        this.s = a.getS();
    }

    public String getS() {
        return s;
    }
}
