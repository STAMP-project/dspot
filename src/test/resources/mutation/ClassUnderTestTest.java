package mutation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassUnderTestTest {

    @Test
    public void testLit() {
        ClassUnderTest underTest = new ClassUnderTest();
        assertEquals(1, underTest.plusOne(0));
        assertEquals(0, underTest.minueOne(1));
    }
}