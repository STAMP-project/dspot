package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.compare.ObjectLog;
import fr.inria.diversify.compare.Observation;
import fr.inria.diversify.dspot.TypeUtils;
import fr.inria.diversify.dspot.support.Counter;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.JunitRunner;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import org.junit.runner.notification.Failure;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 22/10/15
 * Time: 10:06
 */
public class MethodAssertGenerator {

    protected CtMethod test;
    private ClassLoader assertGeneratorClassLoader;
    private CtType originalClass;
    private DSpotCompiler compiler;
    private InputProgram inputProgram;
    private List<Integer> statementsIndexToAssert;

    public MethodAssertGenerator(CtType originalClass, InputProgram inputProgram, DSpotCompiler compiler, ClassLoader applicationClassLoader) throws IOException {
        this.originalClass = originalClass;
        this.compiler = compiler;
        this.assertGeneratorClassLoader = applicationClassLoader;

        this.inputProgram = inputProgram;
        statementsIndexToAssert = new ArrayList<>();
    }

    public CtMethod generateAssert(CtMethod test) throws IOException, ClassNotFoundException {
        this.test = test;
        this.test = createTestWithoutAssert(new ArrayList<>(), false);
        this.test.setParent(test.getParent());
        for(int i = 0; i < Query.getElements(this.test, new TypeFilter(CtStatement.class)).size(); i++) {
            statementsIndexToAssert.add(i);
        }
        CtMethod newTest = generateAssert();
        if(newTest == null || !isCorrect(newTest)) {
            return null;
        }
        return newTest;
    }

    public CtMethod generateAssert(CtMethod test, List<Integer> statementsIndexToAssert) throws IOException, ClassNotFoundException {
        this.test = test;
        this.statementsIndexToAssert = statementsIndexToAssert;
        CtMethod newTest = generateAssert();
        if(newTest == null || !isCorrect(newTest)) {
            return null;
        }
        return newTest;
    }

