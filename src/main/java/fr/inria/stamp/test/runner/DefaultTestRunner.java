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

	private void runWithReset(Class<?> classTest, TestListener listener) {
		try {
			Request request = Request.aClass(classTest);
			Runner runner = request.getRunner();
			Class<?> aClass = classLoader.loadClass("mockit.internal.startup.InstrumentationHolder");
			Field hostJREClassName = aClass.getField("hostJREClassName");
			hostJREClassName.set(aClass, "NegativeArraySizeException");
			Class<?> startUp = classLoader.loadClass("mockit.internal.startup.Startup");
			Method initializeIfPossible = startUp.getMethod("initializeIfPossible");
			Boolean success = (Boolean)initializeIfPossible.invoke(startUp);
			System.out.println(success);
			request = Request.aClass(classTest);
			runner = request.getRunner();
			RunNotifier runNotifier = new RunNotifier();
			runNotifier.addFirstListener(listener);
			runner.run(runNotifier);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private TestListener runFullyReflection(Class<?> classTest) {
		try {
			Class<?> requestClass = classLoader.loadClass("org.junit.runner.Request");
			Object request = requestClass.getMethod("aClass", Class.class)
					.invoke(requestClass, classTest);
			Object runner = request.getClass()
					.getMethod("getRunner")
					.invoke(request);
			Object notifier = classLoader.loadClass("org.junit.runner.notification.RunNotifier")
					.newInstance();
			Class<?> listenerClass = classLoader.loadClass("fr.inria.stamp.test.listener.TestListener");
			Object listenerInstance = listenerClass
					.newInstance();
			notifier.getClass()
					.getMethod("addFirstListener",
							classLoader.loadClass("org.junit.runner.notification.RunListener")
					).invoke(notifier, listenerInstance);
			runner.getClass()
					.getMethod("run", notifier.getClass())
					.invoke(runner, notifier);
			return TestListener.copyFromObject(listenerInstance);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TestListener run(Class<?> classTest, Collection<String> testMethodNames) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		TestListener listener = new TestListener();
		final Future<?> submit = executor.submit(() -> {
			return runFullyReflection(classTest);
			/*Request request = Request.aClass(classTest);
			if (!testMethodNames.isEmpty()) {
				request = request.filterWith(new MethodFilter(testMethodNames));
			}
			Runner runner = request.getRunner();
			RunNotifier runNotifier = new RunNotifier();
			runNotifier.addFirstListener(listener);
			runner.run(runNotifier);*/
		});
		try {
			listener = (TestListener) submit.get(1000000000 * (testMethodNames.size() + 1), TimeUnit.MILLISECONDS);
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
