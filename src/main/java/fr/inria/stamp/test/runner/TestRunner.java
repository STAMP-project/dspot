package fr.inria.stamp.test.runner;

import fr.inria.stamp.coverage.JacocoListener;
import fr.inria.stamp.test.listener.TestListener;

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

    TestListener run(Class<?> testClass, Collection<String> methodNames, JacocoListener jacocoListener);

}
