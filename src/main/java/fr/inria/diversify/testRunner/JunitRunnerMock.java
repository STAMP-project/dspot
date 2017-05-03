package fr.inria.diversify.testRunner;

import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.util.Log;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import spoon.reflect.declaration.CtType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static fr.inria.diversify.dspot.DSpotUtils.buildMavenHome;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/04/17
 */
public class JunitRunnerMock extends JunitRunner {

    private final InputConfiguration configuration;

    public JunitRunnerMock(String classpath, InputConfiguration configuration) {
        super(classpath);
        this.configuration = configuration;

    }

    public JunitResult run(List<CtType<?>> tests, List<String> methodsToRun) {
        JunitResult result = new JunitResult();
        try {
            runMockito(result, tests.get(0), methodsToRun);
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
            final String pathFile = configuration.getInputProgram().getProgramDir() + "/target/surefire-reports/TEST-" + test.getQualifiedName() + ".xml";
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
                        && (currentNode.getFirstChild().getNextSibling().getNodeName().equals("failure")
                        || currentNode.getFirstChild().getNextSibling().getNodeName().equals("error"))) {
                    Failure failure = buildFailure(test, currentNameTest, currentNode.getFirstChild().getNextSibling());
                    if (failure != null)
                        result.addTestFail(failure);
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
        String failureException = nodeFailure.getAttributes().getNamedItem("type").getNodeValue();
        failureException = failureException.endsWith(":") ?
                failureException.substring(0, failureException.length() - 1) :
                failureException;
        try {
            final Class<?> classThrowable = classLoader.loadClass(failureException);
            throwable = (Throwable) classThrowable.newInstance();
            description = Description.createTestDescription(
                    classLoader.loadClass(test.getQualifiedName()), currentNameTest
            );
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            return null;
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

}
