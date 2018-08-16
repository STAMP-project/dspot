package eu.stamp_project.compare;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/08/18
 */
public class MethodsHandlerTest {

    class MyClassWithImplementedToStringAndHashCode {
        @Override
        public int hashCode() {
            return 23;
        }

        @Override
        public String toString() {
            return "MyClassWithImplementedToStringAndHashCode{}";
        }
    }
    class MyClassWithoutImplementedToStringAndHashCode{
        public int getInteger() {
            return 23;
        }
        public boolean shouldBe() {
            return true;
        }
        public boolean hasToBe() {
            return true;
        }
        public boolean isToBe() {
            return true;
        }
        public boolean notAGetter() { // name convention does not match
            return false;
        }
        public boolean getNotAGetter(int a) { // getters do not take parameter
            return false;
        }
        boolean get() { // wrong visiblity
            return false;
        }
        public void getVoid() { // void return type

        }
        public Class<?> getTypeClass() { // do not match return Class<?>
            return Object.class;
        }
        class InnerClass {

        }
        public InnerClass getNotVisibleClass() { // do not match return not public classes
            return new InnerClass();
        }
        public Stream<?> getStream() { // do not match stream
            return Stream.of();
        }
    }

    private MethodsHandler methodsHandlerUnderTest;
    private Object o;
    private MyClassWithoutImplementedToStringAndHashCode myClassWithoutImplementedToStringAndHashCode;
    private MyClassWithImplementedToStringAndHashCode myClassWithImplementedToStringAndHashCode;

    @Before
    public void setUp() throws Exception {
        this.methodsHandlerUnderTest = new MethodsHandler();
        this.o = new Object();
        this.myClassWithoutImplementedToStringAndHashCode = new MyClassWithoutImplementedToStringAndHashCode();
        this.myClassWithImplementedToStringAndHashCode = new MyClassWithImplementedToStringAndHashCode();
    }

    @Test
    public void testIsDefaulttoStringOrHashCode() throws Exception {

        /*
            test the method checker is default toString or hashCode
                In case of the method toString or hashCode has been implemented by the class, it returns false,
                otherwise it returns false, i.e. it is the default implementation of the toString and hashCode, in java.lang.Object
         */

        assertTrue(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(Object.class.getMethod("hashCode")));
        assertTrue(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(Object.class.getMethod("toString")));
        assertTrue(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(o.getClass().getMethod("hashCode")));
        assertTrue(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(o.getClass().getMethod("toString")));

        assertTrue(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                MyClassWithoutImplementedToStringAndHashCode.class.getMethod("hashCode"))
        );
        assertTrue(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                MyClassWithoutImplementedToStringAndHashCode.class.getMethod("toString"))
        );

        assertTrue(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                myClassWithoutImplementedToStringAndHashCode.getClass().getMethod("hashCode"))
        );
        assertTrue(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                myClassWithoutImplementedToStringAndHashCode.getClass().getMethod("toString"))
        );

        assertFalse(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                MyClassWithImplementedToStringAndHashCode.class.getMethod("hashCode"))
        );
        assertFalse(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                MyClassWithImplementedToStringAndHashCode.class.getMethod("toString"))
        );

        assertFalse(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                myClassWithImplementedToStringAndHashCode.getClass().getMethod("hashCode"))
        );
        assertFalse(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                myClassWithImplementedToStringAndHashCode.getClass().getMethod("toString"))
        );

        assertFalse(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                ((Object)myClassWithImplementedToStringAndHashCode).getClass().getMethod("hashCode"))
        );
        assertFalse(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                ((Object)myClassWithImplementedToStringAndHashCode).getClass().getMethod("toString"))
        );

        assertTrue(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                URL.class.getMethod("hashCode"))
        );

        assertFalse(methodsHandlerUnderTest.isDefaulttoStringOrHashCode(
                URL.class.getMethod("toString"))
        );
    }

    @Test
    public void testGetAllMethods() throws Exception {

        /*
            the returned sets of methods of getAllMethods should all match isValidMethod
                here, we checks that the returned method are correct ones
         */

        // we do not match any methods for java.lang.Object
        assertTrue(this.methodsHandlerUnderTest.getAllMethods(Object.class).isEmpty());
        assertTrue(this.methodsHandlerUnderTest.getAllMethods(this.o.getClass()).isEmpty());

        // we do match toString and hashCode implemented in classes
        List<Method> allMethods = this.methodsHandlerUnderTest.getAllMethods(this.myClassWithImplementedToStringAndHashCode.getClass());
        List<String> expectedMethods= Arrays.asList(
                "toString",
                "hashCode"
        );
        assertEquals(
                "Only " + expectedMethods.toString() + " should match: . Actual:" + allMethods.toString(),
                expectedMethods.size(),
                allMethods.size()
        );

        allMethods = this.methodsHandlerUnderTest.getAllMethods(this.myClassWithoutImplementedToStringAndHashCode.getClass());
        expectedMethods= Arrays.asList(
                "getInteger",
                "shouldBe",
                "isToBe",
                "hasToBe"
        );
        assertEquals(
                "Only " + expectedMethods.toString() + " should match: . Actual:" + allMethods.toString(),
                expectedMethods.size(),
                allMethods.size()
        );

    }
}
