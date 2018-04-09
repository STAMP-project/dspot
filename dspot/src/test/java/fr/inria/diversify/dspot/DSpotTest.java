package fr.inria.diversify.dspot;

import fr.inria.AbstractTest;
import fr.inria.Utils;
import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.dspot.amplifier.TestMethodCallAdder;
import fr.inria.diversify.dspot.selector.JacocoCoverageSelector;
import fr.inria.diversify.dspot.selector.TestSelector;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
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
    public void testInheritanceMethod() throws Exception {

        /*
            Test that DSpot can be run on an abstract test, using its implementation.
         */

        final InputConfiguration configuration = new InputConfiguration("src/test/resources/sample/sample.properties");
        DSpot dspot = new DSpot(configuration, 3, Collections.singletonList(new TestDataMutator()), new JacocoCoverageSelector());
        CtType<?> ctType = dspot.amplifyTest("fr.inria.inheritance.Inherited").get(0);
        assertEquals(1, ctType.getMethods().size());
        System.out.println(ctType);
    }

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
        // the test class fr.inria.filter.passing.PassingTest has 2 method, but only one is amplified
        assertEquals(2, Utils.findClass("fr.inria.filter.passing.PassingTest").getMethods().size());
        // the test class fr.inria.filter.failing.FailingTest match the regex, but it is excluded in the properties
        final List<CtType> ctTypes = dSpot.amplifyTest("fr.inria.filter.*");
        assertEquals(1, ctTypes.size());
        // uses the mock to retrieve the number of method to be amplified
        assertEquals(1, dSpot.numberOfMethod);
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
