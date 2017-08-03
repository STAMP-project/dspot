package fr.inria.stamp.test.runner;

import edu.emory.mathcs.backport.java.util.Collections;
import fr.inria.diversify.logger.Logger;
import fr.inria.stamp.test.filter.MethodFilter;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URLClassLoader;
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

	public MockitoTestRunner(URLClassLoader classLoader) {
		super(classLoader);
	}

	public MockitoTestRunner(String classpath) {
		super(classpath);
	}

	public MockitoTestRunner(String[] classpath) {
		super(classpath);
	}

	@Override
	public TestListener run(Class<?> testClass, Collection<String> testMethodNames) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		final TestListener listener = new TestListener();
		final Future<?> submit = executor.submit(() -> {
			try {
				MockitoJUnitRunner runner = new MockitoJUnitRunner(testClass);
				if (!testMethodNames.isEmpty()) {
					runner.filter(new MethodFilter(testMethodNames));
				}
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
	public TestListener run(Class<?> testClass) {
		return this.run(testClass, Collections.emptyList());
	}

	@Override
	public TestListener run(Class<?> testClass, Collection<String> methodNames, RunListener additionalListener) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		final TestListener listener = new TestListener();
		final Future<?> submit = executor.submit(() -> {
			try {
				MockitoJUnitRunner runner = new MockitoJUnitRunner(testClass);
				if (!methodNames.isEmpty()) {
					runner.filter(new MethodFilter(methodNames));
				}
				RunNotifier runNotifier = new RunNotifier();
				runNotifier.addFirstListener(listener);
				runNotifier.addFirstListener(additionalListener);
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
	public TestListener run(Class<?> testClass, RunListener additionalListener) {
		return this.run(testClass, Collections.emptyList(), additionalListener);
	}

}
