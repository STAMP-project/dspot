package fr.inria.diversify.compare;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/10/17
 */
public class UtilsTest {

    @Test//TODO empty collection / map are considered as primitive, we may need to found a semantic of the method.
    public void testIsPrimitiveCollectionOrMap() throws Exception {
        final ArrayList<Integer> list = new ArrayList<>();
        assertTrue(Utils.isPrimitiveCollectionOrMap(list));
        list.add(1);
        assertTrue(Utils.isPrimitiveCollectionOrMap(list));

        final HashMap<Integer, Integer> map = new HashMap<>();
        assertTrue(Utils.isPrimitiveCollectionOrMap(map));
        map.put(1,1);
        assertTrue(Utils.isPrimitiveCollectionOrMap(map));

        final HashSet<Integer> set = new HashSet<>();
        assertTrue(Utils.isPrimitiveCollectionOrMap(set));
        set.add(1);
        assertTrue(Utils.isPrimitiveCollectionOrMap(set));

        assertFalse(Utils.isPrimitiveCollectionOrMap(1));
    }

    @Test
    public void testIsPrimitiveMap() throws Exception {
        final ArrayList<Integer> list = new ArrayList<>();
        assertFalse(Utils.isPrimitiveMap(list));
        list.add(1);
        assertFalse(Utils.isPrimitiveMap(list));

        final HashMap<Integer, Integer> map = new HashMap<>();
        assertTrue(Utils.isPrimitiveMap(map));
        map.put(1,1);
        assertTrue(Utils.isPrimitiveMap(map));

        final HashSet<Integer> set = new HashSet<>();
        assertFalse(Utils.isPrimitiveMap(set));
        set.add(1);
        assertFalse(Utils.isPrimitiveMap(set));

        assertFalse(Utils.isPrimitiveMap(1));
    }

    @Test
    public void testIsPrimitiveCollection() throws Exception {
        final ArrayList<Integer> list = new ArrayList<>();
        assertTrue(Utils.isPrimitiveCollection(list));
        list.add(1);
        assertTrue(Utils.isPrimitiveCollection(list));

        final HashMap<Integer, Integer> map = new HashMap<>();
        assertFalse(Utils.isPrimitiveCollection(map));
        map.put(1,1);
        assertFalse(Utils.isPrimitiveCollection(map));

        final HashSet<Integer> set = new HashSet<>();
        assertTrue(Utils.isPrimitiveCollection(set));
        set.add(1);
        assertTrue(Utils.isPrimitiveCollection(set));

        assertFalse(Utils.isPrimitiveCollection(1));
    }

    @SuppressWarnings("all")
    @Test
    public void testIsPrimitive() throws Exception {
        assertTrue(Utils.isPrimitive((byte)1));
        assertTrue(Utils.isPrimitive(new Byte((byte)1)));
        assertTrue(Utils.isPrimitive((short)1));
        assertTrue(Utils.isPrimitive(new Short((short)1)));
        assertTrue(Utils.isPrimitive(1));
        assertTrue(Utils.isPrimitive(new Integer(1)));
        assertTrue(Utils.isPrimitive(new Long((long)1)));
        assertTrue(Utils.isPrimitive((long)1));
        assertTrue(Utils.isPrimitive(1.0F));
        assertTrue(Utils.isPrimitive(new Float(1.0F)));
        assertTrue(Utils.isPrimitive(1.0D));
        assertTrue(Utils.isPrimitive(new Double(1.0D)));
        assertTrue(Utils.isPrimitive('1'));
        assertTrue(Utils.isPrimitive(new Character('1')));
        assertTrue(Utils.isPrimitive(true));
        assertTrue(Utils.isPrimitive(new Boolean(true)));

        assertFalse(Utils.isPrimitive(null));
        assertFalse(Utils.isPrimitive(new Object()));
    }

    @Test
    public void isArray() throws Exception {
        assertTrue(Utils.isArray(new Object[]{}));
        assertTrue(Utils.isArray(new Integer[]{}));
        assertFalse(Utils.isArray(1));
    }


    @Test
    public void isPrimitiveArray() throws Exception {
        assertTrue(Utils.isPrimitiveArray(new byte[]{((byte)1)}));
        assertTrue(Utils.isPrimitiveArray(new short[]{((short)1)}));
        assertTrue(Utils.isPrimitiveArray(new int[]{(1)}));
        assertTrue(Utils.isPrimitiveArray(new long[]{((long)1)}));
        assertTrue(Utils.isPrimitiveArray(new float[]{(1.0F)}));
        assertTrue(Utils.isPrimitiveArray(new double[]{(1.0D)}));
        assertTrue(Utils.isPrimitiveArray(new char[]{('1')}));
        assertTrue(Utils.isPrimitiveArray(new boolean[]{(true)}));

        assertTrue(Utils.isPrimitiveArray(new Byte[]{((byte)1)}));
        assertTrue(Utils.isPrimitiveArray(new Short[]{((short)1)}));
        assertTrue(Utils.isPrimitiveArray(new Integer[]{(1)}));
        assertTrue(Utils.isPrimitiveArray(new Long[]{((long)1)}));
        assertTrue(Utils.isPrimitiveArray(new Float[]{(1.0F)}));
        assertTrue(Utils.isPrimitiveArray(new Double[]{(1.0D)}));
        assertTrue(Utils.isPrimitiveArray(new Character[]{('1')}));
        assertTrue(Utils.isPrimitiveArray(new Boolean[]{(true)}));

        assertFalse(Utils.isPrimitiveArray(new Object[]{}));
    }
}
