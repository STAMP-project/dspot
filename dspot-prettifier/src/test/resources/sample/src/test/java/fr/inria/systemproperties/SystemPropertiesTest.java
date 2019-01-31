package fr.inria.systemproperties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/10/17
 */
public class SystemPropertiesTest {

    @Test
    public void testSystemProperties() throws Exception {
        assertEquals("toto", System.getProperty("admin"));
        assertEquals("tata", System.getProperty("passwd"));
        assertEquals("toto", System.getProperties().get("admin"));
        assertEquals("tata", System.getProperties().get("passwd"));
    }
}
