package fr.inria.diversify.testRunner;


import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.util.Log;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunNotifier;
import spoon.reflect.declaration.CtType;

import java.util.*;
import java.util.concurrent.*;


/**
 * User: Simon
 * Date: 15/10/15
 * Time: 19:43
 */
public class DefaultJunitRunner extends JunitRunner {



    public DefaultJunitRunner(String classpath) {
        super(classpath);
    }

    public JunitResult run(List<CtType<?>> tests, List<String> methodsToRun) {
        JunitResult result = new JunitResult();
        try {
            Class<?>[] testClasses = loadClass(tests);
            int timeOut = computeTimeOut(methodsToRun);
            runRequest(result, buildRequest(testClasses, methodsToRun), timeOut);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.warn("Time out for running unit test cases");
        }
        return result;
    }


    private int computeTimeOut(List<String> methodsToRun) {
        int classTimeOut = 120;
        if (methodsToRun.isEmpty()) {
            return classTimeOut;
        } else {
            int methodTimeOut = 5;
            return Math.max(methodsToRun.size() * methodTimeOut, classTimeOut);
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
        timedCall(() -> {
            Runner runner = request.getRunner();
            RunNotifier fNotifier = new RunNotifier();
            fNotifier.addFirstListener(result);
            fNotifier.fireTestRunStarted(runner.getDescription());
            runner.run(fNotifier);
        }, timeOut, TimeUnit.SECONDS);
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

}
