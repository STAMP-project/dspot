package fr.inria.helper;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/04/18
 */
public class SubClassOfJUnit3 extends SecondClassJUnit3 {

    @Override
    public void test() {
        assertEquals(3, 3);
    }

    public void testThatIsATest() {
        assertEquals(3,3);
    }

}
