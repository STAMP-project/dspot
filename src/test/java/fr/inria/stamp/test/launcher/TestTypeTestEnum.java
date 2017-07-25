package fr.inria.stamp.test.launcher;

import org.junit.Test;
import spoon.Launcher;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/17
 */
public class TestTypeTestEnum {

	@Test
	public void test() throws Exception {
		Launcher launcher = new Launcher();
		launcher.getEnvironment().setNoClasspath(true);
		launcher.addInputResource("src/test/resources/test/");
		launcher.buildModel();
		assertEquals("MOCKITO", TypeTestEnum.getTypeTest(launcher.getFactory().Class().get("info.sanaulla.dal.BookDALTest")).name());
		assertEquals("DEFAULT", TypeTestEnum.getTypeTest(launcher.getFactory().Class().get("example.TestSuiteExample")).name());
	}
}
