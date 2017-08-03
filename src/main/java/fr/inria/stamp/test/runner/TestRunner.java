package fr.inria.stamp.test.runner;

import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.notification.RunListener;

import java.util.Collection;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public interface TestRunner {

    TestListener run(String fullQualifiedName, Collection<String> testMethodNames);

    TestListener run(String fullQualifiedName, String testMethodName);

    TestListener run(String fullQualifiedName);

    TestListener run(Class<?> classTest, Collection<String> testMethodNames);

    TestListener run(Class<?> classTest, String testMethodName);

    TestListener run(Class<?> classTest);

    TestListener run(Class<?> testClass, RunListener listener);

    TestListener run(Class<?> testClass, Collection<String> methodNames, RunListener listener);

    TestListener run(String fullQualifiedName, RunListener listener);

    TestListener run(String fullQualifiedName, Collection<String> methodNames, RunListener listener);

}
