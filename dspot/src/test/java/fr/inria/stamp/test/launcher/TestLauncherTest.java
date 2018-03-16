package fr.inria.stamp.test.launcher;

import fr.inria.Utils;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.test.listener.TestListener;
import fr.inria.stamp.test.runner.TestRunnerFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/17
 */
@Deprecated
public class TestLauncherTest {

	@Test
	public void testOnJMockit() throws Exception {

		/*
			Runner is able to run all kind of test
		 */

		try {
			FileUtils.deleteDirectory(new File("src/test/resources/jmockit/target/"));
		} catch (Exception ignored) {

		}

		Utils.init("src/test/resources/jmockit/mock.properties");

        final String classpath = Utils.getBuilder().buildClasspath(Utils.getInputProgram().getProgramDir())
        + System.getProperty("path.separator")
				+ Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getClassesDir()
				+ System.getProperty("path.separator")
				+ Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getTestClassesDir();

		final CtClass<?> jmockitTest = Utils.findClass("org.baeldung.mocks.jmockit.LoginControllerIntegrationTest");

		TestListener run = TestLauncher.run(Utils.getInputConfiguration(), classpath, jmockitTest);
		assertEquals(7, run.getRunningTests().size());
		assertEquals(7, run.getPassingTests().size());
		assertEquals(0, run.getFailingTests().size());
	}

	@Test
	public void testOnEasyMock() throws Exception {

		/*
			Runner is able to run all kind of test
		 */

		try {
			FileUtils.deleteDirectory(new File("src/test/resources/easymock/target/"));
		} catch (Exception ignored) {

		}

		Utils.init("src/test/resources/easymock/mock.properties");
		final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(Utils.getInputConfiguration())
				.buildClasspath(Utils.getInputProgram().getProgramDir())
				+ System.getProperty("path.separator")
				+ Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getClassesDir()
				+ System.getProperty("path.separator")
				+ Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getTestClassesDir();

		final CtClass<?> easyMockTest = Utils.findClass("org.baeldung.mocks.easymock.LoginControllerIntegrationTest");

		TestListener run = TestLauncher.run(Utils.getInputConfiguration(), classpath, easyMockTest);
		assertEquals(7, run.getRunningTests().size());
		assertEquals(7, run.getPassingTests().size());
		assertEquals(0, run.getFailingTests().size());
	}

	@Test
	public void testOnMockito() throws Exception {

		/*
			Runner is able to run all kind of test
		 */

		try {
			FileUtils.deleteDirectory(new File("src/test/resources/mockito/target/"));
		} catch (Exception ignored) {

		}

		Utils.init("src/test/resources/mockito/mockito.properties");
		final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(Utils.getInputConfiguration())
				.buildClasspath(Utils.getInputProgram().getProgramDir())
				+ System.getProperty("path.separator")
				+ Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getClassesDir()
				+ System.getProperty("path.separator")
				+ Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getTestClassesDir();

		final CtClass<?> mockitoTest = Utils.findClass("info.sanaulla.dal.BookDALTest");

		TestListener run = TestLauncher.run(Utils.getInputConfiguration(), classpath, mockitoTest);
		assertEquals(5, run.getRunningTests().size());
		assertEquals(4, run.getPassingTests().size());
		assertEquals(1, run.getFailingTests().size());
	}

	@Test
	public void testOnMockito2() throws Exception {

		/*
			Runner is able to run all kind of test
		 */

		try {
			FileUtils.deleteDirectory(new File("src/test/resources/mockito2/target/"));
		} catch (Exception ignored) {

		}

		Utils.init("src/test/resources/mockito2/mock.properties");
		final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(Utils.getInputConfiguration())
				.buildClasspath(Utils.getInputProgram().getProgramDir())
				+ System.getProperty("path.separator")
				+ Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getClassesDir()
				+ System.getProperty("path.separator")
				+ Utils.getInputProgram().getProgramDir() + "/" + Utils.getInputProgram().getTestClassesDir();

		final CtClass<?> mockitoTest = Utils.findClass("org.baeldung.mocks.mockito.LoginControllerIntegrationTest");

		TestListener run = TestLauncher.run(Utils.getInputConfiguration(), classpath, mockitoTest);
		assertEquals(7, run.getRunningTests().size());
		assertEquals(7, run.getPassingTests().size());
		assertEquals(0, run.getFailingTests().size());
	}

