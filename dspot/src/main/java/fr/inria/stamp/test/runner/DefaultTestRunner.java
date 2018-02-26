package fr.inria.stamp.test.runner;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.stamp.test.filter.MethodFilter;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 */
@Deprecated
public class DefaultTestRunner extends AbstractTestRunner {

	DefaultTestRunner(URLClassLoader classLoader) {
		super(classLoader);
	}

	DefaultTestRunner(String classpath) {
		super(classpath);
	}

	DefaultTestRunner(String[] classpath) {
		super(classpath);
	}

	@Override
	public TestListener run(Class<?> testClass, Collection<String> testMethodNames, RunListener... additionalListeners) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		TestListener listener = new TestListener();
		final Future<?> submit = executor.submit(() -> {
			Request request = Request.aClass(testClass);
			if (!testMethodNames.isEmpty()) {
				request = request.filterWith(new MethodFilter(testMethodNames));
			}
			Runner runner = request.getRunner();
			RunNotifier runNotifier = new RunNotifier();
			Arrays.stream(additionalListeners).forEach(runNotifier::addListener);
			runNotifier.addFirstListener(listener);

			// Since we want to use our custom ClassLoader to run the tests of the project being executed by DSpot,
			// and since we create a new thread for starting the JUnit Runner, we need to set the context ClassLoader
			// to be our custom ClassLoader. This is so that any code in the tests or triggered by the test that uses
			// the context ClassLoader will work.
			// As an example if the tests call some code that uses Java's ServiceLoader then it would fail to find and
			// load any provider located in our custom ClassLoader.
			Thread.currentThread().setContextClassLoader(this.classLoader);

			runner.run(runNotifier);
		});
		try {
			long timeBeforeTimeOut = testMethodNames.isEmpty() ?
					AmplificationHelper.getTimeOutInMs() * (testClass.getMethods().length + 1) :
					AmplificationHelper.getTimeOutInMs() * (testMethodNames.size() + 1);
			submit.get(timeBeforeTimeOut, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			submit.cancel(true);
			executor.shutdownNow();
		}
		return listener;
	}

}
