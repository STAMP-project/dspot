package fr.inria.testresources;

import org.junit.Test;

public class TestResources {

    @Test
    public void testResources() throws Exception {
        org.junit.Assert.assertTrue(new java.io.File("./src/test/resources/aResource").exists());
        org.junit.Assert.assertTrue(new java.io.File("src/test/resources/aResource").exists());
        org.junit.Assert.assertTrue(new java.io.File("./src/test/resources/aResourcesDirectory/anotherResource").exists());
        org.junit.Assert.assertTrue(new java.io.File("src/test/resources/aResourcesDirectory/anotherResource").exists());
    }
}