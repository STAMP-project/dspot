package eu.stamp_project.utils;

import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.testrunner.listener.TestResult;

import org.apache.cxf.common.util.WeakIdentityHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.ImportScanner;
import spoon.reflect.visitor.ImportScannerImpl;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 */
public class AmplificationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplificationHelper.class);

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final char DECIMAL_SEPARATOR = (((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator());

    public static int timeOutInMs = 10000;

    private static boolean shouldKeepOriginalTestMethods;

    private static boolean shouldGenerateAmplifiedTestClass;

    public static void init(int timeOutInMs,
            boolean shouldGenerateAmplifiedTestClass,
            boolean shouldKeepOriginalTestMethods) {
        AmplificationHelper.timeOutInMs = timeOutInMs;
        AmplificationHelper.shouldKeepOriginalTestMethods = shouldKeepOriginalTestMethods;
        AmplificationHelper.shouldGenerateAmplifiedTestClass = shouldGenerateAmplifiedTestClass;
    }

    /**
     * Link between an amplified test and its parent (i.e. the original test).
     */
    public static Map<CtMethod<?>, CtMethod> ampTestToParent = Collections.synchronizedMap(new WeakIdentityHashMap<>());


    //(Optimisation) - trace bounds between cloned test methods and their original ones
    //This additional map is added (and ampTestToParent is not used) because
    //existing map does not account for all cloned test methods
    private static Map<CtMethod<?>, CtMethod> originalTestBindings = Collections.synchronizedMap(new WeakIdentityHashMap<>());

    @Deprecated
    private static Map<CtType, Set<CtType>> importByClass = new HashMap<>();

    public static void reset() {
        CloneHelper.reset();
        ampTestToParent.clear();
        importByClass.clear();
    }

    @SuppressWarnings("unchecked")
    public static CtType<?> createAmplifiedTest(List<CtMethod<?>> ampTest, CtType<?> classTest) {
        final Stream<CtMethod<?>> methodToAdd;
        methodToAdd = ampTest.stream();
        final CtType<?> currentTestClass = classTest.clone();
        classTest.getPackage().addType(currentTestClass);
        methodToAdd.forEach(currentTestClass::addMethod);
        // keep original test methods
        if (!shouldKeepOriginalTestMethods) {
            classTest.getMethods().stream()
                    .filter(TestFramework.get()::isTest)
                    //.filter(AmplificationChecker::isTest)
                    .forEach(currentTestClass::removeMethod);
        }
        return currentTestClass;
    }

    public static CtType<?> renameTestClassUnderAmplification(CtType<?> classTest) {
        final CtType<?> currentTestClass = classTest.clone();
        // generate a new test class
        if (shouldGenerateAmplifiedTestClass) {
            final String amplifiedName = getAmplifiedName(classTest);
            currentTestClass.setSimpleName(amplifiedName);
            final CtTypeReference classTestReference = classTest.getReference();
            // renaming all the Spoon nodes
            currentTestClass.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
                @Override
                public boolean matches(CtTypeReference element) {
                    return element.equals(classTestReference);
                }
            }).forEach(ctTypeReference -> ctTypeReference.setSimpleName(amplifiedName));
            // need to update also all the String literals
            currentTestClass.getElements(new TypeFilter<CtLiteral>(CtLiteral.class) {
                @Override
                public boolean matches(CtLiteral element) {
                    return element.getValue() instanceof String &&
                            ((String) element.getValue()).contains(classTest.getSimpleName());
                }
            }).forEach(stringCtLiteral ->
                    stringCtLiteral.setValue(((String) stringCtLiteral.getValue()).replaceAll(classTest.getSimpleName(), amplifiedName))
            );
        }
        classTest.getPackage().addType(currentTestClass);
        return currentTestClass;
    }

    private static String getAmplifiedName(CtType<?> classTest) {
        return classTest.getSimpleName().startsWith("Test") ?
                classTest.getSimpleName() + "Ampl" :
                "Ampl" + classTest.getSimpleName();
    }

    public static CtMethod getAmpTestParent(CtMethod amplifiedTest) {
        return ampTestToParent.get(amplifiedTest);
    }

    public static CtMethod removeAmpTestParent(CtMethod amplifiedTest) {
        return ampTestToParent.remove(amplifiedTest);
    }

    public static int getAmpTestParentSize() {
        return ampTestToParent.size();
    }

    public static void addTestBindingToOriginal(CtMethod clonedTest, CtMethod fromTest) {
        CtMethod originalTest = fromTest;
        if (originalTestBindings.containsKey(fromTest)) {
            originalTest = originalTestBindings.get(fromTest);
        }
        originalTestBindings.put(clonedTest, originalTest);
    }

    public static void removeTestBindingToOriginal(CtMethod clonedTest) {
        originalTestBindings.remove(clonedTest);
    }

    public static CtMethod getOriginalTestMethod(CtMethod clonedTest) {
        return originalTestBindings.get(clonedTest);
    }

    public static int getTestBindingToOriginalSize() {
        return originalTestBindings.size();
    }

    public static void resetTestBindingToOriginal() {
          originalTestBindings.clear();
    }

    @Deprecated
    public static Set<CtType> computeClassProvider(CtType testClass) {
        List<CtType> types = Query.getElements(testClass.getParent(CtPackage.class), new TypeFilter(CtType.class));
        types = types.stream()
                .filter(Objects::nonNull)
                .filter(type -> type.getPackage() != null)
                .filter(type -> type.getPackage().getQualifiedName().equals(testClass.getPackage().getQualifiedName()))
                .collect(Collectors.toList());

        if (testClass.getParent(CtType.class) != null) {
            types.add(testClass.getParent(CtType.class));
        }

        types.addAll(types.stream()
                .flatMap(type -> getImport(type).stream())
                .collect(Collectors.toSet()));


        return new HashSet<>(types);
    }

    @Deprecated
    public static Set<CtType> getImport(CtType type) {
        if (!AmplificationHelper.importByClass.containsKey(type)) {
            ImportScanner importScanner = new ImportScannerImpl();
            try {
                importScanner.computeImports(type);
                Set<CtType> set = importScanner.getAllImports()
                        .stream()
                        .map(CtImport::getReference)
                        .filter(Objects::nonNull)
                        .filter(ctElement -> ctElement instanceof CtType)
                        .map(ctElement -> (CtType) ctElement)
                        .collect(Collectors.toSet());
                AmplificationHelper.importByClass.put(type, set);
            } catch (Exception e) {
                AmplificationHelper.importByClass.put(type, new HashSet<>(0));
            }
        }
        return AmplificationHelper.importByClass.get(type);
    }

    @Deprecated
    public static List<CtMethod<?>> getPassingTests(List<CtMethod<?>> newTests, TestResult result) {
        final List<String> passingTests = result.getPassingTests();
        return newTests.stream()
                .filter(test -> {
                    final Pattern pattern = Pattern.compile(test.getSimpleName() + "\\[\\d+\\]");
                    return passingTests.contains(test.getSimpleName()) ||
                            passingTests.stream().anyMatch(passingTest -> pattern.matcher(passingTest).matches());
                }).collect(Collectors.toList());
    }

    public static CtMethod<?> addOriginInComment(CtMethod<?> amplifiedTest, CtMethod<?> topParent) {
        DSpotUtils.addComment(amplifiedTest,
                "amplification of " +
                        (topParent.getDeclaringType() != null ?
                                topParent.getDeclaringType().getQualifiedName() + "#" : "") + topParent.getSimpleName(),
                CtComment.CommentType.BLOCK);
        return amplifiedTest;
    }

    @Deprecated
    public static CtMethod getTopParent(CtMethod test) {
        CtMethod topParent;
        CtMethod currentTest = test;
        while ((topParent = getAmpTestParent(currentTest)) != null) {
            currentTest = topParent;
        }
        return currentTest;
    }
}
