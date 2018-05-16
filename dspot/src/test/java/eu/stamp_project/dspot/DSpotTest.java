package eu.stamp_project.dspot;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.amplifier.TestMethodCallAdder;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.utils.sosiefier.InputConfiguration;
import org.junit.AfterClass;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/11/17
 */
public class DSpotTest extends AbstractTest {

    @Test
    public void testExcludedClassesInPropertyFile() throws Exception {

        /*
            Usage of properties:
                - excludedClasses: list of full qualified name of test classes to be excluded (separated by comma ',')
                - excludedTestCases: list of name of test cases (methods) to be excluded (separated by comma ',')
         */

        final MockDSpot dSpot = new MockDSpot(Utils.getInputConfiguration(),
                1,
                Collections.singletonList(new TestMethodCallAdder()),
                new JacocoCoverageSelector()
        );
        // the test class fr.inria.filter.passing.PassingTest has 3 methods, but only two are amplified
        assertEquals(3, Utils.findClass("fr.inria.filter.passing.PassingTest").getMethods().size());
        // the test class fr.inria.filter.failing.FailingTest match the regex, but it is excluded in the properties
        final List<CtType> ctTypes = dSpot.amplifyTest("fr.inria.filter.*");
        assertEquals(1, ctTypes.size());
        // uses the mock to retrieve the number of method to be amplified
        assertEquals(2, dSpot.numberOfMethod);
    }

    private class MockDSpot extends DSpot {

        public int numberOfMethod = 0;

        public MockDSpot(InputConfiguration inputConfiguration, int numberOfIterations, List<Amplifier> amplifiers, TestSelector testSelector) throws Exception {
            super(inputConfiguration, numberOfIterations, amplifiers, testSelector);
        }

        @Override
        protected List<CtMethod<?>> filterTestCases(List<CtMethod<?>> testMethods) {
            List<CtMethod<?>> filteredMethods = super.filterTestCases(testMethods);
            numberOfMethod = filteredMethods.size();
            return filteredMethods;
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Utils.reset();
    }
}
