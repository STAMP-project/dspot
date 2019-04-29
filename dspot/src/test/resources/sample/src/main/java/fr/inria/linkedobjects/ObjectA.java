package fr.inria.linkedobjects;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/04/19
 */
public class ObjectA {

    private String s;

    public ObjectA(String s) {
        this.s = s;
    }

    public ObjectA(ObjectB b) {
        this.s = b.getS();
    }

    public String getS() {
        return s;
    }

}
