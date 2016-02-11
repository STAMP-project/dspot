package fr.inria.diversify.crossCheckingOracle;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 22/09/15
 */
public class CrossCheckingOracleBuilder {

    public CtMethod builder(CtMethod test) {
        Factory factory = test.getFactory();
        CtType<?> declaringClass = test.getDeclaringType();

        List<CtLocalVariable> localVar = findAllVariableDeclaration(test.getBody());
        boolean isOverride = test.getAnnotations().stream()
                .anyMatch(annotation -> !annotation.getSignature().contains("Override"));
        if(localVar.isEmpty() && !isOverride) {
            return null;
        }

        Set<CtTypeReference<? extends Throwable>> thrownTypes = getThrow(test);

        CtMethod runMethod = buildRunMethod(test, thrownTypes, localVar);

        CtBlock<Object> body = factory.Core().createBlock();
        switchToSosie(body, false);
        addLocalListDeclaration(body, "objects1");
        addLocalListDeclaration(body, "objects2");
        addLocalListDeclaration(body, "objects3");

        body.addStatement(factory.Code().createCodeSnippetStatement(runMethod.getSimpleName() + "(objects1)"));

        addTeardownStatement(test, body);
        addSetUpStatement(test, body);

        body.addStatement(factory.Code().createCodeSnippetStatement(runMethod.getSimpleName() + "(objects2)"));
        addTeardownStatement(test, body);
        addSetUpStatement(test, body);

        body.addStatement(factory.Code().createCodeSnippetStatement(runMethod.getSimpleName() + "(objects3)"));

        addTeardownStatement(test, body);
        addSetUpStatement(test, body);

        switchToSosie(body, true);
        addLocalListDeclaration(body, "objects4");

        body.addStatement(factory.Code().createCodeSnippetStatement(runMethod.getSimpleName() + "(objects4)"));
        addTeardownStatement(test, body);

        addFilter(body, "objects1", "objects3", "filter");
        addCompare(body, "objects2", "objects4", "filter");

        CtMethod cloneMethod = factory.Method().create(declaringClass,
                test.getModifiers(),
                test.getType(),
                test.getSimpleName() + "_clone",
                test.getParameters(),
                    thrownTypes);

        cloneMethod.setAnnotations(getAnnotations(test));
        cloneMethod.setBody(body);

        return cloneMethod;
    }

    protected CtMethod buildRunMethod(CtMethod test,  Set<CtTypeReference<? extends Throwable>> thrownTypes, List<CtLocalVariable> localVar) {
        Factory factory = test.getFactory();
        CtType<?> declaringClass = test.getDeclaringType();

        CtBlock tryBody = factory.Core().clone(test.getBody());
        addCopyObject(tryBody, "_objects_", localVar);

        CtParameter<Object> param = factory.Core().createParameter();
        CtTypeReference typeRef = factory.Core().createTypeReference();
        typeRef.setSimpleName("java.util.ArrayList<Object>");
        param.setType(typeRef);
        param.setSimpleName("_objects_");
        List<CtParameter<?>> params = new ArrayList<>(1);
        params.add(param);

        CtMethod mth = factory.Method().create(declaringClass,
                test.getModifiers(),
                test.getType(),
                test.getSimpleName() + "_run",
                params,
                thrownTypes);

        if(isExceptionTest(test)) {
            mth.setBody(tryBody);
        } else {
            CtTry tryBlock = buildTryBody(tryBody, factory);
            CtBlock bodyMth = factory.Core().createBlock();
            bodyMth.addStatement(tryBlock);
            mth.setBody(bodyMth);
        }

        return mth;
    }

    protected boolean isExceptionTest(CtMethod test) {
        CtAnnotation<? extends Annotation> annotation = test.getAnnotations().stream()
                .filter(anno -> anno.getSignature().contains("Test"))
                .findFirst()
                .orElse(null);

        if(annotation != null) {
            return annotation.getElementValues().containsKey("expected");
        }
        return false;
    }

    protected CtTry buildTryBody(CtBlock tryBody, Factory factory) {
        CtTry tryBlock = factory.Core().createTry();
        tryBlock.setBody(tryBody);
        CtCatch ctCatch = factory.Core().createCatch();
        tryBlock.addCatcher(ctCatch);
        CtCatchVariable catchVar = factory.Core().createCatchVariable();
        catchVar.setSimpleName("eee");
        CtTypeReference typeCatch = factory.Core().createTypeReference();
        typeCatch.setSimpleName("Throwable");
        catchVar.setType(typeCatch);
        ctCatch.setParameter(catchVar);
        ctCatch.setBody(factory.Core().createBlock());

        return tryBlock;
    }

