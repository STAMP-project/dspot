package fr.inria.mutation;

public class ClassUnderTest {

    public ClassUnderTest() {

    }

    public int plusOne(int integer) {
        return integer + 1;
    }

    /* not tested in ClassUnderTestTest */
    public int minusOne(int integer) {
        return integer - 1;
    }

    public boolean getBoolean() {
        return true;
    }

}