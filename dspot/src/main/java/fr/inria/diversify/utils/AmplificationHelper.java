package fr.inria.diversify.utils;

import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.stamp.minimization.Minimizer;
import fr.inria.diversify.dspot.support.Counter;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
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

    private static int cloneNumber = 1;

    /**
     * Link between an amplified test and its parent (i.e. the original test).
     */
    private static Map<CtMethod, CtMethod> ampTestToParent = new HashMap<>();

    /**
     * Buffer for {@link #ampTestToParent} to avoid adding discarded tests.
     */
    private static Map<CtMethod, CtMethod> tmpAmpTestToParent = new HashMap<>();

    @Deprecated
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

    public static CtType createAmplifiedTest(List<CtMethod<?>> ampTest, CtType<?> classTest, Minimizer minimizer) {
        CtType amplifiedTest = classTest.clone();
        final String amplifiedName = classTest.getSimpleName().startsWith("Test") ?
                classTest.getSimpleName() + "Ampl" :
                "Ampl" + classTest.getSimpleName();
        amplifiedTest.setSimpleName(amplifiedName);
        classTest.getMethods().stream().filter(AmplificationChecker::isTest).forEach(amplifiedTest::removeMethod);
        ampTest.stream().map(minimizer::minimize).forEach(amplifiedTest::addMethod);
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
     * @param methods Test methods
     * @return Test class with new methods
     */
    public static CtType cloneTestClassAndAddGivenTest(CtType original, List<CtMethod<?>> methods) {
        CtType clone = original.clone();
        original.getPackage().addType(clone);
        methods.forEach(clone::addMethod);
        return clone;
    }

    public static CtMethod getAmpTestParent(CtMethod ampTest) {
        return ampTestToParent.get(ampTest);
    }

    /**
     * Load all the buffer.
     */
    private static void loadParentMapBuffer() {
        ampTestToParent.putAll(tmpAmpTestToParent);
        tmpAmpTestToParent.clear();
    }

    /**
     * Load only a subset of the buffer.
     *
     * @param testsToKeep Test methods to load
     */
    private static void loadParentMapBuffer(List<CtMethod<?>> testsToKeep) {
        ampTestToParent.putAll(testsToKeep.stream()
                .collect(HashMap::new,
                        (accumulator, testMethod) -> accumulator.put(testMethod, tmpAmpTestToParent.get(testMethod)),
                        HashMap::putAll)
        );
        tmpAmpTestToParent.clear();
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
     *
     * Performs necessary integration with JUnit and adds timeout.
     *
     * @param method Method to be cloned
     * @param suffix Suffix for the cloned method's name
     * @return The cloned method
     */
    private static CtMethod cloneMethodTest(CtMethod method, String suffix) {
        CtMethod cloned_method = cloneMethod(method, suffix);
        tmpAmpTestToParent.put(cloned_method, method);

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

        cloned_method.addThrownType(method.getFactory().Type().createReference(Exception.class));

        return cloned_method;
    }

    /**
     * Clones a test method for input-amplification purposes.
     *
     * Updates the counter of amplifications applied, assuming one cloning per amplification.
     *
     * See {@link #cloneMethodTest(CtMethod, String)} for the cloning details.
     *
     * @param method Method to be cloned
     * @param suffix Suffix for the cloned method's name
     * @return The cloned method
     */
    public static CtMethod cloneMethodTestIAmp(CtMethod method, String suffix) {
        CtMethod cloned_method = cloneMethodTest(method, suffix);
        Counter.updateInputOf(cloned_method, 1);
        return cloned_method;
    }

    /**
     * Clones a test method for assertion-amplification purposes.
     *
     * Updates the counter of amplifications applied, assuming one cloning per amplification.
     *
     * See {@link #cloneMethodTest(CtMethod, String)} for the cloning details.
     *
     * @param method Method to be cloned
     * @param suffix Suffix for the cloned method's name
     * @return The cloned method
     */
    public static CtMethod cloneMethodTestAAmp(CtMethod method, String suffix) {
        CtMethod cloned_method = cloneMethodTest(method, suffix);
        Counter.updateAssertionOf(cloned_method, 1);
        loadParentMapBuffer();  // load directly because these tests will not be discarded
        return cloned_method;
    }

    public static List<CtMethod<?>> getPassingTests(List<CtMethod<?>> newTests, TestListener result) {
        final List<String> passingTests = result.getPassingTests()
                .stream()
                .map(Description::getMethodName)
                .collect(Collectors.toList());
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
        return Arrays.stream(new String[] {
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
     *
     * The reduction aims at keeping a maximum of diversity.
     *
     * Diversity is measured with the textual representation of amplified tests. Since all the amplified tests come from
     * the same original test, they have a <em>lot</em> in common. We use the sum of the bytes returned by the
     * {@link String#getBytes()} method and then compute the standard deviation on this sum. We only keep amplified
     * tests with a byte sum greater or equal to this standard deviation.
     *
     * @param tests List of tests to be reduced
     * @return A subset of the input tests
     */
    public static List<CtMethod<?>> reduce(List<CtMethod<?>> tests) {
        final List<CtMethod<?>> reducedTests = new ArrayList<>();
        if (tests.size() > MAX_NUMBER_OF_TESTS) {
            LOGGER.warn("Too many tests has been generated: {}", tests.size());
            reducedTests.addAll(_reduce(tests));
            if (reducedTests.size() > MAX_NUMBER_OF_TESTS) {
                return reduce(reducedTests);
            }
            tests.removeAll(reducedTests);
            List<CtMethod<?>> tmp = _reduce(tests);
            while (tmp.size() + reducedTests.size() < MAX_NUMBER_OF_TESTS) { // loop until we have the max number of tests
                reducedTests.addAll(tmp);
                tests.removeAll(reducedTests);
                tmp = _reduce(tests);
            }
            LOGGER.info("Number of generated test reduced to {}", reducedTests.size());
        }
        if (reducedTests.isEmpty()) {
            reducedTests.addAll(tests);
        }
        loadParentMapBuffer(reducedTests);
        return reducedTests;
    }

    private static List<CtMethod<?>> _reduce(List<CtMethod<?>> tests) {
        final List<Long> values = tests.stream()
                .map(CtMethod::toString)
                .map(String::getBytes)
                .map(AmplificationHelper::convert)
                .collect(Collectors.toList()); // compute the sum of toString().getBytes()
        final double standardDeviation = standardDeviation(values);
        final List<CtMethod<?>> reducedTests = values.stream()
                .filter(integer -> Math.abs(values.get(0) - integer) >= standardDeviation)
                .map(values::indexOf)
                .map(tests::get)
                .collect(Collectors.toList()); // keep tests that have a difference with
                                               // the first element greater than the std dev
        reducedTests.add(tests.get(0)); // add the first element, which the "reference"
        return reducedTests;
    }

    private static long convert(byte[] byteArray) {
        long sum = 0L;
        for (byte aByteArray : byteArray) {
            sum += (int) aByteArray;
        }
        return sum;
    }

    private static double standardDeviation(List<Long> hashCodes) {
        final double mean = hashCodes.stream()
                .mapToLong(value -> value)
                .average()
                .orElse(0.0D);
        return Math.sqrt(hashCodes.stream()
                .mapToDouble(value -> value)
                .map(value -> Math.pow(Math.abs((value - mean)), 2.0D))
                .sum()
                / hashCodes.size());
    }

}
