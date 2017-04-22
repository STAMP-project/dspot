package fr.inria.diversify.dspot.testrunner;

import edu.emory.mathcs.backport.java.util.Collections;
import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AmplificationChecker;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.MavenAbstractTest;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.JunitRunnerMock;
import org.junit.Test;
import org.junit.runner.notification.Failure;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtNamedElement;

import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/04/17
 */
public class MockitoRunnerTest extends MavenAbstractTest {

    @Test
    public void testRunMockito() throws Exception {
        final InputConfiguration inputConfiguration = Utils.getInputConfiguration();
        final String classPath = AmplificationHelper.getClassPath(Utils.getCompiler(), Utils.getInputProgram());
        final JunitRunnerMock junitRunnerMock = new JunitRunnerMock(classPath, inputConfiguration);
        final CtClass<?> classTest = Utils.getFactory().Class().get("info.sanaulla.dal.BookDALTest");
        assertTrue(AmplificationChecker.isMocked(classTest));

        final JunitResult result = junitRunnerMock.run(Collections.singletonList(classTest),
                classTest.getMethods()
                        .stream()
                        .filter(AmplificationChecker::isTest)
                        .map(CtNamedElement::getSimpleName)
                        .collect(Collectors.toList())
        );
        assertEquals(5, result.getTestsRun().size());
        assertEquals(4, result.getPassingTests().size());
        assertEquals(1, result.getFailures().size());
        assertNull(result.getFailureOf("testGetAllBooks"));

        final String testGetAllBooksFailing = "testGetAllBooksFailing";
        final Failure failure = result.getFailureOf(testGetAllBooksFailing);
        assertNotNull(failure);
        assertEquals("info.sanaulla.dal.BookDALTest", failure.getDescription().getClassName());
        assertEquals("testGetAllBooksFailing(info.sanaulla.dal.BookDALTest)", failure.getDescription().getDisplayName());
        assertEquals("testGetAllBooksFailing", failure.getDescription().getMethodName());
        assertTrue(failure.getException() instanceof AssertionError);
    }

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/mockito/mockito.properties";
    }
}
