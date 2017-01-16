package fr.inria.diversify.dspot;

import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.JunitResult;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.ImportScanner;
import spoon.reflect.visitor.ImportScannerImpl;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 */
public class AmplificationHelper {

    private static int cloneNumber = 1;
    private static Map<CtMethod,CtMethod> ampTestToParent = new HashMap<>();
    private static Map<CtType, Set<CtType>> importByClass = new HashMap<>();
    private static Random random = new Random();

    public static void setSeedRandom(long seed) {
        random = new Random(seed);
    }

    public static Random getRandom() {
        return random;
    }

    public static void reset() {
        cloneNumber = 1;
        ampTestToParent = new HashMap<>();
        importByClass = new HashMap<>();
    }

    public static CtType createAmplifiedTest(List<CtMethod> ampTest, CtType classTest) {
        CtType amplifiedTest = classTest.clone();
        amplifiedTest.setParent(classTest.getParent());
        amplifiedTest.setSimpleName(classTest.getSimpleName() + "Ampl");
        classTest.getMethods().forEach(method -> amplifiedTest.removeMethod((CtMethod) method));
        ampTest.forEach(classTest::addMethod);
        return classTest;
    }

    public static Map<CtMethod, CtMethod> getAmpTestToParent() {
        return ampTestToParent;
    }

    public static List<CtMethod> updateAmpTestToParent(List<CtMethod> tests, CtMethod parentTest) {
        tests.forEach(test -> ampTestToParent.put(test, parentTest));
        return tests;
    }

    public static Set<CtType> computeClassProvider(CtType testClass) {
        List<CtType> types = Query.getElements(testClass.getParent(CtPackage.class), new TypeFilter(CtType.class));
        types = types.stream()
                .filter(type -> type != null)
                .filter(type -> type.getPackage() != null)
                .filter(type -> type.getPackage().getQualifiedName().equals(testClass.getPackage().getQualifiedName()))
                .collect(Collectors.toList());

        if(testClass.getParent(CtType.class) != null) {
            types.add(testClass.getParent(CtType.class));
        }

        types.addAll(types.stream()
                .flatMap(type -> getImport(type).stream())
                .collect(Collectors.toSet()));

        return types.stream()
                .collect(Collectors.toSet());
    }

    public static Set<CtType> getImport(CtType type) {
        if(!AmplificationHelper.importByClass.containsKey(type)) {
            ImportScanner importScanner = new ImportScannerImpl();
            try {
                Set<CtType> set = importScanner.computeImports(type).stream()
                        .map(CtTypeReference::getDeclaration)
                        .filter(t -> t != null)
                        .collect(Collectors.toSet());
                AmplificationHelper.importByClass.put(type,set);
            } catch (Exception e) {
                AmplificationHelper.importByClass.put(type, new HashSet<>(0));
            }
        }
        return AmplificationHelper.importByClass.get(type);
    }

    public static CtMethod cloneMethod(CtMethod method, String suffix) {
        CtMethod cloned_method = method.clone();
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

    public static CtMethod cloneMethodTest(CtMethod method, String suffix, int timeOut) {
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

        Map<String, Object> elementValue = new HashMap<>();
        elementValue.put("timeout", timeOut);
        testAnnotation.setElementValues(elementValue);

        cloned_method.addAnnotation(testAnnotation);

        return cloned_method;
    }

    public static List<CtMethod> filterTest(List<CtMethod> newTests, JunitResult result) {
        final List<String> goodTests = result.goodTests();
        return newTests.stream()
                .filter(test -> goodTests.contains(test.getSimpleName()))
                .collect(Collectors.toList());
    }

    public static String getRandomString(int length) {
        return IntStream.range(0, length)
                .map(i -> getRandomChar())
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static char getRandomChar() {
        int value = getRandom().nextInt(94) + 32;
        char c = (char) ((value == 34 || value == 39) ? value + (getRandom().nextBoolean() ? 1 : -1) : value);
        return c;//excluding " and '
    }

    public static CtMethod addOriginInComment(CtMethod amplifiedTest, CtMethod topParent) {
        DSpotUtils.addComment(amplifiedTest,
                "amplification of " + topParent.getDeclaringType().getQualifiedName() + "#" + topParent.getSimpleName(),
                CtComment.CommentType.BLOCK);
        return amplifiedTest;
    }

    public static CtMethod getTopParent(CtMethod test) {
        CtMethod topParent;
        CtMethod currentTest = test;
        while ( (topParent = getAmpTestToParent().get(currentTest)) != null) {
            currentTest = topParent;
        }
        return currentTest;
    }

    public static List<CtMethod> getAllTest(InputProgram inputProgram, CtType classTest) {
        Set<CtMethod> mths = classTest.getMethods();
        return mths.stream()
                .filter(mth -> AmplificationChecker.isTest(mth, inputProgram.getRelativeTestSourceCodeDir()))
                .distinct()
                .collect(Collectors.toList());
    }
}
