package fr.inria.diversify.profiling.processor.test;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;

/**
 * Created by Simon on 03/12/14.
 */
public abstract class TestProcessor extends AbstractProcessor<CtMethod> {

    private static List<CtMethod> mutatedMethod = new LinkedList<>();
    private static int count = 0;

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {
        return isTest(candidate)
                && !mutatedMethod.contains(candidate)
                && !testWhitThread(candidate);
    }

    private boolean isTest(CtMethod candidate) {
        if(candidate.isImplicit()
                || !candidate.getModifiers().contains(ModifierKind.PUBLIC)
                || candidate.getBody() == null
                || candidate.getBody().getStatements().size() == 0) {
            return false;
        }

        return candidate.getSimpleName().contains("test")
                || candidate.getAnnotations().stream()
                .map(annotation -> annotation.toString())
                .anyMatch(annotation -> annotation.startsWith("@org.junit.Test"));
    }

    private boolean isAssertInstance(Class cl) {
        if (cl.equals(org.junit.Assert.class) || cl.equals(junit.framework.Assert.class))
            return true;
        Class superCl = cl.getSuperclass();
        if(superCl != null) {
            return isAssertInstance(superCl);
        }
        return false;
    }

    public static int getCount() {
        return count;
    }

    private boolean testWhitThread(CtMethod method) {
        return method.toString().contains("Thread");
    }
}