	@Test
	public void testRunningAllKindOfTests() throws Exception {

		Launcher launcher = new Launcher();
		launcher.getEnvironment().setSourceClasspath(
				System.getProperty("java.class.path").split(System.getProperty("path.separator"))
		);
		launcher.addInputResource("src/test/resources/src/");
		launcher.addInputResource("src/test/resources/test/");
		launcher.buildModel();

		final String classpath = "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar"
				+ System.getProperty("path.separator") +
				"src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar"
				+ System.getProperty("path.separator") +
				"src/test/resources/example-0.0.1-SNAPSHOT.jar"
				+ System.getProperty("path.separator") +
				"src/test/resources/example-0.0.1-SNAPSHOT-tests.jar";
		/*
			Test that we the same entry point we are able to run all kind of test
		 */
		InputConfiguration configuration = new InputConfiguration("src/test/resources/mockito/mockito.properties");
		configuration.setInputProgram(new InputProgram());

		// Mocked
		TestListener results = TestLauncher.run(configuration, classpath, launcher.getFactory().Class().get("info.sanaulla.dal.BookDALTest"));
		assertEquals(5, results.getRunningTests().size());
		assertEquals(1, results.getFailingTests().size());
		assertEquals(4, results.getPassingTests().size());
		assertEquals(0, results.getAssumptionFailingTests().size());
		assertEquals(0, results.getIgnoredTests().size());

		results = TestLauncher.run(configuration, classpath, launcher.getFactory().Class().get("info.sanaulla.dal.BookDALTest"),
				Collections.singleton("testGetBook"));
		assertEquals(1, results.getRunningTests().size());
		assertEquals(0, results.getFailingTests().size());
		assertEquals(1, results.getPassingTests().size());
		assertEquals(0, results.getAssumptionFailingTests().size());
		assertEquals(0, results.getIgnoredTests().size());

		configuration = new InputConfiguration("src/test/resources/test-projects/test-projects.properties");
		configuration.setInputProgram(new InputProgram());

		// Default
		results = TestLauncher.run(configuration, classpath, launcher.getFactory().Class().get("example.TestSuiteExample"),
				Collections.singleton("test1"));
		assertEquals(1, results.getRunningTests().size());
		assertEquals(1, results.getFailingTests().size());
		assertEquals(0, results.getPassingTests().size());
		assertEquals(0, results.getAssumptionFailingTests().size());
		assertEquals(0, results.getIgnoredTests().size());

		configuration = new InputConfiguration("src/test/resources/sample/sample.properties");
		configuration.setInputProgram(new InputProgram());

		// Abstract Class: using implementation of abstract class to run test
		results = TestLauncher.run(configuration, classpath, launcher.getFactory().Class().get("example.InheriteTest"));
		assertEquals(2, results.getRunningTests().size());
		assertEquals(0, results.getFailingTests().size());
		assertEquals(2, results.getPassingTests().size());
		assertEquals(0, results.getAssumptionFailingTests().size());
		assertEquals(0, results.getIgnoredTests().size());

		// will use example.InheriteTest to run the test of AbstractTest
		results = TestLauncher.run(configuration, classpath, launcher.getFactory().Class().get("example.AbstractTest"),
				Collections.singleton("abstractTest"));
		assertEquals(1, results.getRunningTests().size());
		assertEquals(0, results.getFailingTests().size());
		assertEquals(1, results.getPassingTests().size());
		assertEquals(0, results.getAssumptionFailingTests().size());
		assertEquals(0, results.getIgnoredTests().size());

		results = TestLauncher.run(configuration, classpath, launcher.getFactory().Class().get("example.AbstractTest"));
		assertEquals(2, results.getRunningTests().size());
		assertEquals(0, results.getFailingTests().size());
		assertEquals(2, results.getPassingTests().size());
		assertEquals(0, results.getAssumptionFailingTests().size());
		assertEquals(0, results.getIgnoredTests().size());
	}

