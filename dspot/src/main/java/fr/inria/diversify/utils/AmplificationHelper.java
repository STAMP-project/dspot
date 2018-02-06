package fr.inria.diversify.utils;

import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.test.listener.TestListener;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtImport;
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
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 */
public class AmplificationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplificationHelper.class);

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static int cloneNumber = 1;

    private static Map<CtMethod, CtMethod> ampTestToParent = new HashMap<>();

    private static Map<CtMethod, CtMethod> tmpAmpTestToParent = new HashMap<>();

    private static Map<CtType, Set<CtType>> importByClass = new HashMap<>();

    private static Random random = new Random(23L);

    private static int timeOutInMs = 10000;

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
        tmpAmpTestToParent.clear();
        ampTestToParent.clear();
        importByClass.clear();
    }

    public static CtType createAmplifiedTest(List<CtMethod<?>> ampTest, CtType<?> classTest) {
        CtType amplifiedTest = classTest.clone();
        final String amplifiedName = classTest.getSimpleName().startsWith("Test") ?
                classTest.getSimpleName() + "Ampl" :
                "Ampl" + classTest.getSimpleName();
        amplifiedTest.setSimpleName(amplifiedName);
        classTest.getMethods().stream().filter(AmplificationChecker::isTest).forEach(amplifiedTest::removeMethod);
        ampTest.forEach(amplifiedTest::addMethod);
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

    public static Map<CtMethod, CtMethod> getAmpTestToParent() {
        return ampTestToParent;
    }

    public static List<CtMethod> updateAmpTestToParent(List<CtMethod> tests, CtMethod parentTest) {
        tests.forEach(test -> tmpAmpTestToParent.put(test, parentTest));
        return tests;
    }

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

    public static CtMethod cloneMethodTest(CtMethod method, String suffix) {
        CtMethod cloned_method = cloneMethod(method, suffix);
        CtAnnotation testAnnotation = cloned_method.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains("Test"))
                .findFirst().orElse(null);

        if (testAnnotation != null) {
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
        elementValue.put("timeout", timeOutInMs);
        testAnnotation.setElementValues(elementValue);

        cloned_method.addAnnotation(testAnnotation);

        return cloned_method;
    }

    public static List<CtMethod<?>> filterTest(List<CtMethod<?>> newTests, TestListener result) {
        final List<String> goodTests = result.getPassingTests().stream()
                .map(Description::getMethodName).collect(Collectors.toList());
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
        while ((topParent = getAmpTestToParent().get(currentTest)) != null) {
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

    public static String getClassPath(DSpotCompiler compiler, InputProgram inputProgram) {
        String classpath = compiler.getBinaryOutputDirectory().getAbsolutePath();
        classpath += PATH_SEPARATOR + inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir();
        classpath += PATH_SEPARATOR + inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir();
        classpath += PATH_SEPARATOR + compiler.getDependencies();
        return classpath;
    }

    //empirically 200 seems to be enough
    public static int MAX_NUMBER_OF_TESTS = 200;


    // What we want here, is diversity.
    // We use the hashCode of CtMethod to do this
    // We select CtMethod that have very different hashCode
    // We use the Standard deviation and takes only CtMethod that have hashcode different of at least this value
    public static List<CtMethod<?>> reduce(List<CtMethod<?>> newTests) {
        if (newTests.size() > MAX_NUMBER_OF_TESTS) {
            LOGGER.warn("Too many tests has been generated: {}", newTests.size());
            final List<List<Double>> distances = newTests.stream()
                    .map(test1 -> newTests.stream()
                            .map(test2 -> (double) levenshteinDistance(test1.toString(), test2.toString()))
                            .collect(Collectors.toList())
                    ).collect(Collectors.toList());

            final List<Double> averages = newTests.stream()
                    .map(test1 -> newTests.stream()
                            .filter(ctMethod -> !ctMethod.equals(test1))
                            .map(test2 -> (double) levenshteinDistance(test1.toString(), test2.toString()))
                            .mapToDouble(value -> value)
                            .average()
                            .orElse(0.0D)
                    ).collect(Collectors.toList());

            final List<CtMethod<?>> reducedTests = averages.stream()
                    .sorted((o1, o2) -> - Double.compare(o1, o2))
                    .limit(MAX_NUMBER_OF_TESTS)
                    .map(averages::indexOf)
                    .map(newTests::get)
                    .collect(Collectors.toList());
            LOGGER.info("Number of generated test reduced to {}", reducedTests.size());
            return reduce(reducedTests);
        } else {
            ampTestToParent.putAll(newTests.stream()
                    .collect(HashMap::new,
                            (parentsReduced, ctMethod) -> parentsReduced.put(ctMethod, tmpAmpTestToParent.get(ctMethod)),
                            HashMap::putAll)
            );
            tmpAmpTestToParent.clear();
            return newTests;
        }
    }

    // Retrieve from https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
    private static Integer levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

}
