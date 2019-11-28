package eu.stamp_project.dspot.common;

import eu.stamp_project.dspot.common.TypeUtils;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.factory.Factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/08/17
 */
public class TypeUtilsTest {

	@Test
	public void testIsString() {
		final Launcher launcher = new Launcher();
		launcher.buildModel();
		final Factory factory = launcher.getFactory();
		assertTrue(TypeUtils.isString(factory.createLiteral("").getType()));
		assertFalse(TypeUtils.isString(factory.createLiteral(1f).getType()));
		assertFalse(TypeUtils.isString(null));
	}

	@Test
	public void testIsPrimitiveCollection() throws Exception {

		/*
			Should be named isSupportedCollectionType
		 */

		assertTrue(TypeUtils.isPrimitiveCollection(new ArrayList<Integer>()));
		List<Integer> ints = new ArrayList<>();
		ints.add(1);
		assertTrue(TypeUtils.isPrimitiveCollection(ints));
		assertTrue(TypeUtils.isPrimitiveCollection(Collections.singletonList(1)));
		assertTrue(TypeUtils.isPrimitiveCollection(Collections.emptyList()));
		assertTrue(TypeUtils.isPrimitiveCollection(Collections.singletonList("String")));
		assertTrue(TypeUtils.isPrimitiveCollection(Collections.singleton(1)));
		assertFalse(TypeUtils.isPrimitiveCollection(new int[]{1}));
		assertFalse(TypeUtils.isPrimitiveCollection(Collections.singletonList(new Object())));
		assertFalse(TypeUtils.isPrimitiveCollection(Collections.emptyMap()));
	}

	@Test
	public void testIsPrimitiveMap() throws Exception {
		assertTrue(TypeUtils.isPrimitiveMap(Collections.singletonMap(1,1)));
		assertTrue(TypeUtils.isPrimitiveMap(Collections.emptyMap()));
		assertFalse(TypeUtils.isPrimitiveMap(Collections.emptyList()));
	}
}