    private CtMethod generateAssert() throws IOException, ClassNotFoundException {
        List<CtMethod> testsToRun = new ArrayList<>();
        CtType classTest = initTestClass();

        CtMethod cloneTest = test.clone();
        classTest.addMethod(cloneTest);
        testsToRun.add(cloneTest);

        CtMethod testWithoutAssert = createTestWithoutAssert(new ArrayList<>(), false);
        testsToRun.add(testWithoutAssert);
        classTest.addMethod(testWithoutAssert);

        JunitResult result = runTests(classTest, testsToRun);
        if(result == null || result.getTestRuns().size() != testsToRun.size()) {
            return null;
        }
        try {
            String testWithoutAssertName = test.getSimpleName() + "_withoutAssert";
            if(testFailed(testWithoutAssertName, result)) {
                return makeFailureTest(getFailure(testWithoutAssertName, result));
            } else if(!testFailed(test.getSimpleName(), result)) {
                if(!statementsIndexToAssert.isEmpty()) {
                    return buildNewAssert();
                }
            } else {
                removeFailAssert();
                if(!statementsIndexToAssert.isEmpty()) {
                    return buildNewAssert();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected CtMethod makeFailureTest(Failure failure) {
        CtMethod testWithoutAssert = createTestWithoutAssert(new ArrayList<>(), false);
        testWithoutAssert.setSimpleName(test.getSimpleName());
        Factory factory = testWithoutAssert.getFactory();

        Throwable exception = failure.getException();
        if(exception instanceof  AssertionError)   {
            exception = exception.getCause();
        }
        Class exceptionClass;
        if(exception == null) {
            exceptionClass = Throwable.class;
        } else {
            exceptionClass = exception.getClass();
        }

        CtTry tryBlock = factory.Core().createTry();
        tryBlock.setBody(testWithoutAssert.getBody());
        String snippet = "junit.framework.TestCase.fail(\"" +test.getSimpleName()+" should have thrown " + exceptionClass.getSimpleName()+"\")";
        tryBlock.getBody().addStatement(factory.Code().createCodeSnippetStatement(snippet));

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
        return testWithoutAssert;
    }

    private CtMethod buildNewAssert() throws IOException, ClassNotFoundException {
        CtType cl = initTestClass();
        List<CtMethod> testsToRun = new ArrayList<>();


        for(int i = 0; i < 3; i++) {
                CtMethod testWithLog = createTestWithLog();
                testWithLog.setSimpleName(testWithLog.getSimpleName() + i);
                cl.addMethod(testWithLog);
                testsToRun.add(testWithLog);
        }

        ObjectLog.reset();

        runTests(cl, testsToRun);
        return buildTestWithAssert(ObjectLog.getObservations());
    }

    private CtMethod buildTestWithAssert(Map<String, Observation> observations) {
        CtMethod testWithAssert = test.clone();
//        testWithAssert.setParent(test.getParent());
        // add throws

        int numberOfAddedAssertion = 0;

        List<CtStatement> statements = Query.getElements(testWithAssert, new TypeFilter(CtStatement.class));
        for(String id : observations.keySet()) {
           int line = Integer.parseInt(id.split("__")[1]);
            List<String> asserts = observations.get(id).buildAssert();
            for(String snippet : asserts) {
                CtStatement assertStmt = getFactory().Code().createCodeSnippetStatement(snippet);
                try {
                    CtStatement stmt = statements.get(line);
                    if (stmt instanceof CtInvocation && !isVoidReturn((CtInvocation) stmt)) {
                        String localVarSnippet = ((CtInvocation) stmt).getType().toString()
                                + " o_" + id + " = "
                                + stmt.toString();
                        CtStatement localVarStmt = getFactory().Code().createCodeSnippetStatement(localVarSnippet);
                        stmt.replace(localVarStmt);
                        statements.set(line, localVarStmt);
                        localVarStmt.setParent(stmt.getParent());
                        localVarStmt.insertAfter(assertStmt);
                    } else {
                        stmt.insertAfter(assertStmt);
                    }
                    numberOfAddedAssertion++;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.debug("Exception has been thrown during generation of assertion");
                }
            }
        }
        Counter.updateAssertionOf(testWithAssert, numberOfAddedAssertion);
        return testWithAssert;
    }

    private boolean isCorrect(CtMethod test) throws IOException, ClassNotFoundException {
        JunitResult result = runSingleTest(test);
        return result != null && result.getFailures().isEmpty();
    }

    protected CtMethod removeFailAssert() throws IOException, ClassNotFoundException {
        List<Integer> goodAssert = findGoodAssert();
        String testName = test.getSimpleName();
        test = createTestWithoutAssert(goodAssert, true);
        test.setSimpleName(testName);

        return test;
    }

    private List<Integer> findGoodAssert() throws IOException, ClassNotFoundException {
        int stmtIndex = 0;
        List<CtMethod> testsToRun = new ArrayList<>();
        List<Integer> assertIndex = new ArrayList<>();
        List<CtStatement> statements = Query.getElements(test, new TypeFilter(CtStatement.class));
        for(CtStatement statement : statements) {
            if (isAssert(statement)) {
                assertIndex.add(stmtIndex);
            }
            stmtIndex++;
        }

        CtType newClass = originalClass.clone();
        newClass.setParent(originalClass.getParent());
        for(int i = 0; i < assertIndex.size(); i++) {
            List<Integer> assertToKeep = new ArrayList<>();
            assertToKeep.add(assertIndex.get(i));
            CtMethod mth = createTestWithoutAssert(assertToKeep, false);
            mth.setSimpleName(mth.getSimpleName() + "_" + i);
            newClass.addMethod(mth);
            testsToRun.add(mth);
        }
        ObjectLog.reset();
        JunitResult result = runTests(newClass, testsToRun);

        List<Integer> goodAssertIndex = new ArrayList<>();
        for(int i = 0; i < testsToRun.size(); i++) {
            if(!testFailed(testsToRun.get(i).getSimpleName(), result)) {
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

    private JunitResult runTests(CtType testClass, List<CtMethod> testsToRun) throws ClassNotFoundException {
        DiversifyClassLoader diversifyClassLoader = new DiversifyClassLoader(assertGeneratorClassLoader, compiler.getBinaryOutputDirectory().getAbsolutePath());
        if(!writeAndCompile(testClass)) {
            return null;
        }
        List<String> ClassName = Collections.singletonList(testClass.getQualifiedName());
        diversifyClassLoader.setClassFilter(ClassName);
        JunitRunner junitRunner = new JunitRunner(diversifyClassLoader);
        String currentUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", inputProgram.getProgramDir());
        Logger.reset();
        Logger.setLogDir(new File(inputProgram.getProgramDir() + "/log"));
        junitRunner.setClassTimeOut(240);
        junitRunner.setMethodTimeOut(10);
        JunitResult result = junitRunner.runTestClasses(ClassName, testsToRun.stream().map(test -> test.getSimpleName()).collect(Collectors.toList()));
        System.setProperty("user.dir", currentUserDir);
        return result;
    }

    protected JunitResult runSingleTest(CtMethod test) throws ClassNotFoundException, IOException {
        CtType cloneClass = initTestClass();
        CtMethod cloneTest = test.clone();
        cloneClass.addMethod(cloneTest);
        return runTests(cloneClass, Collections.singletonList(cloneTest));
    }

    //todo refactor
    private boolean writeAndCompile(CtType cl) {
        try {
            //TODO Ugly try-catch block but no time to waste.
            try {
                FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
                FileUtils.cleanDirectory(compiler.getBinaryOutputDirectory());
            } catch (FileNotFoundException | IllegalArgumentException ignored) {
                Log.warn("error during cleaning output directories");
                //ignored
            }
            copyLoggerFile();
            PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), cl);
            return compiler.compileFileIn(compiler.getSourceOutputDirectory(), true);
        } catch (Exception e) {
            Log.debug("error during compilation", e);
            return false;
        }
    }

    private CtType initTestClass() {
        CtType clone = originalClass.clone();
        clone.setParent(originalClass.getParent());
        return clone;
    }

    private CtMethod createTestWithLog() {
        CtMethod newTest = test.clone();
        newTest.setSimpleName(test.getSimpleName() + "_withlog");
        List<CtStatement> stmts = Query.getElements(newTest, new TypeFilter(CtStatement.class));
        for(int i = 0; i < stmts.size(); i++) {
            CtStatement stmt = stmts.get(i);
            if(isStmtToLog(stmt)) {
                addLogStmt(stmt, test.getSimpleName() + "__" + i, statementsIndexToAssert.contains(i));
            }
        }
        return newTest;
    }

    private boolean isStmtToLog(CtStatement statement) {
        if(!(statement.getParent() instanceof CtBlock)) {
            return false;
        }
        if(statement instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) statement;

            //type tested by the test class
            String targetType = "";
            if(invocation.getTarget() != null &&
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

    private void addLogStmt(CtStatement stmt, String id, boolean forAssert) {
        String snippet;
        if(forAssert) {
            snippet = "fr.inria.diversify.compare.ObjectLog.log(";
        } else {
            snippet = "fr.inria.diversify.compare.ObjectLog.logObject(";
        }

        CtStatement insertAfter = null;
        if(stmt instanceof CtVariableWrite) {
            CtVariableWrite varWrite = (CtVariableWrite) stmt;
            snippet += varWrite.getVariable()
                    + ",\"" + varWrite.getVariable() + "\",\"" + id + "\")";
            insertAfter = stmt;
        }
        if(stmt instanceof CtLocalVariable) {
            CtLocalVariable localVar = (CtLocalVariable) stmt;
            snippet += localVar.getSimpleName()
                    + ",\"" + localVar.getSimpleName() + "\",\"" + id + "\")";
            insertAfter = stmt;
        }
        if(stmt instanceof CtAssignment) {
            CtAssignment localVar = (CtAssignment) stmt;
            snippet += localVar.getAssigned()
                    + ",\"" + localVar.getAssigned() + "\",\"" + id + "\")";
            insertAfter = stmt;
        }

        if(stmt instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) stmt;
            if (isVoidReturn(invocation)) {
                insertAfter = invocation;
                snippet += invocation.getTarget()
                        + ",\"" + invocation.getTarget() + "\",\"" + id + "\")";
            } else {
                String snippetStmt = "Object o_" + id + " = " + invocation.toString();
                CtStatement localVarSnippet = getFactory().Code().createCodeSnippetStatement(snippetStmt);
                stmt.replace(localVarSnippet);
                insertAfter = localVarSnippet;

                snippet += "o_" + id
                        + ",\"o_" + id + "\",\"" + id + "\")";

            }
        }
        CtStatement logStmt = getFactory().Code().createCodeSnippetStatement(snippet);
        insertAfter.insertAfter(logStmt);
    }

    protected CtMethod createTestWithoutAssert(List<Integer> assertIndexToKeep, boolean updateStatementsIndexToAssert) {
        CtMethod newTest = getFactory().Core().clone(test);
        newTest.setSimpleName(test.getSimpleName() + "_withoutAssert");

        int stmtIndex = 0;
        List<CtStatement> statements = Query.getElements(newTest, new TypeFilter(CtStatement.class));
        for(CtStatement statement : statements){
            try {
                if (!assertIndexToKeep.contains(stmtIndex) && isAssert(statement)) {
                    CtBlock block = buildRemoveAssertBlock((CtInvocation) statement, stmtIndex);
                    if(updateStatementsIndexToAssert) {
                        updateStatementsIndexToAssert(stmtIndex, block.getStatements().size() - 1);
                    }
                    if(statement.getParent() instanceof CtCase) {
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
        statement.replace(statements.get(0));
        for (int i = 1; i < statements.size(); i++) {
            statement.insertAfter(statements.get(i));
        }
    }

    private void updateStatementsIndexToAssert(int stmtIndex, int update) {
        if(update != 0) {
            List<Integer> newList = new ArrayList<>(statementsIndexToAssert.size());
            for (Integer index : statementsIndexToAssert) {
                if(index > stmtIndex) {
                    statementsIndexToAssert.add(index + update);
                } else {
                    newList.add(index);
                }
            }
            statementsIndexToAssert = newList;
        }
    }

    private CtBlock buildRemoveAssertBlock(CtInvocation assertInvocation, int blockId) {
        CtBlock block = getFactory().Core().createBlock();

        int[] idx = { 0 };
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

        return localVar;
    }

    private Factory getFactory() {
        return test.getFactory();
    }

    private boolean isAssert(CtStatement statement) {
        if(statement instanceof CtInvocation) {
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

    private void copyLoggerFile() throws IOException {
        String comparePackage = ObjectLog.class.getPackage().getName().replace(".", "/");
        File srcDir = new File(System.getProperty("user.dir") + "/src/main/java/" + comparePackage);

        File destDir = new File(compiler.getSourceOutputDirectory() + "/" + comparePackage);
        FileUtils.forceMkdir(destDir);

        FileUtils.copyDirectory(srcDir, destDir);

        String typeUtilsPackage = TypeUtils.class.getPackage().getName().replace(".", "/");
        File srcFile = new File(System.getProperty("user.dir") + "/src/main/java/" + typeUtilsPackage + "/TypeUtils.java");

        destDir = new File(compiler.getSourceOutputDirectory() + "/" + typeUtilsPackage);
        FileUtils.forceMkdir(destDir);

        File destFile = new File(compiler.getSourceOutputDirectory() + "/" + typeUtilsPackage + "/TypeUtils.java");
        FileUtils.copyFile(srcFile, destFile);
    }
}
