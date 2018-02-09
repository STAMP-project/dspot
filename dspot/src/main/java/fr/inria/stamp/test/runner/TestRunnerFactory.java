package fr.inria.stamp.test.runner;

import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Deprecated
public class TestRunnerFactory {

    // TODO this is a poor contract, and need to be enhanced
    // we retrieve all the TypeReference used in the test class
    // if any one match with the predicate, we build a reflective test runner.
    // the predicate is based on the named of packages

    // packages added
    //  - jmockit <=> mockit

    // this list contains string to filter test class
    private static final List<String> STRING_ARRAY_LIST = new ArrayList<>();

    static {
        STRING_ARRAY_LIST.add("mockit");
        //STRING_ARRAY_LIST.add("org.mockito");
    }

    public static boolean useReflectiveTestRunner = false;

    public static final Predicate<CtType<?>> containsSpecificAnnotation = testClass ->
            !(testClass.getElements(
                    new TypeFilter<CtTypeReference>(CtTypeReference.class) {
                        @Override
                        public boolean matches(CtTypeReference element) {
                            return STRING_ARRAY_LIST.stream()
                                    .anyMatch(string ->
                                            element.getQualifiedName().startsWith(string)
                                    );
                        }
                    }
            ).isEmpty());

    public static TestRunner createRunner(CtType<?> testClass, String classpath) {
        if (useReflectiveTestRunner || testClass != null && containsSpecificAnnotation.test(testClass)) {
            return new ReflectiveTestRunner(classpath);
        } else {
            return new DefaultTestRunner(classpath);
        }
    }

    public static TestRunner createRunner(CtType<?> testClass, URLClassLoader classLoader) {
        if (useReflectiveTestRunner || containsSpecificAnnotation.test(testClass)) {
            return new ReflectiveTestRunner(classLoader);
        } else {
            return new DefaultTestRunner(classLoader);
        }
    }

}
