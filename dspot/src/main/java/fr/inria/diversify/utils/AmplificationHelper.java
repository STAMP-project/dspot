package fr.inria.diversify.utils;

import eu.stamp.project.testrunner.runner.test.TestListener;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.compilation.DSpotCompiler;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.minimization.Minimizer;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.ImportScanner;
import spoon.reflect.visitor.ImportScannerImpl;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.declaration.CtClassImpl;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 */
public class AmplificationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplificationHelper.class);

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final char DECIMAL_SEPARATOR = (((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator());

    private static int cloneNumber = 1;

    /**
     * Link between an amplified test and its parent (i.e. the original test).
     */
    private static Map<CtMethod<?>, CtMethod> ampTestToParent = new IdentityHashMap<>();

    @Deprecated
    private static Map<CtType, Set<CtType>> importByClass = new HashMap<>();

    private static Random random = new Random(23L);

    private static int timeOutInMs = 10000;

    public static boolean minimize;

    public static void setTimeOutInMs(int newTimeOutInMs) {
        timeOutInMs = newTimeOutInMs;
    }

    public static int getTimeOutInMs() {
        return timeOutInMs;
    }

    public static void setSeedRandom(long seed) {
        random = new Random(seed);
    }

    public static Random getRandom() {
        return random;
    }

    public static void reset() {
        cloneNumber = 1;
        ampTestToParent.clear();
        importByClass.clear();
    }

    public static CtType createAmplifiedTest(List<CtMethod<?>> ampTest, CtType<?> classTest, Minimizer minimizer) {
        CtType amplifiedTest = classTest.clone();
        final String amplifiedName = classTest.getSimpleName().startsWith("Test") ?
                classTest.getSimpleName() + "Ampl" :
                "Ampl" + classTest.getSimpleName();
        amplifiedTest.setSimpleName(amplifiedName);
        classTest.getMethods().stream().filter(AmplificationChecker::isTest).forEach(amplifiedTest::removeMethod);
        if (minimize) {
            ampTest.stream().map(minimizer::minimize).forEach(amplifiedTest::addMethod);
        } else {
            ampTest.forEach(amplifiedTest::addMethod);
        }
        final CtTypeReference classTestReference = classTest.getReference();
        amplifiedTest.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
            @Override
            public boolean matches(CtTypeReference element) {
                return element.equals(classTestReference) && super.matches(element);
            }
        }).forEach(ctTypeReference -> ctTypeReference.setSimpleName(amplifiedName));
        classTest.getPackage().addType(amplifiedTest);
        return amplifiedTest;
    }

    /**
     * Clones the test class and adds the test methods.
     *
     * @param original Test class
     * @param methods  Test methods
     * @return Test class with new methods
     */
    public static CtType cloneTestClassAndAddGivenTest(CtType original, List<CtMethod<?>> methods) {
        CtType clone = original.clone();
        original.getPackage().addType(clone);
        methods.forEach(clone::addMethod);
        return clone;
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

    /**
     * <p>Convert a JUnit3 test class into a JUnit4.
     * This is done in two steps:
     * <ol>
     * <li>Remove the "extends TestCase"</li>
     * <li>Add an @Test annotation, with a default value for the timeout</li>
     * </ol>
     * The timeout is added at this step since the converted test classes, and its test methods,
     * will be amplified.
     * This method convert also super classes in case they inherit from TestCase.
     * This method recompile every converted test class, because they will be executed.
     * </p>
     *
     * @param testClassJUnit3 test class to be converted
     * @return the same test class but in JUnit4
     */
    @SuppressWarnings("unchecked")
    public static CtType<?> convertToJUnit4(CtType<?> testClassJUnit3,
                                            InputConfiguration configuration,
                                            InputProgram program) {
        if (AmplificationChecker.isTestJUnit4(testClassJUnit3)) {
            return testClassJUnit3;
        }
        final Factory factory = testClassJUnit3.getFactory();

        // convert setUp and tearDown
        convertGivenMethodWithGivenClass(testClassJUnit3, "setUp", Before.class);
        convertGivenMethodWithGivenClass(testClassJUnit3, "tearDown", After.class);

        // remove "extends TestCases"
        if (AmplificationChecker.inheritFromTestCase(testClassJUnit3)) {
            ((CtClassImpl) testClassJUnit3).setSuperclass(null);
        } else {
            if (testClassJUnit3.getSuperclass() != null) {
                CtType<?> superclass = testClassJUnit3.getSuperclass().getDeclaration();
                while (superclass != null) {
                    if (AmplificationChecker.inheritFromTestCase(superclass)) {
                        final CtType<?> convertedSuperclass =
                                AmplificationHelper.convertToJUnit4(superclass, configuration, program);
                        DSpotUtils.printCtTypeToGivenDirectory(convertedSuperclass,
                                new File(program.getProgramDir() + "/" + program.getTestClassesDir()));
                        final String classpath = AutomaticBuilderFactory
                                .getAutomaticBuilder(configuration)
                                .buildClasspath(program.getProgramDir())
                                + AmplificationHelper.PATH_SEPARATOR +
                                program.getProgramDir() + "/" + program.getClassesDir()
                                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/"
                                + AmplificationHelper.PATH_SEPARATOR +
                                program.getProgramDir() + "/" + program.getTestClassesDir();

                        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources, classpath,
                                new File(program.getProgramDir() + "/" + program.getTestClassesDir()));
                    }
                    if (superclass.getSuperclass() == null) {
                        break;
                    }
                    superclass = superclass.getSuperclass().getDeclaration();
                }
            }
        }

        // convertToJUnit4 JUnit3 into JUnit4 test methods
        testClassJUnit3
                .getElements(AmplificationChecker.IS_TEST_TYPE_FILTER)
                .forEach(testMethod ->
                        AmplificationHelper.prepareTestMethod(testMethod, factory)
                );

        // convert call to junit.framework.Assert calls to org.junit.Assert
        testClassJUnit3.filterChildren(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation<?> invocation) {
                return invocation.getTarget() != null &&
                        (invocation.getExecutable().getSimpleName().startsWith("assert") ||
                                invocation.getExecutable().getSimpleName().startsWith("fail")) &&
                        invocation.getTarget().getReferencedTypes().stream()
                                .anyMatch(reference ->
                                        reference.equals(
                                                factory.Type().createReference(junit.framework.TestCase.class)
                                        ) || reference.equals(
                                                factory.Type().createReference(junit.framework.Assert.class)
                                        ));
            }
        }).forEach(invocation -> ((CtInvocation) invocation).setTarget(
                factory.createTypeAccess(factory.Type().createReference(org.junit.Assert.class))
                )
        );
        return testClassJUnit3;
    }

    private static void convertGivenMethodWithGivenClass(CtType<?> testClass, String methodName,
                                                         final Class annotationClass) {
        testClass.getElements(new FILTER_OVERRIDE_METHOD_WITH_NAME(methodName))
                .forEach(ctMethod -> {
                    ctMethod.removeModifier(ModifierKind.PROTECTED);
                    ctMethod.addModifier(ModifierKind.PUBLIC);
                    ctMethod.removeAnnotation(ctMethod.getAnnotations().get(0));
                    testClass.getFactory().Annotation().annotate(ctMethod, annotationClass);
                    if (AmplificationChecker.inheritFromTestCase(testClass)) {
                        ctMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                            @Override
                            public boolean matches(CtInvocation<?> element) {
                                return element.getTarget() instanceof CtSuperAccess;
                            }
                        }).forEach(ctMethod.getBody()::removeStatement);
                    }
                });
    }

    private final static class FILTER_OVERRIDE_METHOD_WITH_NAME extends TypeFilter<CtMethod<?>> {
        private final String name;

        FILTER_OVERRIDE_METHOD_WITH_NAME(String name) {
            super(CtMethod.class);
            this.name = name;
        }

        @Override
        public boolean matches(CtMethod<?> element) {
            return element.getAnnotations().size() == 1 &&
                    element.getAnnotation(Override.class) != null &&
                    element.getSimpleName().equals(this.name);
        }
    }


    /**
     * Clones a method.
     *
     * @param method Method to be cloned
     * @param suffix Suffix for the cloned method's name
     * @return The cloned method
     */
    private static CtMethod cloneMethod(CtMethod method, String suffix) {
        CtMethod cloned_method = method.clone();
        //rename the clone
        cloned_method.setSimpleName(method.getSimpleName() + (suffix.isEmpty() ? "" : suffix + cloneNumber));
        cloneNumber++;

        CtAnnotation toRemove = cloned_method.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains("Override"))
                .findFirst().orElse(null);

        if (toRemove != null) {
            cloned_method.removeAnnotation(toRemove);
        }
        return cloned_method;
    }

    /**
     * Clones a test method.
     * <p>
     * Performs necessary integration with JUnit and adds timeout.
     *
     * @param method Method to be cloned
     * @param suffix Suffix for the cloned method's name
     * @return The cloned method
     */
    private static CtMethod cloneTestMethod(CtMethod method, String suffix) {
        CtMethod cloned_method = cloneMethod(method, suffix);
        CtAnnotation testAnnotation = cloned_method.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains("Test"))
                .findFirst().orElse(null);
        if (testAnnotation != null) {
            cloned_method.removeAnnotation(testAnnotation);
        }
        final Factory factory = method.getFactory();
        prepareTestMethod(cloned_method, factory);
        return cloned_method;
    }

    public static void prepareTestMethod(CtMethod cloned_method, Factory factory) {
        CtAnnotation testAnnotation;
        testAnnotation = factory.Core().createAnnotation();
        CtTypeReference<Object> ref = factory.Core().createTypeReference();
        ref.setSimpleName("Test");

        CtPackageReference refPackage = factory.Core().createPackageReference();
        refPackage.setSimpleName("org.junit");
        ref.setPackage(refPackage);
        testAnnotation.setAnnotationType(ref);

        Map<String, Object> elementValue = new HashMap<>();
        elementValue.put("timeout", timeOutInMs);
        testAnnotation.setElementValues(elementValue);

        cloned_method.addAnnotation(testAnnotation);

        cloned_method.addThrownType(factory.Type().createReference(Exception.class));
    }

    public static CtMethod cloneTestMethodForAmp(CtMethod method, String suffix) {
        CtMethod clonedMethod = cloneTestMethod(method, suffix);
        ampTestToParent.put(clonedMethod, method);
        return clonedMethod;
    }

    public static CtMethod cloneTestMethodNoAmp(CtMethod method) {
        return cloneTestMethod(method, "");
    }

    public static List<CtMethod<?>> getPassingTests(List<CtMethod<?>> newTests, TestListener result) {
        final List<String> passingTests = result.getPassingTests();
        return newTests.stream()
                .filter(test -> passingTests.contains(test.getSimpleName()))
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
        char c = (char) ((value == 34 || value == 39 || value == 92) ? value + (getRandom().nextBoolean() ? 1 : -1) : value);
        return c;//discarding " ' and \
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

    public static String getClassPath(DSpotCompiler compiler, InputConfiguration configuration) {
        return Arrays.stream(new String[]{
                        compiler.getBinaryOutputDirectory().getAbsolutePath(),
                        configuration.getInputProgram().getProgramDir() + "/" + configuration.getInputProgram().getClassesDir(),
                        compiler.getDependencies(),
                }
        ).collect(Collectors.joining(PATH_SEPARATOR));
    }

    //empirically 200 seems to be enough
    public static int MAX_NUMBER_OF_TESTS = 200;

    /**
     * Reduces the number of amplified tests to a practical threshold (see {@link #MAX_NUMBER_OF_TESTS}).
     * <p>
     * <p>The reduction aims at keeping a maximum of diversity. Because all the amplified tests come from the same
     * original test, they have a <em>lot</em> in common.
     * <p>
     * <p>Diversity is measured with the textual representation of amplified tests. We use the sum of the bytes returned
     * by the {@link String#getBytes()} method and keep the amplified tests with the most distant values.
     *
     * @param tests List of tests to be reduced
     * @return A subset of the input tests
     */
    public static List<CtMethod<?>> reduce(List<CtMethod<?>> tests) {
        final List<CtMethod<?>> reducedTests = new ArrayList<>();
        if (tests.size() > MAX_NUMBER_OF_TESTS) {
            LOGGER.warn("Too many tests have been generated: {}", tests.size());
            final Map<Long, List<CtMethod<?>>> valuesToMethod = new HashMap<>();
            for (CtMethod<?> test : tests) {
                final long value = AmplificationHelper.sumByteArrayToLong(test.toString().getBytes());
                if (!valuesToMethod.containsKey(value)) {
                    valuesToMethod.put(value, new ArrayList<>());
                }
                valuesToMethod.get(value).add(test);
            }
            final Long average = average(valuesToMethod.keySet());
            while (reducedTests.size() < MAX_NUMBER_OF_TESTS) {
                final Long furthest = furthest(valuesToMethod.keySet(), average);
                reducedTests.add(valuesToMethod.get(furthest).get(0));
                if (valuesToMethod.get(furthest).isEmpty()) {
                    valuesToMethod.remove(furthest);
                } else {
                    valuesToMethod.get(furthest).remove(0);
                    if (valuesToMethod.get(furthest).isEmpty()) {
                        valuesToMethod.remove(furthest);
                    }
                }
            }
            LOGGER.info("Number of generated test reduced to {}", reducedTests.size());
        }
        if (reducedTests.isEmpty()) {
            reducedTests.addAll(tests);
        } else {
            tests.stream()
                    .filter(test -> !reducedTests.contains(test))
                    .forEach(discardedTest -> ampTestToParent.remove(discardedTest));
        }
        return reducedTests;
    }

    /**
     * Returns the average of a collection of double
     */
    private static Long average(Collection<Long> values) {
        return values.stream().collect(Collectors.averagingLong(Long::longValue)).longValue();
    }

    /**
     * Returns the first, most distant element of a collection from a defined value.
     */
    private static Long furthest(Collection<Long> values, Long average) {
        return values.stream()
                .max(Comparator.comparingLong(d -> Math.abs(d - average)))
                .orElse(null);
    }

    private static long sumByteArrayToLong(byte[] byteArray) {
        long sum = 0L;
        for (byte aByteArray : byteArray) {
            sum += (int) aByteArray;
        }
        return sum;
    }

    public static final TypeFilter<CtInvocation<?>> ASSERTIONS_FILTER = new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
        @Override
        public boolean matches(CtInvocation<?> element) {
            return AmplificationChecker.isAssert(element);
        }
    };

}
