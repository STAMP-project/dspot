package fr.inria.diversify.testRunner;


import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.util.Log;
import org.apache.maven.plugin.lifecycle.Execution;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.*;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunNotifier;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.*;

/**
 * User: Simon
 * Date: 15/10/15
 * Time: 19:43
 */
public class JunitRunner {

    private ClassLoader classLoader;
    private int classTimeOut = 120;
    private int methodTimeOut = 5;

    public JunitRunner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public JunitResult runTestClass(String test, List<String> methodsToRun) {
        return runTestClasses(Collections.singletonList(test), methodsToRun);
    }

    public JunitResult runTestClasses(List<String> tests, List<String> methodsToRun) {
        JunitResult result = new JunitResult();
        try {
            Class<?>[] testClasses = loadClass(tests);
            int timeOut = computeTimeOut(methodsToRun);
            runRequest(result, buildRequest(testClasses, methodsToRun), timeOut);
        } catch (Throwable e) {
            Log.warn("Time out for running unit test cases");
        }
        return result;
    }

    private int computeTimeOut(List<String> methodsToRun) {
        if (methodsToRun.isEmpty()) {
            return classTimeOut;
        } else {
            return Math.min(methodsToRun.size() * methodTimeOut, classTimeOut);
        }
    }

    private Request buildRequest(Class<?>[] testClasses, List<String> methodsToRun) {
        Request classesRequest = Request.classes(new Computer(), testClasses);
        if (methodsToRun.isEmpty()) {
            return classesRequest;
        } else {
            return new FilterRequest(classesRequest, new Filter() {
                @Override
                public boolean shouldRun(Description description) {
                    if (description.isTest()) {
                        return methodsToRun.contains(description.getMethodName());
                    }
                    // explicitly check if any children want to run
                    for (Description each : description.getChildren()) {
                        if (shouldRun(each)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public String describe() {
                    return "MethodFilter";
                }
            }
            );
        }
    }

    private void runRequest(final JunitResult result, Request request, int timeOut) throws InterruptedException, ExecutionException, TimeoutException {
        timedCall(new Runnable() {
            public void run() {
                Runner runner = request.getRunner();
                RunNotifier fNotifier = new RunNotifier();
                fNotifier.addFirstListener(result);
                fNotifier.fireTestRunStarted(runner.getDescription());
                runner.run(fNotifier);
            }
        }, timeOut, TimeUnit.SECONDS);
    }

    private Class<?>[] loadClass(List<String> tests) throws ClassNotFoundException {
        Class<?>[] testClasses = new Class<?>[tests.size()];
        for (int i = 0; i < tests.size(); i++) {
            testClasses[i] = classLoader.loadClass(tests.get(i));
        }
        return testClasses;
    }

    private void timedCall(Runnable runnable, long timeout, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(runnable);
        try {
             future.get(timeout, timeUnit);
        } finally {
            future.cancel(true);
            executor.shutdownNow();
            Logger.stopLogging();
            Logger.close();
        }
    }

    public void setMethodTimeOut(int methodTimeOut) {
        this.methodTimeOut = methodTimeOut;
    }

    public void setClassTimeOut(int classTimeOut) {
        this.classTimeOut = classTimeOut;
    }
}
