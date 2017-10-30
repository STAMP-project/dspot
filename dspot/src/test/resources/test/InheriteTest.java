package example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/17
 */
public class InheriteTest extends AbstractTest {

	@Test
	public void test() throws Exception {
		example.Example example = new example.Example();
		assertEquals('T', example.charAt(this.stringToBeTested, 0));
	}
}