    protected Set<CtTypeReference<? extends Throwable>> getThrow(CtMethod mth) {
        CtType<?> declaringClass = mth.getDeclaringType();
        Set<CtTypeReference<? extends Throwable>> thrownTypes = new HashSet<>(mth.getThrownTypes());

        if(findSetUpMethod(declaringClass) != null) {
            thrownTypes.addAll(findSetUpMethod(declaringClass).getThrownTypes());
        }
        if(findTeardownMethod(declaringClass) != null) {
            thrownTypes.addAll(findTeardownMethod(declaringClass).getThrownTypes());
        }

        return thrownTypes;
    }

    protected List<CtAnnotation<? extends Annotation>> getAnnotations(CtMethod mth) {
        return mth.getAnnotations().stream()
                .filter(annotation -> !annotation.getSignature().contains("Override"))
                .collect(Collectors.toList());
    }

    protected void addFilter(CtBlock<Object> body, String originalsObjects, String sosiesObjects, String filterName) {
        Factory factory = body.getFactory();
        CtCodeSnippetStatement stmt = factory.Code().createCodeSnippetStatement("java.util.List<Boolean> " + filterName
                + " = fr.inria.diversify.compare.Compare.getSingleton().buildFilter(" + originalsObjects + "," + sosiesObjects + ")");
        body.addStatement(stmt);
    }

    protected void addCompare(CtBlock<Object> body, String originalsObjects, String sosiesObjects, String filter) {
        Factory factory = body.getFactory();
        CtCodeSnippetStatement stmt = factory.Code().createCodeSnippetStatement("org.junit.Assert.assertTrue(fr.inria.diversify.compare.Compare.getSingleton().compare("
                + originalsObjects + "," + sosiesObjects + ", "+ filter + "))");
        body.addStatement(stmt);
    }

    protected void addLocalListDeclaration( CtBlock body, String varName) {
        Factory factory = body.getFactory();
        CtCodeSnippetStatement newList = factory.Code().createCodeSnippetStatement("java.util.ArrayList<Object> " + varName + " = new java.util.ArrayList<Object>()");
        body.addStatement(newList);
    }

    protected void addCopyObject(CtBlock body, String varName, List<CtLocalVariable> localVariables) {
        Factory factory = body.getFactory();

        for(CtLocalVariable var : localVariables) {
            CtCodeSnippetStatement addVarStmt = factory.Code().createCodeSnippetStatement(varName + ".add(" + var.getSimpleName() + ")");
            body.addStatement(addVarStmt);
        }
    }

    protected void switchToSosie(CtBlock<Object> body, boolean sosie) {
        Factory factory = body.getFactory();
        body.addStatement(factory.Code().createCodeSnippetStatement("fr.inria.diversify.transformation.switchsosie.Switch.switchTransformation = " + sosie));
    }

    protected void addSetUpStatement(CtMethod mth, CtBlock body) {
        if(!mth.getModifiers().contains(ModifierKind.STATIC)) {
            CtCodeSnippetStatement stmt =
                    mth.getFactory().Code().createCodeSnippetStatement("fr.inria.diversify.compare.TestUtils.runSetUp(this)");
            body.addStatement(stmt);
        }
    }

    protected void addTeardownStatement(CtMethod mth, CtBlock body) {
        if(!mth.getModifiers().contains(ModifierKind.STATIC)) {
            CtCodeSnippetStatement stmt =
                    mth.getFactory().Code().createCodeSnippetStatement("fr.inria.diversify.compare.TestUtils.runTearDown(this)");
            body.addStatement(stmt);
        }
    }

    protected CtMethod findSetUpMethod(CtType<?> cl) {
        for(CtMethod mth : cl.getMethods()) {
            if(mth.getSimpleName().toLowerCase().equals("setup")
                    || mth.getAnnotations().stream().anyMatch(anno -> anno.getSignature().toLowerCase().contains("before"))) {
                return mth;
            }
        }
        if(cl.getSuperclass() != null
                && cl.getSuperclass().getSimpleName() != "Object"
                && cl.getSuperclass().getDeclaration() != null) {
            return findSetUpMethod(cl.getSuperclass().getDeclaration());
        } else {
            return null;
        }
    }

    protected CtMethod findTeardownMethod(CtType<?> cl) {
        for(CtMethod mth : cl.getMethods()) {
            if(mth.getSimpleName().toLowerCase().equals("teardown")
                    || mth.getAnnotations().stream().anyMatch(anno -> anno.getSignature().toLowerCase().contains("after"))) {
                return mth;
            }
        }
        if(cl.getSuperclass() != null
                && cl.getSuperclass().getSimpleName() != "Object"
                && cl.getSuperclass().getDeclaration() != null) {
            return findTeardownMethod(cl.getSuperclass().getDeclaration());
        } else {
            return null;
        }
    }

    protected List<CtLocalVariable> findAllVariableDeclaration(CtBlock block) {
        return  (List<CtLocalVariable>) Query.getElements(block, new TypeFilter(CtLocalVariable.class)).stream()
                .filter(var -> ((CtLocalVariable)var).getParent().equals(block))
                .collect(Collectors.toList());
    }
}
