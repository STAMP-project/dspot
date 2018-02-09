package fr.inria.stamp.test.filter;

import fr.inria.stamp.test.runner.DefaultTestRunnerTest;
import fr.inria.stamp.test.runner.MockitoTestRunnerTest;
import org.junit.Test;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Request;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 04/07/17
 */
@Deprecated
public class MethodFilterTest {

	@Test
	public void test() throws Exception {
		Request request = Request.classes(MethodFilterTest.class,// contains 0 testRunTestClass
				DefaultTestRunnerTest.class,// contains 1 testRunTestClass
				MockitoTestRunnerTest.class);// contains 1 testRunTestClass
		request = request.filterWith(new MethodFilter(
				Collections.singletonList("testRunTestClass")
		));
		assertEquals(2, request.getRunner().testCount());
	}

	@Test
	public void testNoMatching() throws Exception {
		Request request = Request.classes(MethodFilterTest.class);// contains 0 testRunTestClass
		request = request.filterWith(new MethodFilter(
				Collections.singletonList("testRunTestClass")
		));
		assertTrue(request.getRunner() instanceof ErrorReportingRunner);
	}
}
