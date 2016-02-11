package fr.inria.diversify.testRunner;


import fr.inria.diversify.profiling.logger.Logger;
import fr.inria.diversify.runner.InputProgram;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.*;
import org.junit.runner.notification.RunNotifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: Simon
 * Date: 15/10/15
 * Time: 19:43
 */
public class JunitRunner {
    protected ClassLoader classLoader;
    protected InputProgram inputProgram;
    protected int classTimeOut = 120;
    protected int methodTimeOut = 5;
    protected static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();

    public JunitRunner(InputProgram inputProgram, ClassLoader classLoader) {
        this.inputProgram = inputProgram;
        this.classLoader = classLoader;
    }

    public JunitResult runTestClass(String test, List<String> methodsToRun) {
        List<String> list = new ArrayList<>(1);
        list.add(test);
        return runTestClasses(list, methodsToRun);
    }

    public JunitResult runTestClasses(List<String> tests) {
       return runTestClasses(tests, new ArrayList<>(0));
    }

    public JunitResult runTestClasses(List<String> tests, List<String> methodsToRun) {
        JunitResult result = new JunitResult();
        try {
            Class<?>[] testClasses = loadClass(tests);
            Logger.reset();
            Logger.setLogDir(new File(inputProgram.getProgramDir() + "/log"));

            int timeOut = computeTimeOut(methodsToRun);

            runRequest(result, buildRequest(testClasses, methodsToRun), timeOut);
        } catch (Exception e) {
//            Log.error("error in JunitRunner", e);
        }
        Logger.close();
        return result;
    }

    protected int computeTimeOut(List<String> methodsToRun) {
        if(methodsToRun.isEmpty()) {
            return classTimeOut;
        } else {
            return Math.min(methodsToRun.size() * methodTimeOut, classTimeOut);
        }
    }

    protected Request buildRequest(Class<?>[] testClasses, List<String> methodsToRun) {
        Request classesRequest = Request.classes(new Computer(), testClasses);
       //Request.runner((new Computer()).getSuite(new JUnit4Builder(),testClasses))
        if(methodsToRun.isEmpty()) {
            return classesRequest;
        } else {
            return new FilterRequest(classesRequest, new MethodFilter(methodsToRun));
        }
    }

    protected void runRequest(final JunitResult result, Request request, int timeOut) throws InterruptedException, ExecutionException, TimeoutException {
        timedCall(new Runnable() {
            public void run() {
                Runner runner = request.getRunner();
                RunNotifier fNotifier = new RunNotifier();
                fNotifier.addFirstListener(result);

                fNotifier.fireTestRunStarted(runner.getDescription());
                runner.run(fNotifier);
//                fNotifier.fireTestRunFinished(result);
            }
        }, timeOut, TimeUnit.SECONDS);
    }

    protected Class<?>[] loadClass(List<String> tests) throws ClassNotFoundException {
        Class<?>[] testClasses = new Class<?>[tests.size()];
        for(int i = 0; i < tests.size(); i++) {
            testClasses[i] = classLoader.loadClass(tests.get(i));
        }
        return testClasses;
    }

    protected void timedCall(Runnable runnable, long timeout, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask task = new FutureTask(runnable, null);
        try {
            THREAD_POOL.execute(task);
            task.get(timeout, timeUnit);
        }  finally {
            task.cancel(true);
        }
    }
}