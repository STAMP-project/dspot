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
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

        final JunitResult result = runTests(clone, tests);
        if (result == null) {
            return Collections.emptyList();
        } else {

            final Set<String> failuresMethodName = result.getFailures();
            final List<String> passingMethodName = result.getPassingTests();

            // add assertion on passing tests
            List<CtMethod<?>> passingTests = addAssertions(clone,
                    tests.stream()
                            .filter(ctMethod -> passingMethodName.contains(ctMethod.getSimpleName()))
                            .collect(Collectors.toList()),
                    statementsIndexToAssert)
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // add try/catch/fail on failing/error tests
            List<CtMethod<?>> failingTests = tests.stream()
                    .filter(ctMethod ->
                            failuresMethodName.contains(ctMethod.getSimpleName()))
                    .map(ctMethod ->
                            makeFailureTest(ctMethod, result.getFailureOf(ctMethod.getSimpleName()))
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (passingTests != null) {
                passingTests.addAll(failingTests);
            } else {
                passingTests = failingTests;
            }
            return passingTests.isEmpty() ? passingTests : filterTest(clone, passingTests, 3);
        }
    }


    private List<CtMethod<?>> addAssertions(CtType<?> testClass, List<CtMethod<?>> testCases, Map<CtMethod<?>, List<Integer>> statementsIndexToAssert) throws IOException, ClassNotFoundException {
        CtType clone = testClass.clone();
        testClass.getPackage().addType(clone);
        final List<CtMethod<?>> testCasesWithLogs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int finalI = i;
            final List<? extends CtMethod<?>> testsWithLog = testCases.stream()
                    .map(ctMethod -> AssertGeneratorHelper.createTestWithLog(ctMethod, statementsIndexToAssert.get(ctMethod), this.originalClass.getSimpleName()))
                    .map(ctMethod -> {
                        ctMethod.setSimpleName(ctMethod.getSimpleName() + finalI);
                        return ctMethod;
                    })
                    .collect(Collectors.toList());
            testsWithLog.forEach(clone::addMethod);
            testCasesWithLogs.addAll(testsWithLog);
        }
        ObjectLog.reset();
        final JunitResult result = runTests(clone, testCasesWithLogs);
        if (result == null || !result.getFailures().isEmpty()) {
            return Collections.emptyList();
        } else {
            Map<String, Observation> observations = AmplificationChecker.isMocked(testClass) ?
                    ObjectLog.getObservations() : ObjectLog.getObservations();
            return testCases.stream()
                    .map(ctMethod -> this.buildTestWithAssert(ctMethod, observations))
                    .collect(Collectors.toList());
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

    private JunitResult runTests(CtType testClass, List<CtMethod<?>> testsToRun) {
        final InputProgram inputProgram = this.configuration.getInputProgram();
        final String dependencies = inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir() + ":" +
                inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir();
        final List<CtMethod<?>> uncompilableMethods = TestCompiler.compile(this.compiler, testClass, true, dependencies);
        if (uncompilableMethods.contains(TestCompiler.METHOD_CODE_RETURN)) {
            return null;
        } else {
            uncompilableMethods.forEach(testsToRun::remove);
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
            final JunitResult result = runTests(clone, clones);
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
