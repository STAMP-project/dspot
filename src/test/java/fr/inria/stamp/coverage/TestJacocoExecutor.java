package fr.inria.stamp.coverage;

import edu.emory.mathcs.backport.java.util.Arrays;
import fr.inria.diversify.Utils;
import org.junit.Test;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/17
 */
public class TestJacocoExecutor {

	@Test
	public void name() throws Exception {
		Utils.init("src/test/resources/test-projects/test-projects.properties");
		JacocoExecutor jacocoExecutor = new JacocoExecutor(Utils.getInputProgram());
		final List<String> methodNames = Arrays.asList(new String[] {"test2", "test3", "test4"});
		System.out.println(jacocoExecutor.executeJacoco("example.TestSuiteExample", methodNames));
	}
}