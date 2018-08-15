package eu.stamp_project.utils;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Math.toIntExact;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/18
 */
public class CloneHelper {

    private static int cloneNumber = 1;

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

    /**
     * Prepares the test annotation of a method
     *
     * @param cloned_method The test method to modify
     * @param factory The factory to create a new test annotation if needed
     */
    public static void prepareTestMethod(CtMethod cloned_method, Factory factory) {
        CtAnnotation testAnnotation = cloned_method.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains("Test"))
                .findFirst().orElse(null);
        if (testAnnotation != null) {
            final Map<String, CtExpression<?>> values = new HashMap<>(testAnnotation.getValues());
            CtExpression<?> originalTimeout = values.get("timeout");
            if (originalTimeout == null ||
                    originalTimeout instanceof CtLiteral &&
                            (((CtLiteral) originalTimeout).getValue().equals(0L))) {
                values.put("timeout", factory.createLiteral(AmplificationHelper.timeOutInMs));
            } else {
                int valueOriginalTimeout;
                if (originalTimeout.toString().endsWith("L")) {
                    String stringTimeout = originalTimeout.toString();
                    valueOriginalTimeout = toIntExact(parseLong(stringTimeout.substring(0, stringTimeout.length() - 1)));
                } else {
                    valueOriginalTimeout = parseInt(originalTimeout.toString());
                }
                if (valueOriginalTimeout < AmplificationHelper.timeOutInMs) {
                    CtLiteral newTimeout = factory.createLiteral(AmplificationHelper.timeOutInMs);
                    values.put("timeout", newTimeout);
                }
            }
            if (values.containsKey("expected")) {
                values.remove("expected");
            }
            testAnnotation.setValues(values);
        } else {
            CtAnnotation newTestAnnotation;
            newTestAnnotation = factory.Core().createAnnotation();
            CtTypeReference<Object> ref = factory.Core().createTypeReference();
            ref.setSimpleName("Test");

            CtPackageReference refPackage = factory.Core().createPackageReference();
            refPackage.setSimpleName("org.junit");
            ref.setPackage(refPackage);
            newTestAnnotation.setAnnotationType(ref);

            Map<String, Object> elementValue = new HashMap<>();
            elementValue.put("timeout", AmplificationHelper.timeOutInMs);
            newTestAnnotation.setElementValues(elementValue);
            cloned_method.addAnnotation(newTestAnnotation);
        }

        cloned_method.addThrownType(factory.Type().createReference(Exception.class));
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
     * Clones a method.
     *
     * @param method Method to be cloned
     * @param suffix Suffix for the cloned method's name
     * @return The cloned method
     */
    private static CtMethod cloneMethod(CtMethod method, String suffix) {
        CtMethod cloned_method = method.clone();
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
        final Factory factory = method.getFactory();
        prepareTestMethod(cloned_method, factory);
        return cloned_method;
    }
}
