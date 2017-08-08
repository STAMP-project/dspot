package fr.inria.stamp.test.launcher;

import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.Test;
import spoon.Launcher;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/17
 */
public class TestLauncherTest {

	@Test
	public void test() throws Exception {

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
}
