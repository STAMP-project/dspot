package example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/17
 */
public abstract class AbstractTest {

	protected String stringToBeTested = "Tanacetum";

	@Test
	public void abstractTest() throws Exception {
		example.Example example = new example.Example();
		assertEquals('T', example.charAt(this.stringToBeTested, 0));
	}

}
