package eu.stamp_project.utils.check;

import eu.stamp_project.Main;
import eu.stamp_project.utils.options.check.Checker;
import eu.stamp_project.utils.options.check.InputErrorException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/11/18
 */
public class CheckerTest {

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

    @Test
    public void testWrongPathToProperties() {
        try {
            Main.main(new String[0]);
            fail();
        } catch (InputErrorException e) {
            assertEquals("Error in the provided input. Please check your properties file and your command-line options.", e.getMessage());
        }
    }

    @Test
    public void testNoCorrectValueForAmplifiers() {
        try {
            Main.main(new String[] {
                    "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                    "--amplifiers", "NotAnAmplifier:NotAnotherAmplifier"
            });
            fail();
        } catch (InputErrorException e) {
            assertEquals("Error in the provided input. Please check your properties file and your command-line options.", e.getMessage());
        }
    }

    @Test
    public void testNoCorrectValueForBudgetizer() {
        try {
            Main.main(new String[] {
                    "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                    "--budgetizer", "NotABudgetizer"
            });
            fail();
        } catch (InputErrorException e) {
            assertEquals("Error in the provided input. Please check your properties file and your command-line options.", e.getMessage());
        }
    }

    @Test
    public void testNoCorrectValueForTestCriterion() {
        try {
            Main.main(new String[] {
                    "--path-to-properties", "src/test/resources/test-projects/test-projects.properties",
                    "--test-criterion", "NotASelector"
            });
            fail();
        } catch (InputErrorException e) {
            assertEquals("Error in the provided input. Please check your properties file and your command-line options.", e.getMessage());
        }
    }
}
