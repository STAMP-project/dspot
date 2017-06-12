package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.compare.ObjectLog;
import fr.inria.diversify.compare.Observation;
import fr.inria.diversify.dspot.AmplificationChecker;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.dspot.support.Counter;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.TestCompiler;
import fr.inria.diversify.testRunner.TestRunner;
import org.junit.runner.notification.Failure;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fr.inria.diversify.compare.ObjectLog.FILENAME_OF_OBSERVATIONS;
import static fr.inria.diversify.dspot.assertGenerator.AssertGeneratorHelper.takeAllStatementToAssert;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/3/17
 */
public class MethodsAssertGenerator {

    private int numberOfFail = 0;

    private CtType originalClass;

    private Factory factory;

    private InputConfiguration configuration;

    private DSpotCompiler compiler;

    public MethodsAssertGenerator(CtType originalClass, InputConfiguration configuration, DSpotCompiler compiler) {
        this.originalClass = originalClass;
        this.configuration = configuration;
        this.compiler = compiler;
        this.factory = configuration.getInputProgram().getFactory();
    }

    public List<CtMethod<?>> generateAsserts(CtType testClass, List<CtMethod<?>> tests) throws IOException, ClassNotFoundException {
        final Map<CtMethod<?>, List<Integer>> statementsIndexToAssert = takeAllStatementToAssert(testClass, tests);
        final List<CtMethod<?>> testWithoutAssert = tests.stream()
                .map(test -> AssertGeneratorHelper.createTestWithoutAssert(test, statementsIndexToAssert.get(test)))
                .collect(Collectors.toList());
        return generateAsserts(testClass,
                testWithoutAssert,
                takeAllStatementToAssert(testClass, testWithoutAssert));
    }

    public List<CtMethod<?>> generateAsserts(CtType testClass, List<CtMethod<?>> tests, Map<CtMethod<?>, List<Integer>> statementsIndexToAssert) throws IOException, ClassNotFoundException {
        CtType clone = testClass.clone();
        testClass.getPackage().addType(clone);
        tests.forEach(clone::addMethod);

        final JunitResult result = runTests(clone, tests, false);
        if (result == null) {
            return Collections.emptyList();
        } else {

            final Set<String> failuresMethodName = result.getFailures();
            final List<String> passingMethodName = result.getPassingTests();

            final List<CtMethod<?>> generatedTestWithAssertion = new ArrayList<>();
            // add assertion on passing tests
            if (!passingMethodName.isEmpty()) {
                List<CtMethod<?>> passingTests = addAssertions(clone,
                        tests.stream()
                                .filter(ctMethod -> passingMethodName.contains(ctMethod.getSimpleName()))
                                .collect(Collectors.toList()),
                        statementsIndexToAssert)
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (passingTests != null) {
                    generatedTestWithAssertion.addAll(passingTests);
                }
            }

            // add try/catch/fail on failing/error tests
            if (!failuresMethodName.isEmpty()) {
                final List<CtMethod<?>> failingTests = tests.stream()
                        .filter(ctMethod ->
                                failuresMethodName.contains(ctMethod.getSimpleName()))
                        .map(ctMethod ->
                                makeFailureTest(ctMethod, result.getFailureOf(ctMethod.getSimpleName()))
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!failingTests.isEmpty()) {
                    generatedTestWithAssertion.addAll(failingTests);
                }
            }
            return generatedTestWithAssertion.isEmpty() ?
                    generatedTestWithAssertion :
                    filterTest(clone, generatedTestWithAssertion, 3);
        }
    }


