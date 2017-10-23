package fr.inria.diversify.utils.sosiefier;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Simon on 03/12/14.
 */
@Deprecated
public abstract class TestProcessor extends AbstractProcessor<CtMethod> {
    public static List<CtMethod> notHarmanTest = new LinkedList<>();
    protected static List<CtMethod> mutatedMethod = new LinkedList<>();
    protected static int count = 0;

    protected String logName = "fr.inria.diversify.testamplification.logger.Logger";

    public static Set<CtType> ampclasses = new HashSet<>();

    protected boolean guavaTestlib = false;

    protected int cloneNumber = 1;

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {
        return isTest(candidate)
                && !mutatedMethod.contains(candidate)
                && !testWhitThread(candidate);
    }

    protected boolean isTest(CtMethod candidate) {
        if(candidate.isImplicit()
                || !candidate.getModifiers().contains(ModifierKind.PUBLIC)
                || candidate.getBody() == null
                || candidate.getBody().getStatements().size() == 0) {
            return false;
        }
        if(!guavaTestlib) {
            return candidate.getSimpleName().contains("test")
                    || candidate.getAnnotations().stream()
                    .map(annotation -> annotation.toString())
                    .anyMatch(annotation -> annotation.startsWith("@org.junit.Test"));
        } else {
            return  candidate.getDeclaringType().getSimpleName().endsWith("Tester")
                    && (candidate.getSimpleName().contains("test")
                    || candidate.getAnnotations().stream()
                    .map(annotation -> annotation.toString())
                    .anyMatch(annotation -> annotation.startsWith("@org.junit.Test")));
        }
    }

    public void setGuavaTestlib(boolean guavaTestlib) {
        this.guavaTestlib = guavaTestlib;
    }

    protected CtMethod cloneMethod(CtMethod method, String suffix) {
        count++;
        CtMethod cloned_method = this.getFactory().Core().clone(method);
        cloned_method.setParent(method.getParent());
        //rename the clone
        cloned_method.setSimpleName(method.getSimpleName()+suffix+cloneNumber);
        cloneNumber++;

        CtAnnotation toRemove = cloned_method.getAnnotations().stream()
                                             .filter(annotation -> annotation.toString().contains("Override"))
                                             .findFirst().orElse(null);

        if(toRemove != null) {
            cloned_method.removeAnnotation(toRemove);
        }
        mutatedMethod.add(cloned_method);
        return cloned_method;
    }

    protected CtMethod cloneMethodTest(CtMethod method, String suffix, int timeOut) {
        CtMethod cloned_method = cloneMethod(method,suffix);
        CtAnnotation testAnnotation = cloned_method.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains("Test"))
                .findFirst().orElse(null);

        if(testAnnotation != null) {
            cloned_method.removeAnnotation(testAnnotation);
        }

        testAnnotation = getFactory().Core().createAnnotation();
        CtTypeReference<Object> ref = getFactory().Core().createTypeReference();
        ref.setSimpleName("Test");

        CtPackageReference refPackage = getFactory().Core().createPackageReference();
        refPackage.setSimpleName("org.junit");
        ref.setPackage(refPackage);
        testAnnotation.setAnnotationType(ref);


        Map<String, Object> elementValue = new HashMap<String, Object>();
        elementValue.put("timeout", timeOut);
        testAnnotation.setElementValues(elementValue);

        cloned_method.addAnnotation(testAnnotation);

        ampclasses.add(cloned_method.getDeclaringType());
        return cloned_method;
    }

    protected boolean isAssert(CtInvocation invocation) {
        try {
            Class cl = invocation.getExecutable().getDeclaringType().getActualClass();
            String signature = invocation.getExecutable().getSimpleName();
            return (signature.contains("assert") || signature.contains("fail"))
             && isAssertInstance(cl);
        } catch (Exception e) {
            return false;
        }

    }

    protected boolean isAssertInstance(Class cl) {
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

    protected boolean testWhitThread(CtMethod method) {
        return method.toString().contains("Thread");
    }

    protected String getLogName() {
        return logName;
    }

    public void setLogger(String logName) {
        this.logName = logName;
    }
}
