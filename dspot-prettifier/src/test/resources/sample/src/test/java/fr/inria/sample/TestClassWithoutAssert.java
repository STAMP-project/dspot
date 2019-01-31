package fr.inria.sample;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 13:39
 */
public class TestClassWithoutAssert {

    @Test
    public void test1() {
        ClassWithBoolean cl = new ClassWithBoolean();
        cl.getFalse();
        cl.getBoolean();
        java.io.File file = new java.io.File("");
        boolean var = cl.getTrue();
    }

    @Test
    public void test2() {
        ClassWithBoolean cl = new ClassWithBoolean();
        cl.getFalse();
        cl.getFalse();
        cl.getFalse();
    }

    @Test
    public void test3() throws Exception {
        final fr.inria.sample.ClassWithMap classWithMap = new fr.inria.sample.ClassWithMap();
        classWithMap.getFullMap();
    }

    @Test
    public void testOnClass() throws Exception {
        class MyClass {
            public Class<?> typeClass() {
                return MyClass.class;
            }
        }
        MyClass clazz = new MyClass();
        assertEquals(MyClass.class, clazz.typeClass());
    }
}