    private List<CtMethod<?>> addAssertions(CtType<?> testClass, List<CtMethod<?>> testCases, Map<CtMethod<?>, List<Integer>> statementsIndexToAssert) throws IOException, ClassNotFoundException {
        CtType clone = testClass.clone();
        testClass.getPackage().addType(clone);
        final List<CtMethod<?>> testCasesWithLogs = testCases.stream()
                .map(ctMethod ->
                        AssertGeneratorHelper.createTestWithLog(ctMethod,
                                statementsIndexToAssert.get(ctMethod),
                                this.originalClass.getSimpleName()
                        )
                ).collect(Collectors.toList());
        final List<CtMethod<?>> testToRuns = new ArrayList<>();
        IntStream.range(0, 3).forEach(i ->
                testToRuns.addAll(
                        testCasesWithLogs.stream()
                                .map(CtMethod::clone)
                                .map(ctMethod -> {
                                    ctMethod.setSimpleName(ctMethod.getSimpleName() + i);
                                    return ctMethod;
                                })
                                .map(ctMethod -> {
                                    clone.addMethod(ctMethod);
                                    return ctMethod;
                                })
                                .collect(Collectors.toList())
                )
        );
        ObjectLog.reset();
        final JunitResult result = runTests(clone, testToRuns, true);
        if (result == null || !result.getFailures().isEmpty()) {
            return Collections.emptyList();
        } else {
            Map<String, Observation> observations = AmplificationChecker.isMocked(testClass) ?
                    readObservations(clone) : ObjectLog.getObservations();
            return testCases.stream()
                    .map(ctMethod -> this.buildTestWithAssert(ctMethod, observations))
                    .collect(Collectors.toList());
        }
    }

