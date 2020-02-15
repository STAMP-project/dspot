package eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/10/17
 */
public class ObjectLogUtilsTest {

    public class MyList extends ArrayList {

    }

    @Test//TODO empty collection / map are considered as primitive, we may need to found a semantic of the method.
    public void testIsPrimitiveCollectionOrMap() throws Exception {

        MyList mylist = new MyList();
        mylist.add("");
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveCollectionOrMap(mylist));

        final ArrayList<Integer> list = new ArrayList<>();
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveCollectionOrMap(list));
        list.add(1);
        assertTrue(ObjectLogUtils.isNonEmptyPrimitiveCollectionOrMap(list));

        final HashMap<Integer, Integer> map = new HashMap<>();
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveCollectionOrMap(map));
        map.put(1,1);
        assertTrue(ObjectLogUtils.isNonEmptyPrimitiveCollectionOrMap(map));

        final HashSet<Integer> set = new HashSet<>();
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveCollectionOrMap(set));
        set.add(1);
        assertTrue(ObjectLogUtils.isNonEmptyPrimitiveCollectionOrMap(set));

        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveCollectionOrMap(1));

    }

    @Test
    public void testIsPrimitiveMap() throws Exception {
        final ArrayList<Integer> list = new ArrayList<>();
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveMap(list));
        list.add(1);
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveMap(list));

        final HashMap<Integer, Integer> map = new HashMap<>();
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveMap(map));
        map.put(1,1);
        assertTrue(ObjectLogUtils.isNonEmptyPrimitiveMap(map));

        final HashSet<Integer> set = new HashSet<>();
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveMap(set));
        set.add(1);
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveMap(set));

        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveMap(1));
    }

    @Test
    public void testIsPrimitiveCollection() throws Exception {
        final ArrayList<Integer> list = new ArrayList<>();
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveCollection(list));
        list.add(1);
        assertTrue(ObjectLogUtils.isNonEmptyPrimitiveCollection(list));

        final HashMap<Integer, Integer> map = new HashMap<>();
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveCollection(map));
        map.put(1,1);
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveCollection(map));

        final HashSet<Integer> set = new HashSet<>();
        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveCollection(set));
        set.add(1);
        assertTrue(ObjectLogUtils.isNonEmptyPrimitiveCollection(set));

        assertFalse(ObjectLogUtils.isNonEmptyPrimitiveCollection(1));
    }

    @SuppressWarnings("all")
    @Test
    public void testIsPrimitive() throws Exception {
        assertTrue(ObjectLogUtils.isPrimitive((byte)1));
        assertTrue(ObjectLogUtils.isPrimitive(new Byte((byte)1)));
        assertTrue(ObjectLogUtils.isPrimitive((short)1));
        assertTrue(ObjectLogUtils.isPrimitive(new Short((short)1)));
        assertTrue(ObjectLogUtils.isPrimitive(1));
        assertTrue(ObjectLogUtils.isPrimitive(new Integer(1)));
        assertTrue(ObjectLogUtils.isPrimitive(new Long((long)1)));
        assertTrue(ObjectLogUtils.isPrimitive((long)1));
        assertTrue(ObjectLogUtils.isPrimitive(1.0F));
        assertTrue(ObjectLogUtils.isPrimitive(new Float(1.0F)));
        assertTrue(ObjectLogUtils.isPrimitive(1.0D));
        assertTrue(ObjectLogUtils.isPrimitive(new Double(1.0D)));
        assertTrue(ObjectLogUtils.isPrimitive('1'));
        assertTrue(ObjectLogUtils.isPrimitive(new Character('1')));
        assertTrue(ObjectLogUtils.isPrimitive(true));
        assertTrue(ObjectLogUtils.isPrimitive(new Boolean(true)));

        assertFalse(ObjectLogUtils.isPrimitive(null));
        assertFalse(ObjectLogUtils.isPrimitive(new Object()));
    }

    @Test
    public void isArray() throws Exception {
        assertTrue(ObjectLogUtils.isArray(new Object[]{}));
        assertTrue(ObjectLogUtils.isArray(new Integer[]{}));
        assertFalse(ObjectLogUtils.isArray(1));
    }


    @Test
    public void isPrimitiveArray() throws Exception {
        assertTrue(ObjectLogUtils.isPrimitiveArray(new byte[]{((byte)1)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new short[]{((short)1)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new int[]{(1)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new long[]{((long)1)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new float[]{(1.0F)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new double[]{(1.0D)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new char[]{('1')}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new boolean[]{(true)}));

        assertTrue(ObjectLogUtils.isPrimitiveArray(new Byte[]{((byte)1)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new Short[]{((short)1)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new Integer[]{(1)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new Long[]{((long)1)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new Float[]{(1.0F)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new Double[]{(1.0D)}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new Character[]{('1')}));
        assertTrue(ObjectLogUtils.isPrimitiveArray(new Boolean[]{(true)}));

        assertFalse(ObjectLogUtils.isPrimitiveArray(new Object[]{}));
    }
}
