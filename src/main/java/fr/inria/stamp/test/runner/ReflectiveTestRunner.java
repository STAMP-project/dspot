package fr.inria.stamp.test.runner;

import fr.inria.diversify.logger.Logger;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.notification.RunListener;

import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ReflectiveTestRunner extends AbstractTestRunner {

    public ReflectiveTestRunner(URLClassLoader classLoader) {
        super(classLoader);
    }

    public ReflectiveTestRunner(String classpath) {
        super(classpath);
    }

    public ReflectiveTestRunner(String[] classpath) {
        super(classpath);
    }

    private TestListener runUsingReflection(Class<?> classTest) {
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
        final Future<?> submit = executor.submit(() ->
            runUsingReflection(classTest)
        );
        try {
            Object listener = submit.get(10000 * (testMethodNames.size() + 1),
                    TimeUnit.MILLISECONDS);
            return TestListener.copyFromObject(listener);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            submit.cancel(true);
            executor.shutdownNow();
            Logger.stopLogging();
            Logger.close();
        }
    }

    @Override
    public TestListener run(Class<?> classTest) {
        return this.run(classTest, Collections.emptyList());
    }

    @Override
    public TestListener run(Class<?> testClass, Collection<String> testMethodNames, RunListener additionalListener) {
        throw new UnsupportedOperationException("Can not load additionnal listener from custom classloader: must be implemented"); // TODO
    }

    @Override
    public TestListener run(Class<?> testClass, RunListener additionalListener) {
        return this.run(testClass, Collections.emptyList(), additionalListener);
    }
}
