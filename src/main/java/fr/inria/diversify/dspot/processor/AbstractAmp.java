package fr.inria.diversify.dspot.processor;

import fr.inria.diversify.profiling.coverage.Coverage;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.ImportScanner;
import spoon.reflect.visitor.ImportScannerImpl;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 02/12/15
 * Time: 14:30
 */
public abstract class AbstractAmp {
    protected Set<String> previousTestAmp;
    protected int cloneNumber;
    protected static Map<CtMethod,CtMethod> ampTestToParent;

    protected Random random;

    public abstract List<CtMethod> apply(CtMethod method);
    public abstract CtMethod applyRandom(CtMethod method);

    public void reset(InputProgram inputProgram, Coverage coverage, CtClass testClass) {
        cloneNumber = 1;
        previousTestAmp = new HashSet<>();
        ampTestToParent = new HashMap<>();
    }


    protected List<CtMethod> filterAmpTest(List<CtMethod> tests, CtMethod parentTest) {
//        List<CtMethod> filterTests = tests.stream()
//                .filter(test -> {
//                    try {
//                        return !previousTestAmp.contains(test.toString());
//                    } catch (Exception e) {
//                        return false;
//                    }
//                })
//                .collect(Collectors.toList());
//
//        filterTests.stream()
//                .forEach(test -> {
//                        ampTestToParent.put(test,parentTest);
//                        previousTestAmp.add(test.toString());
//                });
//
//        return filterTests;
        tests.stream()
                .forEach(test -> ampTestToParent.put(test,parentTest));
        return tests;
    }

    protected boolean isTest(CtMethod candidate) {
        if(candidate.isImplicit()
                || !candidate.getModifiers().contains(ModifierKind.PUBLIC)
                || candidate.getBody() == null
                || candidate.getBody().getStatements().size() == 0) {
            return false;
        }

            return  candidate.getDeclaringType().getSimpleName().endsWith("Tester")
                    && (candidate.getSimpleName().contains("test")
                    || candidate.getAnnotations().stream()
                    .map(annotation -> annotation.toString())
                    .anyMatch(annotation -> annotation.startsWith("@org.junit.Test")));
    }

    protected boolean isAssert(CtStatement stmt) {
        if(stmt instanceof CtInvocation) {
            return isAssert((CtInvocation) stmt);
        } else {
            return false;
        }
    }

    protected boolean isAssert(CtInvocation invocation) {
        try {
            Class cl = invocation.getExecutable().getDeclaringType().getActualClass();
            String mthName = invocation.getExecutable().getSimpleName();
            return (mthName.startsWith("assert") || mthName.contains("fail"))
                    || isAssertInstance(cl);
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

    protected CtMethod cloneMethod(CtMethod method, String suffix) {
        CtMethod cloned_method = method.getFactory().Core().clone(method);
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

        testAnnotation = method.getFactory().Core().createAnnotation();
        CtTypeReference<Object> ref = method.getFactory().Core().createTypeReference();
        ref.setSimpleName("Test");

        CtPackageReference refPackage = method.getFactory().Core().createPackageReference();
        refPackage.setSimpleName("org.junit");
        ref.setPackage(refPackage);
        testAnnotation.setAnnotationType(ref);


        Map<String, Object> elementValue = new HashMap<String, Object>();
        elementValue.put("timeout", timeOut);
        testAnnotation.setElementValues(elementValue);

        cloned_method.addAnnotation(testAnnotation);

        return cloned_method;
    }

    protected Set<CtType> computeClassProvider(CtClass testClass) {
        List<CtType> types = Query.getElements(testClass.getParent(CtPackage.class), new TypeFilter(CtType.class));
        types = types.stream()
                .filter(type -> type.getPackage() != null)
                .filter(type -> type.getPackage().getQualifiedName().equals(testClass.getPackage().getQualifiedName()))
                .collect(Collectors.toList());
        types.add(testClass.getParent(CtClass.class));

        ImportScanner importScanner = new ImportScannerImpl();
        types.addAll(types.stream()
                .map(cl -> {
                    try {
                        return importScanner.computeImports(cl);
                    } catch (Exception e) {
                        return new ArrayList<CtTypeReference<?>>();
                    }})
                .flatMap(list -> list.stream())
                .map(typeRef -> typeRef.getDeclaration())
                .collect(Collectors.toSet()));

        return types.stream()
                .filter(type -> type != null)
                .collect(Collectors.toSet());
    }

    protected Random getRandom() {
        if(random == null) {
            random = new Random();
        }
        return random;
    }

    public static Map<CtMethod, CtMethod> getAmpTestToParent() {
        return ampTestToParent;
    }
}
