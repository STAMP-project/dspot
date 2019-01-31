package fr.inria.testresources;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestResources {

    @Test
    public void testResources() throws Exception {
        assertNotNull(TestResources.class.getClassLoader().getResourceAsStream("aResource"));
        assertNotNull(TestResources.class.getClassLoader().getResourceAsStream("aResourcesDirectory/anotherResource"));
    }
}