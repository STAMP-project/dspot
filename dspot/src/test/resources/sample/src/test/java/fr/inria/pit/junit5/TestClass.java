package fr.inria.pit.junit5;

import fr.inria.pit.ClassToBeTested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestClass {

    @Test
    void test() {
        ClassToBeTested classToBeTested = new ClassToBeTested();
        classToBeTested.aMethod();
        assertEquals(1, classToBeTested.getA());
    }
}
