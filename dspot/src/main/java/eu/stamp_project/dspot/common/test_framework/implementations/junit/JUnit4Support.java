package eu.stamp_project.dspot.common.test_framework.implementations.junit;

import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Math.toIntExact;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class JUnit4Support extends JUnitSupport {

    public JUnit4Support() {
        super("org.junit.Assert");
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationAfterClass() {
        return "org.junit.AfterClass";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationTest() {
        return "org.junit.Test";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationIgnore() {
        return "org.junit.Ignore";
    }

    @Override
    public CtMethod<?> prepareTestMethod(CtMethod<?> testMethod) {
        final CtMethod<?> clone = super.prepareTestMethod(testMethod);
        final Factory factory = testMethod.getFactory();
        CtAnnotation testAnnotation = clone
                .getAnnotations()
                .stream()
                .filter(annotation -> annotation.toString().contains("Test"))
                .findFirst()
                .orElse(null);
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
            values.remove("expected");
            testAnnotation.setValues(values);
        } else {
            CtAnnotation newTestAnnotation = factory.Core().createAnnotation();
            CtTypeReference<Object> ref = factory.Core().createTypeReference();
            ref.setSimpleName("Test");
            CtPackageReference refPackage = factory.Core().createPackageReference();
            refPackage.setSimpleName("org.junit");
            ref.setPackage(refPackage);
            newTestAnnotation.setAnnotationType(ref);
            Map<String, Object> elementValue = new HashMap<>();
            elementValue.put("timeout", AmplificationHelper.timeOutInMs);
            newTestAnnotation.setElementValues(elementValue);
            clone.addAnnotation(newTestAnnotation);
        }
        return clone;
    }
}
