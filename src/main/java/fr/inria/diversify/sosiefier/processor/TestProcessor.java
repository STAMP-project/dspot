package fr.inria.diversify.sosiefier.processor;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Simon on 03/12/14.
 */
@Deprecated
public abstract class TestProcessor extends AbstractProcessor<CtMethod> {
    protected static List<CtMethod> cloneTests = new LinkedList<>();
    protected static int count = 0;

    protected String logName;


    protected int cloneNumber = 1;

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {
        return isTest(candidate)
                && !cloneTests.contains(candidate);
//                && !testWhitThread(candidate);
    }

    protected boolean isTest(CtMethod candidate) {
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
        cloneTests.add(cloned_method);
        return cloned_method;
    }

    protected boolean isAssert(CtInvocation invocation) {
        try {
            String mthName = invocation.getExecutable().getSimpleName();
            if(!(mthName.startsWith("assert") || mthName.startsWith("fail"))) {
                return false;
            }
            Class cl = invocation.getExecutable().getDeclaringType().getActualClass();
            return isAssertInstance(cl);
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