	@Test
	public void testFromSpoonNodes() throws Exception {

		Launcher launcher = new Launcher();
		launcher.getEnvironment().setSourceClasspath(
				System.getProperty("java.class.path").split(System.getProperty("path.separator"))
		);
		launcher.addInputResource("src/test/resources/src/");
		launcher.addInputResource("src/test/resources/test/");
		launcher.buildModel();

		final String classpath = "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar"
				+ System.getProperty("path.separator") +
				"src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar"
				+ System.getProperty("path.separator") +
				"src/test/resources/example-0.0.1-SNAPSHOT.jar"
				+ System.getProperty("path.separator") +
				"src/test/resources/example-0.0.1-SNAPSHOT-tests.jar";
		/*
			Test that we the same entry point we are able to run all kind of test
		 */
		InputConfiguration configuration = new InputConfiguration("src/test/resources/mockito/mockito.properties");
		configuration.setInputProgram(new InputProgram());

		// Mocked
		TestListener results = TestLauncher.runFromSpoonNodes(configuration, classpath,
				launcher.getFactory().Class().get("info.sanaulla.dal.BookDALTest"),
				launcher.getFactory().Class().get("info.sanaulla.dal.BookDALTest").getMethodsByName("testGetBook")
		);
		assertEquals(1, results.getRunningTests().size());
		assertEquals(0, results.getFailingTests().size());
		assertEquals(1, results.getPassingTests().size());
		assertEquals(0, results.getAssumptionFailingTests().size());
		assertEquals(0, results.getIgnoredTests().size());

		configuration = new InputConfiguration("src/test/resources/test-projects/test-projects.properties");
		configuration.setInputProgram(new InputProgram());

		// Default
		results = TestLauncher.runFromSpoonNodes(configuration, classpath,
				launcher.getFactory().Class().get("example.TestSuiteExample"),
				launcher.getFactory().Class().get("example.TestSuiteExample").getMethodsByName("test1")
		);
		assertEquals(1, results.getRunningTests().size());
		assertEquals(1, results.getFailingTests().size());
		assertEquals(0, results.getPassingTests().size());
		assertEquals(0, results.getAssumptionFailingTests().size());
		assertEquals(0, results.getIgnoredTests().size());

		configuration = new InputConfiguration("src/test/resources/sample/sample.properties");
		configuration.setInputProgram(new InputProgram());

		// will use example.InheriteTest to run the test of AbstractTest
		results = TestLauncher.runFromSpoonNodes(configuration, classpath,
				launcher.getFactory().Class().get("example.AbstractTest"),
				launcher.getFactory().Class().get("example.AbstractTest").getMethodsByName("abstractTest"));
		assertEquals(1, results.getRunningTests().size());
		assertEquals(0, results.getFailingTests().size());
		assertEquals(1, results.getPassingTests().size());
		assertEquals(0, results.getAssumptionFailingTests().size());
		assertEquals(0, results.getIgnoredTests().size());
	}

	@Test
	public void testLauncherWithResources() throws Exception {
		Utils.init("src/test/resources/project-with-resources/project-with-resources.properties");
		final CtClass aClass = Utils.findClass("resolver.ClasspathResolverTest");
		final String classPath = AmplificationHelper.getClassPath(Utils.getCompiler(), Utils.getInputConfiguration());
		final TestListener run = TestLauncher.run(Utils.getInputConfiguration(), classPath, aClass);
		assertEquals(10, run.getPassingTests().size());
		assertEquals(10, run.getRunningTests().size());
		assertTrue(run.getFailingTests().isEmpty());
	}

