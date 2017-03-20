package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.compare.ObjectLog;
import fr.inria.diversify.compare.Observation;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.dspot.selector.json.TestCaseJSON;
import fr.inria.diversify.dspot.support.Counter;
import fr.inria.diversify.dspot.support.DSpotCompiler;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.inria.diversify.dspot.assertGenerator.AssertGeneratorHelper.addLogStmt;

/**
 * User: Simon
 * Date: 22/10/15
 * Time: 10:06
 */
@Deprecated
public class MethodAssertGenerator {

    private int numberOfFail = 0;
    CtMethod test;
    private CtType originalClass;
    private InputProgram inputProgram;
    private List<Integer> statementsIndexToAssert;
    private DSpotCompiler compiler;

    public MethodAssertGenerator(CtType originalClass, InputProgram inputProgram, DSpotCompiler compiler) throws IOException {
        this.originalClass = originalClass;
        this.inputProgram = inputProgram;
        this.statementsIndexToAssert = new ArrayList<>();
        this.compiler = compiler;
    }

    public CtMethod generateAssert(CtMethod test) throws IOException, ClassNotFoundException {
        this.test = test;
        this.test.setParent(test.getParent());
        for (int i = 0; i < Query.getElements(this.test, new TypeFilter(CtStatement.class)).size(); i++) {
            statementsIndexToAssert.add(i);
        }
        this.test = createTestWithoutAssert(test, statementsIndexToAssert);
        CtMethod newTest = generateAssert();
        if (newTest == null || !isCorrect(newTest)) {
            return null;
        }
        return newTest;
    }

    public CtMethod generateAssert(CtMethod test, List<Integer> statementsIndexToAssert) throws IOException, ClassNotFoundException {
        this.test = test;
        this.statementsIndexToAssert = statementsIndexToAssert;
        CtMethod newTest = generateAssert();
        if (newTest == null || !isCorrect(newTest)) {
            return null;
        }
        return newTest;
    }

