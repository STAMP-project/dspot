
package fr.inria.sample;

import java.util.Comparator;

import org.junit.Test;

public class TestClassWithSpecificCaseToBeAsserted {

	@Test
	public void test1() {

		int a = 0;
		int b = 1;

		new Comparator<Integer>() {
			@Override
			public int compare(Integer integer, Integer t1) {
				return integer - t1;
			}
		}.compare(a, b);

	}
}
