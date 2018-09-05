package eu.stamp_project.utils;

import eu.stamp_project.minimization.Minimizer;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.testrunner.runner.test.TestListener;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtInvocation;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    /**
     * Link between an amplified test and its parent (i.e. the original test).
     */
    public static Map<CtMethod<?>, CtMethod> ampTestToParent = new IdentityHashMap<>();

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
        if (InputConfiguration.get().shouldMinimize()) {
            final Minimizer minimizer = InputConfiguration.get().getSelector().getMinimizer();
            methodToAdd = ampTest.stream().map(minimizer::minimize);
        } else {
            methodToAdd = ampTest.stream();
        }
        if (InputConfiguration.get().isGenerateAmplifiedTestClass()) {
            CtType amplifiedTest = classTest.clone();
            final String amplifiedName = getAmplifiedName(classTest);
            amplifiedTest.setSimpleName(amplifiedName);
            classTest.getMethods().stream().filter(AmplificationChecker::isTest).forEach(amplifiedTest::removeMethod);
            methodToAdd.forEach(amplifiedTest::addMethod);
            final CtTypeReference classTestReference = classTest.getReference();
            // renaming all the Spoon nodes
            amplifiedTest.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
                @Override
                public boolean matches(CtTypeReference element) {
                    return element.equals(classTestReference) && super.matches(element);
                }
            }).forEach(ctTypeReference -> ctTypeReference.setSimpleName(getAmplifiedName(classTest)));
            // need to update also all the String literals
            amplifiedTest.getElements(new TypeFilter<CtLiteral>(CtLiteral.class) {
                @Override
                public boolean matches(CtLiteral element) {
                    return element.getValue() instanceof String &&
                            ((String)element.getValue()).contains(classTest.getSimpleName());
                }
            }).forEach(stringCtLiteral ->
                    stringCtLiteral.setValue(((String)stringCtLiteral.getValue()).replaceAll(classTest.getSimpleName(), amplifiedName))
            );
            classTest.getPackage().addType(amplifiedTest);
            return amplifiedTest;
        } else {
            methodToAdd.forEach(classTest::addMethod);
            return classTest;
        }
    }

    @NotNull
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

    public static List<CtMethod<?>> getPassingTests(List<CtMethod<?>> newTests, TestListener result) {
        final List<String> passingTests = result.getPassingTests();
        return newTests.stream()
                .filter(test -> passingTests.contains(test.getSimpleName()))
                .collect(Collectors.toList());
    }

    public static CtMethod<?> addOriginInComment(CtMethod<?> amplifiedTest, CtMethod<?> topParent) {
        DSpotUtils.addComment(amplifiedTest,
                "amplification of " +
                        (topParent.getDeclaringType() != null ?
                                topParent.getDeclaringType().getQualifiedName() + "#" : "") + topParent.getSimpleName(),
                CtComment.CommentType.BLOCK);
        return amplifiedTest;
    }

    public static CtMethod getTopParent(CtMethod test) {
        CtMethod topParent;
        CtMethod currentTest = test;
        while ((topParent = getAmpTestParent(currentTest)) != null) {
            currentTest = topParent;
        }
        return currentTest;
    }

    public static List<CtMethod<?>> getAllTest(CtType<?> classTest) {
        Set<CtMethod<?>> methods = classTest.getMethods();
        return methods.stream()
                .filter(AmplificationChecker::isTest)
                .distinct()
                .collect(Collectors.toList());
    }

    @Deprecated
    public static String getClassPath(DSpotCompiler compiler, InputConfiguration configuration) {
        return Arrays.stream(new String[]{
                        compiler.getBinaryOutputDirectory().getAbsolutePath(),
                        configuration.getAbsolutePathToClasses(),
                        compiler.getDependencies(),
                }
        ).collect(Collectors.joining(PATH_SEPARATOR));
    }

    public static final TypeFilter<CtInvocation<?>> ASSERTIONS_FILTER = new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
        @Override
        public boolean matches(CtInvocation<?> element) {
            return AmplificationChecker.isAssert(element);
        }
    };

}
