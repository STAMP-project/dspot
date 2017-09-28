package fr.inria.stamp.input;

import org.junit.Test;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/07/17
 */
public class ProjectTest {

	@Test
	public void test() throws Exception {
		Project project = new Project("src/test/resources/test-projects/test-projects.properties");
	}

}
