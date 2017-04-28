package fr.inria.diversify.dspot;

import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.assertGenerator.AssertGenerator;
import fr.inria.diversify.dspot.selector.TestSelector;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.TestCompiler;
import fr.inria.diversify.testRunner.TestRunner;
import fr.inria.diversify.util.Log;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 03/12/15
 * Time: 13:52
 */
public class Amplification {

    private InputConfiguration configuration;
    private List<Amplifier> amplifiers;
    private TestSelector testSelector;
    private AssertGenerator assertGenerator;
    private DSpotCompiler compiler;

    private static int ampTestCount;

    public Amplification(InputConfiguration configuration, List<Amplifier> amplifiers, TestSelector testSelector, DSpotCompiler compiler) {
        this.configuration = configuration;
        this.amplifiers = amplifiers;
        this.testSelector = testSelector;
        this.compiler = compiler;
        this.assertGenerator = new AssertGenerator(this.configuration, this.compiler);
    }

    public CtType amplification(CtType<?> classTest, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        return amplification(classTest, AmplificationHelper.getAllTest(this.configuration.getInputProgram(), classTest), maxIteration);
    }

    public static CtType<?> cc;

    public CtType amplification(CtType<?> classTest, List<CtMethod<?>> methods, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        List<CtMethod<?>> tests = methods.stream()
                .filter(mth -> AmplificationChecker.isTest(mth, this.configuration.getInputProgram().getRelativeTestSourceCodeDir()))
                .collect(Collectors.toList());
        if (tests.isEmpty()) {
            return null;
        }

        cc = classTest;

        Log.info("amplification of {} ({} test)", classTest.getQualifiedName(), tests.size());
        testSelector.reset();
        List<CtMethod<?>> ampTest = new ArrayList<>();

        updateAmplifiedTestList(ampTest, preAmplification(classTest, tests));

        for (int i = 0; i < tests.size(); i++) {
            CtMethod test = tests.get(i);
            if (!classTest.toString().equals(body)) {
                throw new RuntimeException();
            }
            Log.debug("amp {} ({}/{})", test.getSimpleName(), i + 1, tests.size());
            testSelector.reset();
            JunitResult result = compileAndRunTests(classTest, Collections.singletonList(tests.get(i)));
            if (result != null) {
                if (result.getFailures().isEmpty()
                        && !result.getTestsRun().isEmpty()) {
                    updateAmplifiedTestList(ampTest,
                            amplification(classTest, test, maxIteration));
                } else {
                    Log.debug("{} / {} test cases failed!", result.getFailures().size(), result.getTestsRun().size());
                }
            }
        }
        return AmplificationHelper.createAmplifiedTest(ampTest, classTest);
    }

    private List<CtMethod<?>> amplification(CtType<?> classTest, CtMethod test, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        List<CtMethod<?>> currentTestList = new ArrayList<>();
        currentTestList.add(test);

        List<CtMethod<?>> amplifiedTests = new ArrayList<>();

        for (int i = 0; i < maxIteration; i++) {
            Log.debug("iteration {}:", i);
            List<CtMethod<?>> testToBeAmplified = testSelector.selectToAmplify(currentTestList);
            if (testToBeAmplified.isEmpty()) {
                Log.debug("No test could be generated from selected test");
                continue;
            }
            Log.debug("{} tests selected to be amplified over {} available tests", testToBeAmplified.size(), currentTestList.size());

            currentTestList = AmplificationHelper.reduce(amplifyTests(testToBeAmplified));
            List<CtMethod<?>> testWithAssertions = assertGenerator.generateAsserts(classTest, currentTestList);
            if (testWithAssertions.isEmpty()) {
                continue;
            } else {
                currentTestList = testWithAssertions;
            }
            JunitResult result = compileAndRunTests(classTest, currentTestList);
            if (result == null) {
                continue;
            } else if (!result.getFailures().isEmpty()) {
                Log.warn("Discarding failing test cases");
                final Set<String> failingTestCase = result.getFailures();
                currentTestList = currentTestList.stream()
                        .filter(ctMethod -> !failingTestCase.contains(ctMethod.getSimpleName()))
                        .collect(Collectors.toList());
            }
            currentTestList = AmplificationHelper.filterTest(currentTestList, result);
            Log.debug("{} test method(s) has been successfully generated", currentTestList.size());
            amplifiedTests.addAll(testSelector.selectToKeep(currentTestList));
        }
        return amplifiedTests;
    }

    private void updateAmplifiedTestList(List<CtMethod<?>> ampTest, List<CtMethod<?>> amplification) {
        ampTest.addAll(amplification);
        ampTestCount += amplification.size();
        Log.debug("total amp test: {}, global: {}", amplification.size(), ampTestCount);
    }

