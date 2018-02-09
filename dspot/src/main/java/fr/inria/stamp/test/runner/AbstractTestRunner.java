package fr.inria.stamp.test.runner;

import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 */
@Deprecated
public abstract class AbstractTestRunner implements TestRunner {

    protected URLClassLoader classLoader;

    AbstractTestRunner() {

    }

    AbstractTestRunner(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    AbstractTestRunner(String classpath) {
        this(classpath.split(System.getProperty("path.separator")));
    }

    AbstractTestRunner(String[] classpath) {
        this.classLoader = new URLClassLoader(Arrays.stream(classpath)
                .map(File::new)
                .map(File::toURI)
                .map(uri -> {
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new), ClassLoader.getSystemClassLoader());
    }

    Class<?> loadClass(String fullQualifiedName) {
        try {
            return this.classLoader.loadClass(fullQualifiedName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TestListener run(String fullQualifiedName, String testMethodName) {
        return this.run(this.loadClass(fullQualifiedName), Collections.singleton(testMethodName));
    }

    @Override
    public TestListener run(String fullQualifiedName, Collection<String> testMethodNames) {
        return this.run(this.loadClass(fullQualifiedName), testMethodNames);
    }

    @Override
    public TestListener run(String fullQualifiedName, RunListener... listeners) {
        return this.run(this.loadClass(fullQualifiedName), listeners);
    }

    @Override
    public TestListener run(String fullQualifiedName, Collection<String> methodNames, RunListener... listeners) {
        return this.run(this.loadClass(fullQualifiedName), methodNames, listeners);
    }

    @Override
    public TestListener run(Class<?> testClass, String testMethodName) {
        return this.run(testClass, Collections.singleton(testMethodName));
    }

    @Override
    public TestListener run(Class<?> testClass, RunListener... listeners) {
        return this.run(testClass, Collections.emptyList(), listeners);
    }
}
