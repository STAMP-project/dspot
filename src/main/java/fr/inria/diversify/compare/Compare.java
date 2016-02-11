package fr.inria.diversify.compare;


//import com.rits.cloning.Cloner;

import java.lang.reflect.Method;
import java.util.*;

/**
 * User: Simon
 * Date: 23/09/15
 * Time: 14:31
 */
public class Compare {
    protected static Compare singleton;

    protected boolean equals;
    protected Invocator invocator;
    protected MethodsHandler methodsHandler;
    protected int maxCompare;
    protected int nbSampleMethods;
    protected static Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();
//    protected Cloner cloner;


    public static Compare getSingleton() {
        if(singleton == null) {
            initCompare(false, true, true, 5, 10);
        }
        return singleton;
    }


    public static void initCompare(boolean equals, boolean onlyPublicMethod, boolean onlyNotStaticMethod, int maxCompare, int nbSampleMethods) {
        if(singleton == null) {
            singleton = new Compare(equals, onlyPublicMethod, onlyNotStaticMethod, maxCompare, nbSampleMethods);
        }
    }

    private Compare(boolean equals, boolean onlyPublicMethod, boolean onlyNotStaticMethod, int maxCompare, int nbSampleMethods) {
        this.equals = equals;
        this.invocator = new Invocator(10);
        this.methodsHandler = new MethodsHandler(onlyPublicMethod, onlyNotStaticMethod);
        this.maxCompare = maxCompare;
        this.nbSampleMethods = nbSampleMethods;
    }

    protected boolean filterB = false;
    public List<Boolean> buildFilter(List<Object> list1, List<Object> list2) {
        filterB = true;
        List<Boolean> filter = new ArrayList<Boolean>(list1.size());
        for(int i = 0; i < list1.size(); i++) {
            filter.add(compareAnything(list1.get(i), list2.get(i), maxCompare));
        }
        filterB = false;
        return filter;
    }

    public boolean compare(List<Object> list1, List<Object> list2, List<Boolean> filter) {
        for(int i = 0; i < list1.size(); i++) {
            if(filter.get(i)) {
                boolean result = compareAnything(list1.get(i), list2.get(i), maxCompare);
                if(!result) {
                    System.out.println(list1.get(i).getClass() + " " +list1.get(i) +"\n");
                    System.out.println(list2.get(i).getClass() + " " +list2.get(i));
                    return result;
                }
            }
        }
        return true;
    }

    protected boolean compareAnything(Object o1, Object o2, int nbCompare) {

        if(nbCompare <= 0) {
            return true;
        }
        if(o1 == o2) {
            return true;
        }
        if ((o1 == null && o2 != null) || (o1 != null && o2 == null)) {
            return false;
        }
        if(isWrapperType(o1) && isWrapperType(o2)) {
            return o1.equals(o2);
        }
        if (isString(o1) && isString(o2)) {
            return equalsString((String) o1, (String) o2);
        }
        if(isClass(o1) && isClass(o2)) {
            return equalsString(o1.toString(), o2.toString());
        }

        if(isCollection(o1) && isCollection(o2)) {
            return compareNotNullCollection((Collection<?>) o1, (Collection<?>) o2, nbCompare);
        }

        if(isIterator(o1) && isIterator(o2)) {
            return true;
            // return compareNotNullIterator(o1,o2,nbCompare);
        }

        if(isArray(o1) && isArray(o2)) {
            if(isPrimitiveArray(o1)) {
                return arrayPrimitiveEquals(o1, o2);
            } else {
                return compareNotNullArray((Object[]) o1, (Object[]) o2, nbCompare);
            }
        }

        return compareNotNullObject(o1, o2, nbCompare);
    }

    protected boolean equalsString(String o1, String o2) {
        return o1.split("@")[0].equals(o2.split("@")[0]);
    }

    protected boolean arrayPrimitiveEquals(Object o1, Object o2) {
        String type = o1.getClass().getCanonicalName();

        if(type.equals("byte[]")) {
            return Arrays.equals((byte[])o1, (byte[])o2);
        }
        if(type.equals("short[]")) {
            return Arrays.equals((short[])o1, (short[])o2);
        }
        if(type.equals("int[]")) {
            return Arrays.equals((int[])o1, (int[])o2);
        }
        if(type.equals("long[]")) {
            return Arrays.equals((long[])o1, (long[])o2);
        }
        if(type.equals("float[]")) {
            return Arrays.equals((float[])o1, (float[])o2);
        }
        if(type.equals("double[]")) {
            return Arrays.equals((double[])o1, (double[])o2);
        }
        if(type.equals("boolean[]")) {
            return Arrays.equals((boolean[])o1, (boolean[])o2);
        }
        if(type.equals("char[]")) {
            return Arrays.equals((char[])o1, (char[])o2);
        }

        return false;
    }

