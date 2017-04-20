package fr.inria.diversify.testRunner;


import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.dspot.AmplificationChecker;
import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.util.Log;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import spoon.reflect.declaration.CtType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static fr.inria.diversify.dspot.DSpotUtils.buildMavenHome;

/**
 * User: Simon
 * Date: 15/10/15
 * Time: 19:43
 */
public class JunitRunner {

    private final InputConfiguration configuration;
    private int classTimeOut = 120;
    private int methodTimeOut = 5;

    private ClassLoader classLoader;

    public JunitRunner(String classpath, InputConfiguration configuration) {
        this.configuration = configuration;
        final List<URL> tmp = Arrays.stream(classpath.split(System.getProperty("path.separator")))
                .map(File::new)
                .collect(ArrayList<URL>::new,
                        (urls, file) -> {
                            try {
                                urls.add(file.toURI().toURL());
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        ArrayList<URL>::addAll);
        classLoader = new URLClassLoader(tmp.toArray(new URL[tmp.size()]));
    }

    public JunitResult runTestClass(CtType<?> test, List<String> methodsToRun) {
        return runTestClasses(Collections.singletonList(test), methodsToRun);
    }

    public JunitResult runTestClasses(List<CtType<?>> tests, List<String> methodsToRun) {
        JunitResult result = new JunitResult();
        try {
            if (AmplificationChecker.isMocked(tests)) {
                runMockito(result, tests.get(0), methodsToRun);
            } else {
                Class<?>[] testClasses = loadClass(tests);
                int timeOut = computeTimeOut(methodsToRun);
                runRequest(result, buildRequest(testClasses, methodsToRun), timeOut);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.warn("Time out for running unit test cases");
        }
        return result;
    }

    private void runMockito(JunitResult result, CtType<?> test, List<String> methodsToRun) {
        try {
            MavenBuilder builder = new MavenBuilder(configuration.getInputProgram().getProgramDir());
            builder.setBuilderPath(buildMavenHome(configuration));
            builder.runGoals(new String[]{"-Dtest=" + buildTestCaseName(test.getQualifiedName(), methodsToRun), "test"}, false);
            final String pathFile = configuration.getInputProgram().getProgramDir() + "target/surefire-reports/TEST-" + test.getQualifiedName() + ".xml";
            readSurefireReports(test, pathFile, result);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String buildTestCaseName(final String fullQualifiedNameTest, List<String> methodsToRun) {
        return fullQualifiedNameTest + "#" + methodsToRun.stream()
                .collect(Collectors.joining("+"));
    }

    private void readSurefireReports(CtType<?> test, String pathFile, JunitResult result) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(pathFile);
            Node currentNode = getNextTestCase(doc.getFirstChild().getFirstChild());
            while (currentNode != null) {
                final String currentNameTest = currentNode.getAttributes().getNamedItem("name").getNodeValue();
                result.addTestRun(currentNameTest);
                if (currentNode.getFirstChild() != null
                        && currentNode.getFirstChild().getNextSibling().getNodeName().equals("failure")) {
                    result.addTestFail(buildFailure(test, currentNameTest, currentNode.getFirstChild().getNextSibling()));
                }
                currentNode = getNextTestCase(currentNode);
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private Failure buildFailure(CtType<?> test, String currentNameTest, Node nodeFailure) {
        Throwable throwable = null;
        Description description = null;
        try {
            String failureException = nodeFailure.getAttributes().getNamedItem("type").getNodeValue();
            failureException = failureException.endsWith(":") ?
                    failureException.substring(0, failureException.length() - 1) :
                    failureException;
            final Class<?> classThrowable = classLoader.loadClass(failureException);
            throwable = (Throwable) classThrowable.newInstance();
            description = Description.createTestDescription(
                    classLoader.loadClass(test.getQualifiedName()), currentNameTest
            );
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return new Failure(description, throwable);
    }

    private Node getNextTestCase(Node currentNode) {
        Node nextTestCase = currentNode.getNextSibling();
        while (nextTestCase != null && !nextTestCase.getNodeName().equals("testcase")) {
            nextTestCase = nextTestCase.getNextSibling();
        }
        return nextTestCase;
    }


    private int computeTimeOut(List<String> methodsToRun) {
        if (methodsToRun.isEmpty()) {
            return classTimeOut;
        } else {
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

    private Class<?>[] loadClass(List<CtType<?>> tests) throws ClassNotFoundException {
        Class<?>[] testClasses = new Class<?>[tests.size()];
        for (int i = 0; i < tests.size(); i++) {
            try {
                testClasses[i] = classLoader.loadClass(tests.get(i).getQualifiedName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
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

}
