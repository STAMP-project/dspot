package fr.inria.diversify.profiling.processor.main;

import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 21/03/16
 * Time: 10:34
 */
public class TestFinderProcessor extends AbstractLoggingInstrumenter<CtMethod> {

    public TestFinderProcessor(InputProgram inputProgram) {
        super(inputProgram);
    }

    public boolean isToBeProcessed(CtMethod mth) {
        CtType declaringClass = mth.getDeclaringType();
        if(!declaringClass.isTopLevel()) {
            return false;
        }
        List<CtParameter> params = mth.getParameters();
        boolean condition = params.stream()
                .map(param -> param.getType())
                .allMatch(param -> isPrimitive(param)
                        || isString(param)
                        || isPrimitiveArray(param)
                        || isPrimitiveCollection(param)
                        || isPrimitiveMap(param));

        if(condition) {
            return checkVisibility(mth)
                && !Query.getElements(mth, new TypeFilter(CtInvocation.class)).isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public void process(CtMethod method) {
//        int id = methodId(method);
        String id = method.getDeclaringType().getQualifiedName() + "." + method.getSimpleName();
        CtTry ctTry = tryFinallyBody(method);
        Factory factory = getFactory();

        String snippet = getLogger()
                + ".startLog(Thread.currentThread(), \"" + id;
        if(method.getParameters().isEmpty()) {
            snippet += "\")";
        } else {
            snippet += "\", " + method.getParameters().stream()
                    .map(param -> ((CtParameter)param).getSimpleName())
                    .collect(Collectors.joining(", "))
                    + ")";
        }
        CtCodeSnippetStatement beginStmt = factory.Code().createCodeSnippetStatement(snippet);
        ctTry.getBody().insertBegin(beginStmt);

        snippet = getLogger() + ".stopLog(Thread.currentThread(),\"" + id + "\"";
        if(method.getModifiers().contains(ModifierKind.STATIC)) {
            snippet += ", null)";
        } else {
            snippet +=", this)";
        }

        CtCodeSnippetStatement stmt = factory.Code().createCodeSnippetStatement(snippet);
        ctTry.getFinalizer().addStatement(stmt);
    }

    protected boolean isPrimitive(CtTypeReference type) {
        return type.unbox().isPrimitive();
    }

    protected boolean isString(CtTypeReference type) {
        try {
            return String.class.isAssignableFrom(type.getActualClass());
        } catch (Exception e) {}
        return false;
    }

    protected boolean isPrimitiveArray(CtTypeReference type) {
        if(CtArrayTypeReference.class.isInstance(type)) {
            return isPrimitive(((CtArrayTypeReference)type).getComponentType());
        }
        return false;
    }

    protected boolean isPrimitiveCollection(CtTypeReference type) {
        try {
            return Collection.class.isAssignableFrom(type.getActualClass());
        } catch (Exception e) {}
        return false;
    }

    protected boolean isPrimitiveMap(CtTypeReference type) {
        try {
            return Map.class.isAssignableFrom(type.getActualClass());
        } catch (Exception e) {}
        return false;
    }

    protected boolean checkVisibility(CtMethod method) {
        if(method.getModifiers().contains(ModifierKind.PUBLIC)) {
            return true;
        } else {
            List<CtInvocation> invocations = Query.getElements(method, new TypeFilter(CtInvocation.class));
            return invocations.stream()
                    .allMatch(invocation -> {
                        try {
                            return Modifier.isPublic(invocation.getExecutable().getActualMethod().getModifiers());
                        } catch (Exception e) {
                            CtExecutable exe = invocation.getExecutable().getDeclaration();
                            if(exe instanceof CtMethod) {
                                return  ((CtMethod)exe).getModifiers().contains(ModifierKind.PUBLIC);
                            } else {
                                return ((CtConstructor)exe).getModifiers().contains(ModifierKind.PUBLIC);
                            }
                        }
                    });

        }
    }
}