    protected boolean compareNotNullArray(Object[] array1, Object[] array2, int nbCompare) {
        if (array1.length != array2.length) {
            return false;
        } else {
            if (equals) {
                return Arrays.deepEquals(array1, array2);
            } else {
                for (int i = 0; i < array1.length; i++) {
                    if (!compareAnything(array1[i], array2[i], nbCompare - 1)) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    protected boolean compareNotNullCollection(Collection<?> collection1, Collection<?> collection2, int nbCompare) {
        int count = 0;
        boolean success;
        if (filterB) {
            success = true;
            while (count < 4 && success) {
                success = compareNotNullCollection0(collection1, collection2, nbCompare);
                count++;
            }
        } else {
            success = false;
            while (count < 8 && !success) {
                success = compareNotNullCollection0(collection1, collection2, nbCompare);
                count++;
            }
        }
        return success;
    }
    protected boolean compareNotNullCollection0(Collection<?> collection1, Collection<?> collection2, int nbCompare) {
        if (collection1.size() != collection2.size()) {
            return false;
        } else {
            if (equals) {
                return collection1.equals(collection2);
            } else {
                if (isList(collection1) && isList(collection2)) {
                    Iterator i1 = collection1.iterator();
                    Iterator i2 = collection2.iterator();

                    while (i1.hasNext()) {
                        if (!compareAnything(i1.next(), i2.next(), nbCompare -1)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return compareNotNullObject(collection1, collection2, nbCompare);
                }
            }
        }
    }


    protected boolean compareNotNullIterator(Object o1, Object o2, int nbCompare) {
        int count = 0;
        boolean success;
        if (filterB) {
            success = true;
            while (count < 4 && success) {
                success = compareNotNullObject(o1, o2, nbCompare);
                count++;
            }
        } else {
            success = false;
            while (count < 8 && !success) {
                success = compareNotNullObject(o1, o2, nbCompare);
                count++;
            }
        }
        return success;
    }

    protected boolean compareNotNullObject(Object o1, Object o2, int nbCompare) {
        for (Method method : methodsHandler.getAllMethods(o1)) {
//        for (Method method : methodsHandler.getRandomMethods(o1, nbSampleMethods)) {
            Invocation invocation1 = new Invocation(o1, method);
            Invocation invocation2 = new Invocation(o2, method);

            invocator.invoke(invocation1);
            invocator.invoke(invocation2);

            if (invocation1.sameStatus(invocation2)) {
                return compareAnything(invocation1.getResult(), invocation2.getResult(), nbCompare -1);
            } else {
                return invocation1.hasTimeOutError() || invocation2.hasTimeOutError();
            }
        }
        return true;
    }


    protected boolean isClass(Object o) {
        return o != null && o instanceof Class;
    }

    protected boolean isPrimitive(Object o) {
        return o != null && o.getClass().isPrimitive();
    }

    public static boolean isWrapperType(Object o) {
        return WRAPPER_TYPES.contains(o.getClass());
    }

    protected static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }

    protected boolean isArray(Object o) {
        return o != null && o.getClass().isArray();
    }

    protected boolean isPrimitiveArray(Object o) {
        String type = o.getClass().getCanonicalName();
        return type != null && isArray(o) &&
                (type.equals("byte[]")
                        || type.equals("short[]")
                        || type.equals("int[]")
                        || type.equals("long[]")
                        || type.equals("float[]")
                        || type.equals("double[]")
                        || type.equals("boolean[]")
                        || type.equals("char[]"));
    }

    protected boolean isCollection(Object o) {
        return o != null && o instanceof Collection;
    }

    protected boolean isIterator(Object o) {
        //  Iterable
        return o != null && o instanceof Iterator;
    }

    protected boolean isList(Object o) {
        return o != null && o instanceof List;
    }

    protected boolean isString(Object o) {
        return o != null && o.getClass().equals(String.class);
    }
}
