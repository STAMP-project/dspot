package fr.inria.stamp.test.runner;

import fr.inria.diversify.logger.Logger;
import fr.inria.stamp.test.filter.MethodFilter;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 */
public class DefaultTestRunner extends AbstractTestRunner {

	public DefaultTestRunner(URLClassLoader classLoader) {
		super(classLoader);
	}

	public DefaultTestRunner(String classpath) {
		super(classpath);
	}

	public DefaultTestRunner(String[] classpath) {
		super(classpath);
	}


	@Override
	public TestListener run(Class<?> classTest, Collection<String> testMethodNames) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		final TestListener listener = new TestListener();
		final Future<?> submit = executor.submit(() -> {
			Request request = Request.aClass(classTest);
			if (!testMethodNames.isEmpty()) {
				request = request.filterWith(new MethodFilter(testMethodNames));
			}
			Runner runner = request.getRunner();
			RunNotifier runNotifier = new RunNotifier();
			runNotifier.addFirstListener(listener);
			runner.run(runNotifier);
		});
		try {
			submit.get(1000000000 * (testMethodNames.size() + 1), TimeUnit.MILLISECONDS);
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
	public TestListener run(Class<?> classTest) {
		return this.run(classTest, Collections.emptyList());
	}

	@Override
	public TestListener run(Class<?> testClass, Collection<String> testMethodNames, RunListener additionalListener) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		TestListener listener = new TestListener();
		final Future<?> submit = executor.submit(() -> {
			Request request = Request.aClass(testClass);
			if (!testMethodNames.isEmpty()) {
				request = request.filterWith(new MethodFilter(testMethodNames));
			}
			Runner runner = request.getRunner();
			RunNotifier runNotifier = new RunNotifier();
			runNotifier.addListener(additionalListener);
			runNotifier.addListener(listener);
			runner.run(runNotifier);
		});
		try {
			submit.get(10000 * (testMethodNames.size() + 1), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			submit.cancel(true);
			executor.shutdownNow();
		}
		return listener;
	}

	@Override
	public TestListener run(Class<?> testClass, RunListener additionalListener) {
		return this.run(testClass, Collections.emptyList(), additionalListener);
	}
}
