package mutation;

public class ClassUnderTest {

    public ClassUnderTest() {

    }

    public int plusOne(int integer) {
        return integer + 1;
    }

    public int minueOne(int integer) {
        return integer - 1;
    }

    /* not tested in ClassUnderTestTest */
    public int timesTwo(int integer) {
        return integer * 2;
    }

}