    private List<CtMethod<?>> preAmplification(CtType classTest, List<CtMethod<?>> methods) throws IOException, ClassNotFoundException {
        List<CtMethod<?>> tests = methods.stream()
                .filter(ctMethod -> AmplificationChecker.isTest(ctMethod, configuration.getInputProgram().getRelativeTestSourceCodeDir()))
                .collect(Collectors.toList());
        if (tests.isEmpty()) {
            return Collections.emptyList();
        }
        JunitResult result = compileAndRunTests(classTest, tests);
        if (result == null) {
            Log.warn("Need a green test suite to run dspot");
            return Collections.emptyList();
        }
        if (!result.getFailures().isEmpty()) {
            Log.warn("{} tests failed before the amplifications", result.getFailures().size());
            Log.warn("{}", result.getFailures().stream().reduce("",
                    (s, failure) -> s + failure + ":" + System.getProperty("line.separator"),
                    String::concat)
            );
            Log.warn("Discarding following test cases for the amplification");

            result.getFailures().forEach(failure -> {
                try {
                    CtMethod testToRemove = tests.stream()
                            .filter(m -> failure.equals(m.getSimpleName()))
                            .findFirst().get();
                    tests.remove(tests.indexOf(testToRemove));
                    Log.warn("{}", testToRemove.getSimpleName());
                } catch (Exception ignored) {
                    //ignored
                }
            });
            return preAmplification(classTest, tests);
        } else {
            testSelector.update();
            resetAmplifiers(classTest);
            Log.debug("Try to add assertions before amplification");
            List<CtMethod<?>> preAmplifiedMethods = testSelector.selectToKeep(
                    assertGenerator.generateAsserts(
                            classTest, testSelector.selectToAmplify(tests))
            );
            if (tests.containsAll(preAmplifiedMethods)) {
                return Collections.emptyList();
            } else {
                return preAmplifiedMethods;
            }
        }
    }

    public static final String  body = "public class BookDALTest {\n" +
            "    private static info.sanaulla.dal.BookDAL mockedBookDAL;\n" +
            "\n" +
            "    private static info.sanaulla.models.Book book1;\n" +
            "\n" +
            "    private static info.sanaulla.models.Book book2;\n" +
            "\n" +
            "    @org.junit.BeforeClass\n" +
            "    public static void setUp() {\n" +
            "        info.sanaulla.dal.BookDALTest.mockedBookDAL = org.mockito.Mockito.mock(info.sanaulla.dal.BookDAL.class);\n" +
            "        info.sanaulla.dal.BookDALTest.book1 = new info.sanaulla.models.Book(\"8131721019\", \"Compilers Principles\", java.util.Arrays.asList(\"D. Jeffrey Ulman\", \"Ravi Sethi\", \"Alfred V. Aho\", \"Monica S. Lam\"), \"Pearson Education Singapore Pte Ltd\", 2008, 1009, \"BOOK_IMAGE\");\n" +
            "        info.sanaulla.dal.BookDALTest.book2 = new info.sanaulla.models.Book(\"9788183331630\", \"Let Us C 13th Edition\", java.util.Arrays.asList(\"Yashavant Kanetkar\"), \"BPB PUBLICATIONS\", 2012, 675, \"BOOK_IMAGE\");\n" +
            "        org.mockito.Mockito.when(info.sanaulla.dal.BookDALTest.mockedBookDAL.getAllBooks()).thenReturn(java.util.Arrays.asList(info.sanaulla.dal.BookDALTest.book1, info.sanaulla.dal.BookDALTest.book2));\n" +
            "        org.mockito.Mockito.when(info.sanaulla.dal.BookDALTest.mockedBookDAL.getBook(\"8131721019\")).thenReturn(info.sanaulla.dal.BookDALTest.book1);\n" +
            "        org.mockito.Mockito.when(info.sanaulla.dal.BookDALTest.mockedBookDAL.addBook(info.sanaulla.dal.BookDALTest.book1)).thenReturn(info.sanaulla.dal.BookDALTest.book1.getIsbn());\n" +
            "        org.mockito.Mockito.when(info.sanaulla.dal.BookDALTest.mockedBookDAL.updateBook(info.sanaulla.dal.BookDALTest.book1)).thenReturn(info.sanaulla.dal.BookDALTest.book1.getIsbn());\n" +
            "    }\n" +
            "\n" +
            "    @org.junit.Test\n" +
            "    public void testGetAllBooks() throws java.lang.Exception {\n" +
            "        java.util.List<info.sanaulla.models.Book> allBooks = info.sanaulla.dal.BookDALTest.mockedBookDAL.getAllBooks();\n" +
            "        org.junit.Assert.assertEquals(2, allBooks.size());\n" +
            "        info.sanaulla.models.Book myBook = allBooks.get(0);\n" +
            "        org.junit.Assert.assertEquals(\"8131721019\", myBook.getIsbn());\n" +
            "        org.junit.Assert.assertEquals(\"Compilers Principles\", myBook.getTitle());\n" +
            "        org.junit.Assert.assertEquals(4, myBook.getAuthors().size());\n" +
            "        org.junit.Assert.assertEquals(((java.lang.Integer) (2008)), myBook.getYearOfPublication());\n" +
            "        org.junit.Assert.assertEquals(((java.lang.Integer) (1009)), myBook.getNumberOfPages());\n" +
            "        org.junit.Assert.assertEquals(\"Pearson Education Singapore Pte Ltd\", myBook.getPublication());\n" +
            "        org.junit.Assert.assertEquals(\"BOOK_IMAGE\", myBook.getImage());\n" +
            "    }\n" +
            "\n" +
            "    @org.junit.Test\n" +
            "    public void testGetBook() {\n" +
            "        java.lang.String isbn = \"8131721019\";\n" +
            "        info.sanaulla.models.Book myBook = info.sanaulla.dal.BookDALTest.mockedBookDAL.getBook(isbn);\n" +
            "        org.junit.Assert.assertNotNull(myBook);\n" +
            "        org.junit.Assert.assertEquals(isbn, myBook.getIsbn());\n" +
            "        org.junit.Assert.assertEquals(\"Compilers Principles\", myBook.getTitle());\n" +
            "        org.junit.Assert.assertEquals(4, myBook.getAuthors().size());\n" +
            "        org.junit.Assert.assertEquals(\"Pearson Education Singapore Pte Ltd\", myBook.getPublication());\n" +
            "        org.junit.Assert.assertEquals(((java.lang.Integer) (2008)), myBook.getYearOfPublication());\n" +
            "        org.junit.Assert.assertEquals(((java.lang.Integer) (1009)), myBook.getNumberOfPages());\n" +
            "    }\n" +
            "\n" +
            "    @org.junit.Test\n" +
            "    public void testAddBook() {\n" +
            "        java.lang.String isbn = info.sanaulla.dal.BookDALTest.mockedBookDAL.addBook(info.sanaulla.dal.BookDALTest.book1);\n" +
            "        org.junit.Assert.assertNotNull(isbn);\n" +
            "        org.junit.Assert.assertEquals(info.sanaulla.dal.BookDALTest.book1.getIsbn(), isbn);\n" +
            "    }\n" +
            "\n" +
            "    @org.junit.Test\n" +
            "    public void testUpdateBook() {\n" +
            "        java.lang.String isbn = info.sanaulla.dal.BookDALTest.mockedBookDAL.updateBook(info.sanaulla.dal.BookDALTest.book1);\n" +
            "        org.junit.Assert.assertNotNull(isbn);\n" +
            "        org.junit.Assert.assertEquals(info.sanaulla.dal.BookDALTest.book1.getIsbn(), isbn);\n" +
            "    }\n" +
            "}";

