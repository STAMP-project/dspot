package fr.inria.removebadtest;

public class ClassToBeTested {

    public int methodInt() {
        return 1;
    }

    public void methodException() {
        throw new RuntimeException();
    }

    public boolean methodBoolean() {
        return true;
    }


}