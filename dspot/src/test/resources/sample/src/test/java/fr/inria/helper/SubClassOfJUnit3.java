package fr.inria.helper;

public class SubClassOfJUnit3 extends SecondClassJUnit3 {

    @Override
    public void test() {
        assertEquals(3, 3);
    }

    public void testThatIsATest() {
        assertEquals(3,3);
    }

}
