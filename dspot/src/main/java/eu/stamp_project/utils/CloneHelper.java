package eu.stamp_project.utils;

import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.program.InputConfiguration;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.lang.annotation.Annotation;
import java.util.List;

import com.googlecode.junittoolbox.ParallelRunner;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/18
 */
public class CloneHelper {

    private static int cloneNumber = 1;
    final static Factory factory = InputConfiguration.get().getFactory();

    public static void reset() {
        cloneNumber = 1;
    }

    /**
     * Clones the test class and adds the test methods.
     *
     * @param original Test class
     * @param methods  Test methods
     * @return Test class with new methods
     */
    public static CtType cloneTestClassAndAddGivenTest(CtType original, List<CtMethod<?>> methods) {
        CtType clone = original.clone();
        original.getPackage().addType(clone);
        methods.forEach(clone::addMethod);
        return clone;
    }

    public static void addParallelExecutionAnnotation(CtType clone, List<CtMethod<?>> tests) {
        if (TestFramework.isJUnit5(tests.get(0))) {
            addJUnit5ParallelExecutionAnnotation(clone);
        }else {
            addJUnit4ParallelExecutionAnnotation(clone);
        }
    }

    public static void removeParallelExecutionAnnotation(CtType clone, List<CtMethod<?>> tests) {
        if (TestFramework.isJUnit5(tests.get(0))) {
            removeJUnit5ParallelExecutionAnnotation(clone);
        }else {
            removeJUnit4ParallelExecutionAnnotation(clone);
        }
    }

    private static void removeJUnit4ParallelExecutionAnnotation(CtType clone) {
        removeAnnotation(clone, "RunWith");
    }

    private static void removeJUnit5ParallelExecutionAnnotation(CtType clone) {
        removeAnnotation(clone, "Execution");
    }

    private static void removeAnnotation(CtType clone, String label) {
        CtAnnotation existing_annotation = clone.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains(label))
                .findFirst().orElse(null);
        if (existing_annotation!=null) {
            clone.removeAnnotation(existing_annotation);
        }
    }

    private static void addJUnit4ParallelExecutionAnnotation(CtType clone) {
        CtAnnotation existing_annotation = clone.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains("RunWith"))
                .findFirst().orElse(null);
        if (existing_annotation==null) {
            CtAnnotation<Annotation> annotation =
                    factory.Code().createAnnotation(factory.Code().createCtTypeReference(org.junit.runner.RunWith.class));
            annotation.addValue("value", ParallelRunner.class);
            clone.addAnnotation(annotation);
        }
    }

    private static void addJUnit5ParallelExecutionAnnotation(CtType clone) {
        CtAnnotation existing_annotation = clone.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains("Execution"))
                .findFirst().orElse(null);
        if (existing_annotation==null) {
            CtAnnotation<Annotation> annotation =
                    factory.Code().createAnnotation(factory.Code().createCtTypeReference(org.junit.jupiter.api.parallel.Execution.class));
            annotation.addValue("value", org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT);
            clone.addAnnotation(annotation);
        }
    }

    public static CtMethod cloneTestMethodForAmp(CtMethod method, String suffix) {
        CtMethod clonedMethod = cloneTestMethod(method, suffix);
        AmplificationHelper.ampTestToParent.put(clonedMethod, method);
        return clonedMethod;
    }

    public static CtMethod cloneTestMethodNoAmp(CtMethod method) {
        return cloneTestMethod(method, "");
    }

    /**
     * Clones a method and registers its bound to the original method.
     *
     * @param method Method to be cloned
     * @return The cloned method
     */

    public static CtMethod<?> cloneMethod(CtMethod<?> method) {
        CtMethod cloned_method = method.clone();
        //Optimization: Tracking bound to original method
        AmplificationHelper.addTestBindingToOriginal(cloned_method, method);
        return cloned_method;
    }

    /**
     * Clones a method.
     *
     * @param method Method to be cloned
     * @param suffix Suffix for the cloned method's name
     * @return The cloned method
     */
    public static CtMethod cloneMethod(CtMethod<?> method, String suffix) {
        CtMethod cloned_method = cloneMethod(method);

        //rename the clone
        cloned_method.setSimpleName(method.getSimpleName() + (suffix.isEmpty() ? "" : suffix + cloneNumber));
        cloneNumber++;

        CtAnnotation toRemove = cloned_method.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains("Override"))
                .findFirst().orElse(null);

        if (toRemove != null) {
            cloned_method.removeAnnotation(toRemove);
        }
        return cloned_method;
    }

    /**
     * Clones a test method.
     * <p>
     * Performs necessary integration with JUnit and adds timeout.
     *
     * @param method Method to be cloned
     * @param suffix Suffix for the cloned method's name
     * @return The cloned method
     */
    private static CtMethod cloneTestMethod(CtMethod method, String suffix) {
        CtMethod cloned_method = cloneMethod(method, suffix);
        TestFramework.get().prepareTestMethod(cloned_method);
        return cloned_method;
    }
}
