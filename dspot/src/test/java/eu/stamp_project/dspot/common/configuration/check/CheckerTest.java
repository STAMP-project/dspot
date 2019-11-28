package eu.stamp_project.dspot.common.configuration.check;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/11/18
 */
public class CheckerTest {

    @Test
    public void testJVMArgs() {
        assertTrue(Checker.checkJVMArgs("-Xms1024M"));
        assertTrue(Checker.checkJVMArgs("-Xms1024m"));
        assertTrue(Checker.checkJVMArgs("-Xms1G"));
        assertTrue(Checker.checkJVMArgs("-Xms1g"));

        assertTrue(Checker.checkJVMArgs("-Xmx1024M"));
        assertTrue(Checker.checkJVMArgs("-Xmx1024m"));
        assertTrue(Checker.checkJVMArgs("-Xmx1G"));
        assertTrue(Checker.checkJVMArgs("-Xmx1g"));

        assertTrue(Checker.checkJVMArgs("-Daproperty=3"));

        assertFalse(Checker.checkJVMArgs("-Daproperty3"));
        assertFalse(Checker.checkJVMArgs("-aproperty=3"));
        assertFalse(Checker.checkJVMArgs("-Xmx1x"));
        assertFalse(Checker.checkJVMArgs("-Xms1x"));
    }

    @Test
    public void testCheckVersion() {
        Checker.checkIsACorrectVersion("1");
        Checker.checkIsACorrectVersion("10");
        Checker.checkIsACorrectVersion("10.1");
        Checker.checkIsACorrectVersion("10.10");

        Checker.checkIsACorrectVersion("1.1.1");
        Checker.checkIsACorrectVersion("1.1.10");
        Checker.checkIsACorrectVersion("1.10.10");
        Checker.checkIsACorrectVersion("10.1.10");
        Checker.checkIsACorrectVersion("10.10.10");

        //Version with snapshot
        Checker.checkIsACorrectVersion("1.2.5-SNAPSHOT");

        try {
            Checker.checkIsACorrectVersion("1.");
            fail("should have thrown InputErrorException");
        } catch (InputErrorException e) {
            // expected
        }

        try {
            Checker.checkIsACorrectVersion("a.");
            fail("should have thrown InputErrorException");
        } catch (InputErrorException e) {
            // expected
        }

        try {
            Checker.checkIsACorrectVersion("1.a");
            fail("should have thrown InputErrorException");
        } catch (InputErrorException e) {
            // expected
        }

        try {
            Checker.checkIsACorrectVersion("b");
            fail("should have thrown InputErrorException");
        } catch (InputErrorException e) {
            // expected
        }
    }
}
