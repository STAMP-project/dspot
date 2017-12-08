package fr.inria.multiple.pom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloWorldTest {

    @Test
    public void test() throws Exception {
        assertEquals("Hello World", HelloWorld.run());
    }
}