    private List<CtMethod<?>> amplifyTests(List<CtMethod<?>> tests) {
        List<CtMethod<?>> amplifiedTests = tests.stream()
                .flatMap(test -> amplifyTest(test).stream())
                .filter(test -> test != null && !test.getBody().getStatements().isEmpty())
                .collect(Collectors.toList());
        Log.debug("{} new tests generated", amplifiedTests.size());
        return amplifiedTests;
    }

    private List<CtMethod<?>> amplifyTest(CtMethod test) {
        return amplifiers.stream()
                .flatMap(amplifier -> amplifier.apply(test).stream())
                .map(amplifiedTest ->
                        AmplificationHelper.addOriginInComment(amplifiedTest, AmplificationHelper.getTopParent(test))
                ).collect(Collectors.toList());
    }

    private void resetAmplifiers(CtType parentClass) {
        amplifiers.forEach(amp -> amp.reset(parentClass));
    }

    private JunitResult compileAndRunTests(CtType classTest, List<CtMethod<?>> currentTestList) {
        CtType amplifiedTestClass = this.testSelector.buildClassForSelection(classTest, currentTestList);
        boolean status = TestCompiler.writeAndCompile(this.compiler, amplifiedTestClass, false,
                this.configuration.getInputProgram().getProgramDir() + this.configuration.getInputProgram().getClassesDir() + "/:" +
                        this.configuration.getInputProgram().getProgramDir() + this.configuration.getInputProgram().getTestClassesDir() + "/");
        if (!status) {
            Log.debug("Unable to compile {}", amplifiedTestClass.getSimpleName());
            return null;
        }
        JunitResult result;
        try {
            String classpath = AmplificationHelper.getClassPath(this.compiler, this.configuration.getInputProgram());
            result = TestRunner.runTests(amplifiedTestClass, currentTestList, classpath, configuration);
        } catch (Exception ignored) {
            Log.debug("Error during running test");
            return null;
        }
        if (result == null || result.getTestsRun().size() < currentTestList.size()) {
            Log.debug("Error during running test");
            return null;
        }
        Log.debug("update test testCriterion");
        testSelector.update();
        return result;
    }
}