	@Test
	public void testLauncherWithResourcesInsideTheSourcesFolders() throws Exception {
		Utils.init("src/test/resources/project-with-resources/project-with-resources.properties");
		final CtClass aClass = Utils.findClass("textresources.in.sources.TestResourcesInSources");
		final String classPath = AmplificationHelper.getClassPath(Utils.getCompiler(), Utils.getInputConfiguration());
		final TestListener run = TestLauncher.run(Utils.getInputConfiguration(), classPath, aClass);
		assertEquals(1, run.getPassingTests().size());
		assertEquals(1, run.getRunningTests().size());
		assertEquals(0, run.getFailingTests().size());
		assertTrue(run.getFailingTests().isEmpty());
	}

	@Test
	public void testTimeoutTuning() throws Exception {
		/*
			Contract: DSpot is able to tune the timeout of the test runner.
				We test that with a small time (0), DSpot throws a TimeoutException exception.
				We test that with a enough large time (10000, default value) DSpot runs the test correctly.
		 */
		Utils.init("src/test/resources/sample/sample.properties");
		AmplificationHelper.setTimeOutInMs(0);
		final CtClass aClass = Utils.findClass("fr.inria.systemproperties.SystemPropertiesTest");
		final String classPath = AmplificationHelper.getClassPath(Utils.getCompiler(), Utils.getInputConfiguration());
		try {
			TestLauncher.run(Utils.getInputConfiguration(), classPath, aClass);
			fail("testTimeoutTuning should have thrown a java.util.concurrent.TimeoutException");
		}  catch (Exception e) {
			assertTrue(e instanceof java.lang.RuntimeException);
			assertTrue(e.getCause() instanceof java.util.concurrent.TimeoutException);
		}
		AmplificationHelper.setTimeOutInMs(10000);
		final TestListener run  = TestLauncher.run(Utils.getInputConfiguration(), classPath, aClass);
		assertEquals(1, run.getPassingTests().size());
		assertEquals(1, run.getRunningTests().size());
		assertEquals(0, run.getFailingTests().size());
		assertTrue(run.getFailingTests().isEmpty());
	}


	@Test
	public void testLauncherOnTestThatUseSystemProperties() throws Exception {

		/*
			Contract: DSpot is able to run a test that use System Properties.
				System Properties must be described in the properties file given as input.
				System Properties must be described with the key systemProperties (i.e. systemProperties=...)
				System Properties must be a couple of key and value, separated by an equals '=' (e.g. key=value)
				System Properties must be separated by a comma ',' (e.g. key1=value1,key2=value2)

		 */

		Utils.init("src/test/resources/sample/sample.properties");
		final CtClass aClass = Utils.findClass("fr.inria.systemproperties.SystemPropertiesTest");
		final String classPath = AmplificationHelper.getClassPath(Utils.getCompiler(), Utils.getInputConfiguration());
		final TestListener run = TestLauncher.run(Utils.getInputConfiguration(), classPath, aClass);
		assertEquals(1, run.getPassingTests().size());
		assertEquals(1, run.getRunningTests().size());
		assertEquals(0, run.getFailingTests().size());
		assertTrue(run.getFailingTests().isEmpty());
	}

	@Test
	public void testLauncherOnTestUsingReflectiveTestRunnerOnTestThatUseSystemProperty() throws Exception {

		/*
			Using the ReflectiveTestRunner
		 */
		TestRunnerFactory.useReflectiveTestRunner = true;
		Utils.init("src/test/resources/sample/sample.properties");
		final CtClass aClass = Utils.findClass("fr.inria.systemproperties.SystemPropertiesTest");
		final String classPath = AmplificationHelper.getClassPath(Utils.getCompiler(), Utils.getInputConfiguration());
		final TestListener run = TestLauncher.run(Utils.getInputConfiguration(), classPath, aClass);
		assertEquals(1, run.getPassingTests().size());
		assertEquals(1, run.getRunningTests().size());
		assertEquals(0, run.getFailingTests().size());
		assertTrue(run.getFailingTests().isEmpty());
		TestRunnerFactory.useReflectiveTestRunner = false;
	}
}