    private Map<String, Observation> readObservations(CtType<?> classTest) {
        try (FileInputStream fin = new FileInputStream(this.configuration.getInputProgram().getProgramDir() + "/" + FILENAME_OF_OBSERVATIONS)) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            return (Map<String, Observation>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private CtMethod<?> buildTestWithAssert(CtMethod test, Map<String, Observation> observations) {
        CtMethod testWithAssert = test.clone();
        int numberOfAddedAssertion = 0;
        List<CtStatement> statements = Query.getElements(testWithAssert, new TypeFilter(CtStatement.class));
        for (String id : observations.keySet()) {
            if (!id.startsWith(testWithAssert.getSimpleName())) {
                continue;
            }
            int line = Integer.parseInt(id.split("__")[1]);
            final List<CtStatement> assertStatements = AssertBuilder.buildAssert(factory,
                    observations.get(id).getNotDeterministValues(),
                    observations.get(id).getObservationValues());
            for (CtStatement statement : assertStatements) {
                DSpotUtils.addComment(statement, "AssertGenerator add assertion", CtComment.CommentType.INLINE);
                try {
                    CtStatement stmt = statements.get(line);
                    if (stmt instanceof CtInvocation && !AssertGeneratorHelper.isVoidReturn((CtInvocation) stmt)) {
                        String localVarSnippet = ((CtInvocation) stmt).getType().toString()
                                + " o_" + id + " = "
                                + stmt.toString();
                        CtStatement localVarStmt = factory.Code().createCodeSnippetStatement(localVarSnippet);
                        stmt.replace(localVarStmt);
                        statements.set(line, localVarStmt);
                        DSpotUtils.addComment(localVarStmt, "AssertGenerator replace invocation", CtComment.CommentType.INLINE);
                        localVarStmt.setParent(stmt.getParent());
                        localVarStmt.insertAfter(statement);
                    } else {
                        stmt.insertAfter(statement);
                    }
                    numberOfAddedAssertion++;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Counter.updateAssertionOf(testWithAssert, numberOfAddedAssertion);
        if (!testWithAssert.equals(test)) {
            AmplificationHelper.getAmpTestToParent().put(testWithAssert, test);
            return testWithAssert;
        } else {
            return null;
        }
    }

    protected CtMethod<?> makeFailureTest(CtMethod<?> test, Failure failure) {
        CtMethod testWithoutAssert = AssertGeneratorHelper.createTestWithoutAssert(test, new ArrayList<>());
        testWithoutAssert.setSimpleName(test.getSimpleName());
        Factory factory = testWithoutAssert.getFactory();

        Throwable exception = failure.getException();
        if (exception instanceof AssertionError) {
            exception = exception.getCause();
        }
        Class exceptionClass;
        if (exception == null) {
            exceptionClass = Exception.class;
        } else {
            exceptionClass = exception.getClass();
        }

        CtTry tryBlock = factory.Core().createTry();
        tryBlock.setBody(testWithoutAssert.getBody());
        String snippet = "org.junit.Assert.fail(\"" + test.getSimpleName() + " should have thrown " + exceptionClass.getSimpleName() + "\")";
        tryBlock.getBody().addStatement(factory.Code().createCodeSnippetStatement(snippet));
        DSpotUtils.addComment(tryBlock, "AssertGenerator generate try/catch block with fail statement", CtComment.CommentType.INLINE);

        CtCatch ctCatch = factory.Core().createCatch();
        CtTypeReference exceptionType = factory.Type().createReference(exceptionClass);
        ctCatch.setParameter(factory.Code().createCatchVariable(exceptionType, "eee"));

        ctCatch.setBody(factory.Core().createBlock());

        List<CtCatch> catchers = new ArrayList<>(1);
        catchers.add(ctCatch);
        tryBlock.setCatchers(catchers);

        CtBlock body = factory.Core().createBlock();
        body.addStatement(tryBlock);

        testWithoutAssert.setBody(body);
        testWithoutAssert.setSimpleName(testWithoutAssert.getSimpleName() + "_failAssert" + (numberOfFail++));
        Counter.updateAssertionOf(testWithoutAssert, 1);

        AmplificationHelper.getAmpTestToParent().put(testWithoutAssert, test);

        return testWithoutAssert;
    }

    private JunitResult runTests(CtType testClass, List<CtMethod<?>> testsToRun, boolean withLog) {
        final InputProgram inputProgram = this.configuration.getInputProgram();
        final String dependencies = inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir() + ":" +
                inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir();
        final List<CtMethod<?>> uncompilableMethods = TestCompiler.compile(this.compiler, testClass,
                withLog, dependencies);
        if (uncompilableMethods.contains(TestCompiler.METHOD_CODE_RETURN)) {
            return null;
        } else {
            testsToRun.removeAll(uncompilableMethods);
            uncompilableMethods.forEach(testClass::removeMethod);
            String classpath = AmplificationHelper.getClassPath(this.compiler, inputProgram);
            return TestRunner.runTests(testClass, testsToRun, classpath, this.configuration);
        }
    }

    private List<CtMethod<?>> filterTest(CtType clone, List<CtMethod<?>> tests, int nTime) {
        final List<CtMethod<?>> clones = tests.stream()
                .map(CtMethod::clone)
                .collect(Collectors.toList());
        clones.forEach(clone::addMethod);

        for (int i = 0; i < nTime; i++) {
            if (clones.isEmpty()) {
                return Collections.emptyList();
            }
            final JunitResult result = runTests(clone, clones, false);
            if (result == null) {
                return Collections.emptyList();
            } else {
                final ArrayList<String> failingTestHeaders = result.getFailures().stream()
                        .collect(ArrayList<String>::new,
                                (testHeaders, failure) -> testHeaders.add(result.getFailureOf(failure).getTestHeader().split("\\(")[0]),
                                ArrayList<String>::addAll);
                failingTestHeaders.forEach(failingTestHeader -> {
                            final Optional<CtMethod<?>> optionalFailingTestCase = clones.stream()
                                    .filter(ctMethod ->
                                            ctMethod.getSimpleName().equals(failingTestHeader))
                                    .findFirst();
                            if (optionalFailingTestCase.isPresent()) {
                                final CtMethod<?> failingTestCase = optionalFailingTestCase.get();
                                clones.remove(failingTestCase);
                                clone.removeMethod(failingTestCase);
                            }
                        }
                );
            }
        }
        return clones;
    }
}
