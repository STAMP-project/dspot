package fr.inria.stamp.test.runner;

import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.notification.RunListener;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static fr.inria.diversify.utils.AmplificationHelper.PATH_SEPARATOR;

@Deprecated
public class ReflectiveTestRunner extends AbstractTestRunner {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReflectiveTestRunner.class);

    ReflectiveTestRunner(URLClassLoader classLoader) {
        super(classLoader);
        DSpotUtils.copyPackageFromResources("fr/inria/stamp/test/listener/",
                "TestListener");
    }

    ReflectiveTestRunner(String classpath) {
        this((classpath + PATH_SEPARATOR + "target/dspot/dependencies/").split(System.getProperty("path.separator")));
    }

    ReflectiveTestRunner(String[] classpath) {
        DSpotUtils.copyPackageFromResources("fr/inria/stamp/test/listener/",
                "TestListener");
        this.classLoader = new URLClassLoader(Arrays.stream(classpath)
                .map(File::new)
                .map(File::toURI)
                .map(uri -> {
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new), null);
    }

    private Object runUsingReflection(Class<?> classTest) {
        try {
            Class<?> requestClass = this.loadClass("org.junit.runner.Request");
            Object request = requestClass.getMethod("aClass", Class.class)
                    .invoke(requestClass, classTest);
            Object runner = request.getClass()
                    .getMethod("getRunner")
                    .invoke(request);
            Object notifier = this.loadClass("org.junit.runner.notification.RunNotifier")
                    .newInstance();
            Class<?> listenerClass = this.loadClass("fr.inria.stamp.test.listener.TestListener");
            Object listenerInstance = listenerClass
                    .newInstance();
            notifier.getClass()
                    .getMethod("addFirstListener",
                            this.loadClass("org.junit.runner.notification.RunListener")
                    ).invoke(notifier, listenerInstance);
            runner.getClass()
                    .getMethod("run", notifier.getClass())
                    .invoke(runner, notifier);
            return listenerInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //TODO: implements a way to use additionalListeners
    @Override
    public TestListener run(Class<?> testClass, Collection<String> testMethodNames, RunListener... additionalListeners) {
        if (additionalListeners.length != 0) {
            LOGGER.warn("Additional listeners is not supported for ReflectiveTestRunner");
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<?> submit = executor.submit(() -> {
            Thread.currentThread().setContextClassLoader(classLoader);
            return runUsingReflection(testClass);
        });
        try {
            Object listener = submit.get(10000000 * (testMethodNames.size() + 1),
                    TimeUnit.MILLISECONDS);
            return TestListener.copyFromObject(listener, testMethodNames);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            submit.cancel(true);
            executor.shutdownNow();
        }
    }

}
