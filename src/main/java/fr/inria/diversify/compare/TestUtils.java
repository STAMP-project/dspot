package fr.inria.diversify.compare;


import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * User: Simon
 * Date: 01/10/15
 * Time: 11:58
 */
public class TestUtils {

    public static void runSetUp(Object o) {
        runSetUp(o, o.getClass());
    }

    protected static void runSetUp(Object receiver, Class testClass) {
        for(Method mth :  testClass.getDeclaredMethods()) {
            if(isSetUpMethod(mth)) {
                try {
                    mth.setAccessible(true);
                    mth.invoke(receiver);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        if(testClass.getSuperclass() != null && !testClass.getSuperclass().getSimpleName().equals("Object")) {
            runSetUp(receiver, testClass.getSuperclass());
        }
    }

    protected static boolean isSetUpMethod(Method mth) {
        if(mth.getName().contains("setUp") && mth.getParameters().length == 0) {
            return true;
        }
        for(Annotation annotation : mth.getDeclaredAnnotations()) {
            if(annotation.annotationType().getSimpleName().equals("Before")) {
                return true;
            }
        }
        return false;
    }

    public static void runTearDown(Object o) {
        runSetUp(o, o.getClass());
    }

    protected static void runTearDown(Object receiver, Class testClass) {
        for(Method mth :  testClass.getDeclaredMethods()) {
            if(isTearDownMethod(mth)) {
                try {
                    mth.setAccessible(true);
                    mth.invoke(receiver);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        if(testClass.getSuperclass() != null && !testClass.getSuperclass().getSimpleName().equals("Object")) {
            runSetUp(receiver, testClass.getSuperclass());
        }
    }


    protected static boolean isTearDownMethod(Method mth) {
        if(mth.getName().equals("tearDown") && mth.getParameters().length == 0) {
            return true;
        }
        for(Annotation annotation : mth.getDeclaredAnnotations()) {
            if(annotation.annotationType().getSimpleName().equals("After")) {
                return true;
            }
        }
        return false;
    }
}
