package fr.inria.stamp.test.runner;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class TestRunnerFactory {

    //This list contains the name of annotation in RunWith that needs
    //to be run with refection
    private static final List<String> annotationNames = new ArrayList<>();

    static {
        annotationNames.add("JMockitRunner");
    }

    private static boolean containsSpecificAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotationNames.contains(annotation.toString())) {
                return true;
            }
        }
        return false;
    }

    public static TestRunner createRunner(Class<?> testClass, String classpath) {
        if (containsSpecificAnnotation(testClass.getAnnotations())) {
            return new ReflectiveTestRunner(classpath);
        } else {
            return new DefaultTestRunner(classpath);
        }
    }

}
