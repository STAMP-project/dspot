package eu.stamp_project.dspot.assertiongenerator.assertiongenerator;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.testrunner.runner.Failure;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/05/18
 */
public class TryCatchFailGeneratorTest extends AbstractTestOnSample {


    @Test
    public void testOnStandardsErrors() throws Exception {

        /*
            The TryCatchFailGeneratorTest should discard:
                - OutOfMemoryError
                - StackOverFlowError
         */

        String testName = "testOutOfMemoryError";
        final String testClassName = "fr.inria.filter.failing.FailingTest";
        final TryCatchFailGenerator tryCatchFailGenerator = new TryCatchFailGenerator();


        CtMethod testAssertionError = findMethod(testClassName, testName);
        CtMethod<?> ctMethod = tryCatchFailGenerator
                .surroundWithTryCatchFail(testAssertionError,
                        new Failure(testName, testClassName, new AssertionError())
                );
        assertNull(ctMethod);

        testName = "testStackOverFlowError";
        testAssertionError = findMethod(testClassName, testName);
        ctMethod = tryCatchFailGenerator
                .surroundWithTryCatchFail(testAssertionError,
                        new Failure(testName, testClassName, new AssertionError())
                );
        assertNull(ctMethod);
    }

    @Test
    public void testAssertionErrorFailureReturnNull() throws Exception {

        /*
            When the cause of the failure is an AssertionError, it means that there is still
            an assertions inside the test, while it shouldn't
            In this case, we return null, since surrounding with try/catch such error is
            meaningless.
         */

        final String testName = "testAssertionError";
        final String testClassName = "fr.inria.filter.failing.FailingTest";
        final CtMethod testAssertionError = findMethod(testClassName, testName);
        final TryCatchFailGenerator tryCatchFailGenerator = new TryCatchFailGenerator();
        final CtMethod<?> ctMethod = tryCatchFailGenerator
                .surroundWithTryCatchFail(testAssertionError,
                        new Failure(testName, testClassName, new AssertionError())
                );
        assertNull(ctMethod);
    }

    @Test
    public void testSurroundWithTryCatchFail() throws Exception {

        final String testName = "testFailingWithException";
        final String testClassName = "fr.inria.filter.failing.FailingTest";
        final CtMethod testAssertionError = findMethod(testClassName, testName);
        final TryCatchFailGenerator tryCatchFailGenerator = new TryCatchFailGenerator();
        CtMethod<?> ctMethod = tryCatchFailGenerator
                .surroundWithTryCatchFail(testAssertionError,
                        new Failure(testName, testClassName, new NullPointerException())
                );
        ctMethod = tryCatchFailGenerator
                .surroundWithTryCatchFail(ctMethod,
                        new Failure(testName, testClassName, new ArrayIndexOutOfBoundsException(-100))
                );
        System.out.println(ctMethod);
        assertThat(ctMethod.toString(), containsString("catch (java.lang.NullPointerException"));
        assertThat(ctMethod.toString(), containsString("catch (java.lang.ArrayIndexOutOfBoundsException"));
    }
}
