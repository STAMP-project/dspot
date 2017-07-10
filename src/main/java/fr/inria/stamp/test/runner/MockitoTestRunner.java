package fr.inria.stamp.test.runner;

import fr.inria.diversify.logger.Logger;
import fr.inria.stamp.test.filter.MethodFilter;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/07/17.
 */
public class MockitoTestRunner extends AbstractTestRunner {

	public MockitoTestRunner(String classpath) {
		super(classpath);
	}

	public MockitoTestRunner(String[] classpath) {
		super(classpath);
	}

	@Override
	public TestListener run(String fullQualifiedName, Collection<String> testMethodNames) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		final TestListener listener = new TestListener();
		final Future<?> submit = executor.submit(() -> {
			try {
				MockitoJUnitRunner runner = new MockitoJUnitRunner(this.loadClass(fullQualifiedName));
				runner.filter(new MethodFilter(testMethodNames));
				RunNotifier runNotifier = new RunNotifier();
				runNotifier.addFirstListener(listener);
				runner.run(runNotifier);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		try {
			submit.get(10000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			submit.cancel(true);
			executor.shutdownNow();
			Logger.stopLogging();
			Logger.close();
		}
		return listener;
	}

	@Override
	public TestListener run(String fullQualifiedName) {
		try {
			TestListener listener = new TestListener();
			MockitoJUnitRunner runner = new MockitoJUnitRunner(this.loadClass(fullQualifiedName));
			RunNotifier runNotifier = new RunNotifier();
			runNotifier.addFirstListener(listener);
			runner.run(runNotifier);
			return listener;
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
