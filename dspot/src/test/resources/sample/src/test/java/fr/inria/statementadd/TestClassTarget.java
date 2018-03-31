package fr.inria.statementadd;

import org.junit.Test;

import java.util.ArrayList;


public class TestClassTarget {

	@Test
	public void test() throws Exception {
		ClassTarget clazz = new ClassTarget();
	}

	class Internal {
		public int compute(int i) {
			return (int) Math.pow(2, i);
		}
	}

	@Test
	public void testWithLoop() throws Exception {
		ArrayList<Internal> internalList = new ArrayList<>();
		internalList.add(new Internal());
		for (Internal i : internalList)
			i.compute(0);
	}
}