    private CtMethod generateAssert() throws IOException, ClassNotFoundException {
        List<CtMethod<?>> testsToRun = new ArrayList<>();
        CtType classTest = initTestClass();

        CtMethod cloneTest = test.clone();
        classTest.addMethod(cloneTest);
        testsToRun.add(cloneTest);

        JunitResult result = runTests(classTest, testsToRun);
        if (result == null || result.getTestRuns().size() != testsToRun.size()) {
            return null;
        }
        try {
            if (!testFailed(cloneTest.getSimpleName(), result)) {
                if (!statementsIndexToAssert.isEmpty()) {
                    return buildNewAssert();
                }
            } else {
                return makeFailureTest(getFailure(cloneTest.getSimpleName(), result));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    protected CtMethod makeFailureTest(Failure failure) {
        CtMethod testWithoutAssert = createTestWithoutAssert(test, new ArrayList<>());
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

        return testWithoutAssert;
    }

    private CtMethod buildNewAssert() throws IOException, ClassNotFoundException {
        CtType cl = initTestClass();
        List<CtMethod<?>> testsToRun = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CtMethod testWithLog = createTestWithLog();
            testWithLog.setSimpleName(testWithLog.getSimpleName() + i);
            cl.addMethod(testWithLog);
            testsToRun.add(testWithLog);
        }
        ObjectLog.reset();
        JunitResult result = runTests(cl, testsToRun);
        if (result == null || !result.getFailures().isEmpty()) {
            return null;
        }
        return buildTestWithAssert(ObjectLog.getObservations());
    }

    private CtMethod buildTestWithAssert(Map<String, Observation> observations) {
        CtMethod testWithAssert = test.clone();
        final Factory factory = test.getFactory();
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
        return testWithAssert;
    }

    private boolean isCorrect(CtMethod test) throws IOException, ClassNotFoundException {
        JunitResult result = runVersusRandomness(test);
        return result != null && result.getFailures().isEmpty();
    }

    @Deprecated
    protected CtMethod removeFailAssert() throws IOException, ClassNotFoundException {
        List<Integer> goodAssert = findGoodAssert();
        String testName = test.getSimpleName();
        test = createTestWithoutAssert(test, goodAssert);
        test.setSimpleName(testName);

        return test;
    }

    @Deprecated
    private List<Integer> findGoodAssert() throws IOException, ClassNotFoundException {
        int stmtIndex = 0;
        List<CtMethod<?>> testsToRun = new ArrayList<>();
        List<Integer> assertIndex = new ArrayList<>();
        List<CtStatement> statements = Query.getElements(test, new TypeFilter(CtStatement.class));
        for (CtStatement statement : statements) {
            if (isAssert(statement)) {
                assertIndex.add(stmtIndex);
            }
            stmtIndex++;
        }

        CtType newClass = originalClass.clone();
        newClass.setParent(originalClass.getParent());
        for (int i = 0; i < assertIndex.size(); i++) {
            List<Integer> assertToKeep = new ArrayList<>();
            assertToKeep.add(assertIndex.get(i));
            CtMethod mth = createTestWithoutAssert(test, assertToKeep);
            mth.setSimpleName(mth.getSimpleName() + "_" + i);
            newClass.addMethod(mth);
            testsToRun.add(mth);
        }
        ObjectLog.reset();
        JunitResult result = runTests(newClass, testsToRun);

        List<Integer> goodAssertIndex = new ArrayList<>();
        for (int i = 0; i < testsToRun.size(); i++) {
            if (!testFailed(testsToRun.get(i).getSimpleName(), result)) {
                goodAssertIndex.add(assertIndex.get(i));
            }
        }
        return goodAssertIndex;
    }

    protected Failure getFailure(String methodName, JunitResult result) {
        return result.getFailures().stream()
                .filter(failure -> methodName.equals(failure.getDescription().getMethodName()))
                .findAny()
                .orElse(null);
    }

    private boolean testFailed(String methodName, JunitResult result) {
        return getFailure(methodName, result) != null;
    }

    public JunitResult runTests(CtType testClass, List<CtMethod<?>> testsToRun) throws ClassNotFoundException {
        final String dependencies = inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir() + ":" +
                inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir();
        boolean statusCompilation = TestCompiler.writeAndCompile(this.compiler, testClass,
                true, dependencies);
        if (!statusCompilation) {
            return null;
        } else {
            String classpath = AmplificationHelper.getClassPath(this.compiler, this.inputProgram);
            return TestRunner.runTests(testClass, testsToRun, classpath, this.inputProgram);
        }
    }

    protected JunitResult runVersusRandomness(CtMethod test) throws ClassNotFoundException, IOException {
        CtType cloneClass = initTestClass();
        CtMethod cloneTest = test.clone();
        cloneClass.addMethod(cloneTest);
        for (int i = 0; i < 2; i++) {
            JunitResult result = runTests(cloneClass, Collections.singletonList(cloneTest));
            if (result == null || !result.getFailures().isEmpty()) {
                return result;
            }
        }
        return runTests(cloneClass, Collections.singletonList(cloneTest));
    }

    private CtType initTestClass() {
        CtType clone = originalClass.clone();
        this.originalClass.getPackage().addType(clone);
        return clone;
    }

    private CtMethod createTestWithLog() {
        CtMethod newTest = test.clone();
        newTest.setSimpleName(test.getSimpleName() + "_withlog");
        List<CtStatement> stmts = Query.getElements(newTest, new TypeFilter(CtStatement.class));
        for (int i = 0; i < stmts.size(); i++) {
            CtStatement stmt = stmts.get(i);
            if (isStmtToLog(stmt)) {
                addLogStmt(stmt, test.getSimpleName() + "__" + i, statementsIndexToAssert.contains(i));
            }
        }
        return newTest;
    }

    private boolean isStmtToLog(CtStatement statement) {
        if (!(statement.getParent() instanceof CtBlock)) {
            return false;
        }
        if (statement instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) statement;

            //type tested by the test class
            String targetType = "";
            if (invocation.getTarget() != null &&
                    invocation.getTarget().getType() != null) {
                targetType = invocation.getTarget().getType().getSimpleName();
            }
            return originalClass.getSimpleName().startsWith(targetType)
                    || !isVoidReturn(invocation);
        }
        return statement instanceof CtVariableWrite
                || statement instanceof CtAssignment
                || statement instanceof CtLocalVariable;
    }

    private boolean isVoidReturn(CtInvocation invocation) {
        return invocation.getType() != null && (invocation.getType().getSimpleName().equals("Void") || invocation.getType().getSimpleName().equals("void"));
    }

    protected CtMethod createTestWithoutAssert(CtMethod test, List<Integer> assertIndexToKeep) {
        CtMethod newTest = test.clone();
        newTest.setSimpleName(test.getSimpleName() + "_withoutAssert");

        int stmtIndex = 0;
        List<CtStatement> statements = Query.getElements(newTest, new TypeFilter(CtStatement.class));
        for (CtStatement statement : statements) {
            try {
                if (!assertIndexToKeep.contains(stmtIndex) && isAssert(statement)) {
                    CtBlock block = buildRemoveAssertBlock((CtInvocation) statement, stmtIndex);
                    if (statement.getParent() instanceof CtCase) {
                        CtCase ctCase = (CtCase) statement.getParent();
                        int index = ctCase.getStatements().indexOf(statement);
                        ctCase.getStatements().add(index, block);
                        ctCase.getStatements().remove(statement);
                    } else {
                        if (block.getStatements().size() == 0) {
                            statement.delete();
                        } else if (block.getStatements().size() == 1) {
                            statement.replace(block.getStatement(0));
                        } else {
                            replaceStatementByListOfStatements(statement, block.getStatements());
                        }
                    }
                }
                stmtIndex++;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return newTest;
    }

    private void replaceStatementByListOfStatements(CtStatement statement, List<CtStatement> statements) {
        String oldStatement = statement.toString();
        statement.replace(statements.get(0));
        for (int i = 1; i < statements.size(); i++) {
            statement.insertAfter(statements.get(i));
        }
        DSpotUtils.addComment(statement, "MethodAssertion Generator replaced " + oldStatement, CtComment.CommentType.BLOCK);
    }

    private CtBlock buildRemoveAssertBlock(CtInvocation assertInvocation, int blockId) {
        CtBlock block = getFactory().Core().createBlock();

        int[] idx = {0};
        getNotLiteralArgs(assertInvocation).stream()
                .filter(arg -> !(arg instanceof CtVariableAccess))
                .map(arg -> buildVarStatement(arg, blockId + "_" + (idx[0]++)))
                .forEach(stmt -> block.addStatement(stmt));

        block.setParent(assertInvocation.getParent());
        return block;
    }

    private List<CtExpression> getNotLiteralArgs(CtInvocation invocation) {
        List<CtExpression> args = invocation.getArguments();
        return args.stream()
                .filter(arg -> !(arg instanceof CtLiteral))
                .collect(Collectors.toList());
    }

    private CtLocalVariable<Object> buildVarStatement(CtExpression arg, String id) {
        CtTypeReference<Object> objectType = getFactory().Core().createTypeReference();
        objectType.setSimpleName("Object");
        CtLocalVariable<Object> localVar = getFactory().Code().createLocalVariable(objectType, "o_" + id, arg);
        DSpotUtils.addComment(localVar, "MethodAssertGenerator build local variable", CtComment.CommentType.INLINE);
        return localVar;
    }

    private Factory getFactory() {
        return test.getFactory();
    }

    private boolean isAssert(CtStatement statement) {
        if (statement instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) statement;
            try {
                String signature = invocation.getExecutable().getSimpleName();
                return (signature.contains("assertTrue")
                        || signature.contains("assertFalse")
                        || signature.contains("assertSame")
                        || signature.contains("assertEquals"));
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
