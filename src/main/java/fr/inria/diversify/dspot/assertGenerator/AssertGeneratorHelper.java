package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpotUtils;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.inria.diversify.dspot.AmplificationChecker.isAssert;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/3/17
 */
public class AssertGeneratorHelper {

    static boolean isVoidReturn(CtInvocation invocation) {
        return invocation.getType() != null && (invocation.getType().getSimpleName().equals("Void") || invocation.getType().getSimpleName().equals("void"));
    }

    static CtMethod<?> createTestWithoutAssert(CtMethod<?> test, List<Integer> assertIndexToKeep) {
        CtMethod newTest = test.clone();
        newTest.setSimpleName(test.getSimpleName() + "_withoutAssert");
        int stmtIndex = 0;
        List<CtStatement> statements = Query.getElements(newTest, new TypeFilter(CtStatement.class));
        for (CtStatement statement : statements) {
            try {
                if (!assertIndexToKeep.contains(stmtIndex) && isAssert(statement)) {
                    CtBlock block = buildRemoveAssertBlock(test.getFactory(), (CtInvocation) statement, stmtIndex);
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
            } catch (Exception ignored) {
                //ignored, skipping to the next statement
            }
        }
        return newTest;
    }

    private static CtLocalVariable<Object> buildVarStatement(Factory factory, CtExpression arg, String id) {
        CtTypeReference<Object> objectType = factory.Core().createTypeReference();
        objectType.setSimpleName("Object");
        CtLocalVariable<Object> localVar = factory.Code().createLocalVariable(objectType, "o_" + id, arg);
        DSpotUtils.addComment(localVar, "MethodAssertGenerator build local variable", CtComment.CommentType.INLINE);
        return localVar;
    }

    private static List<CtExpression> getNotLiteralArgs(CtInvocation invocation) {
        List<CtExpression> args = invocation.getArguments();
        return args.stream()
                .filter(arg -> !(arg instanceof CtLiteral))
                .collect(Collectors.toList());
    }

    static void replaceStatementByListOfStatements(CtStatement statement, List<CtStatement> statements) {
        String oldStatement = statement.toString();
        statement.replace(statements.get(0));
        for (int i = 1; i < statements.size(); i++) {
            statement.insertAfter(statements.get(i));
        }
        DSpotUtils.addComment(statement, "MethodAssertion Generator replaced " + oldStatement, CtComment.CommentType.BLOCK);
    }

    static CtBlock buildRemoveAssertBlock(Factory factory, CtInvocation assertInvocation, int blockId) {
        CtBlock block = factory.Core().createBlock();
        int[] idx = {0};
        getNotLiteralArgs(assertInvocation).stream()
                .filter(arg -> !(arg instanceof CtVariableAccess))
                .map(arg -> buildVarStatement(factory, arg, blockId + "_" + (idx[0]++)))
                .forEach(stmt -> block.addStatement(stmt));

        block.setParent(assertInvocation.getParent());
        return block;
    }

    static Map<CtMethod<?>, List<Integer>> takeAllStatementToAssert(CtType testClass, List<CtMethod<?>> tests) {
        return tests.stream()
                .collect(Collectors.toMap(Function.identity(),
                        ctMethod -> {
                            List<Integer> indices = new ArrayList<>();
                            for (int i = 0; i < Query.getElements(testClass, new TypeFilter(CtStatement.class)).size(); i++) {
                                indices.add(i);
                            }
                            return indices;
                        }
                ));
    }

    static List<Integer> findStatementToAssert(CtMethod<?> test) {
        if (AmplificationHelper.getAmpTestToParent() != null
                && !AmplificationHelper.getAmpTestToParent().isEmpty()
                && AmplificationHelper.getAmpTestToParent().get(test) != null) {
            CtMethod parent = AmplificationHelper.getAmpTestToParent().get(test);
            while (AmplificationHelper.getAmpTestToParent().get(parent) != null) {
                parent = AmplificationHelper.getAmpTestToParent().get(parent);
            }
            return findStatementToAssertFromParent(test, parent);
        } else {
            return findStatementToAssertOnlyInvocation(test);
        }
    }

    static List<Integer> findStatementToAssertOnlyInvocation(CtMethod<?> test) {
        List<CtStatement> stmts = Query.getElements(test, new TypeFilter(CtStatement.class));
        List<Integer> indexs = new ArrayList<>();
        for (int i = 0; i < stmts.size(); i++) {
            if (CtInvocation.class.isInstance(stmts.get(i))) {
                indexs.add(i);
            }
        }
        return indexs;
    }

    static List<Integer> findStatementToAssertFromParent(CtMethod<?> test, CtMethod<?> parentTest) {
        List<CtStatement> originalStmts = Query.getElements(parentTest, new TypeFilter(CtStatement.class));
        List<String> originalStmtStrings = originalStmts.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        List<CtStatement> ampStmts = Query.getElements(test, new TypeFilter(CtStatement.class));
        List<String> ampStmtStrings = ampStmts.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < ampStmtStrings.size(); i++) {
            int index = originalStmtStrings.indexOf(ampStmtStrings.get(i));
            if (index == -1) {
                indices.add(i);
            } else {
                originalStmtStrings.remove(index);
            }
        }
        return indices;
    }

    static CtMethod<?> createTestWithLog(CtMethod test, List<Integer> statementsIndexToAssert, final String simpleNameTestClass) {
        CtMethod clone = test.clone();
        clone.setSimpleName(test.getSimpleName() + "_withlog");
        final List<CtStatement> allStatement = clone.getElements(new TypeFilter<>(CtStatement.class));
        allStatement.stream()
                .filter(statement -> isStmtToLog(simpleNameTestClass, statement))
                .forEach(statement ->
                        addLogStmt(statement,
                                test.getSimpleName() + "__" + indexOfByRef(allStatement, statement),
                                statementsIndexToAssert != null &&
                                        statementsIndexToAssert.contains(allStatement.indexOf(statement)))
                );
        return clone;
    }

    private static int indexOfByRef(List<CtStatement> statements, CtStatement statement) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) == statement) {
                return i;
            }
        }
        return -1;
    }

    static boolean isStmtToLog(String nameOfOriginalClass, CtStatement statement) {
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
            return nameOfOriginalClass.startsWith(targetType)
                    || !isVoidReturn(invocation);
        }
        return statement instanceof CtVariableWrite
                || statement instanceof CtAssignment
                || statement instanceof CtLocalVariable;
    }

    static void addLogStmt(CtStatement stmt, String id, boolean forAssert) {
        if (stmt instanceof CtLocalVariable && ((CtLocalVariable) stmt).getDefaultExpression() == null) {
            return;
        }
        String snippet;
        if (forAssert) {
            snippet = "fr.inria.diversify.compare.ObjectLog.log(";
        } else {
            snippet = "fr.inria.diversify.compare.ObjectLog.logObject(";
        }

        CtStatement insertAfter = null;
        if (stmt instanceof CtVariableWrite) {
            CtVariableWrite varWrite = (CtVariableWrite) stmt;
            snippet += varWrite.getVariable()
                    + ",\"" + varWrite.getVariable() + "\",\"" + id + "\")";
            insertAfter = stmt;
        }
        if (stmt instanceof CtLocalVariable) {
            CtLocalVariable localVar = (CtLocalVariable) stmt;
            snippet += localVar.getSimpleName()
                    + ",\"" + localVar.getSimpleName() + "\",\"" + id + "\")";
            insertAfter = stmt;
        }
        if (stmt instanceof CtAssignment) {
            CtAssignment localVar = (CtAssignment) stmt;
            snippet += localVar.getAssigned()
                    + ",\"" + localVar.getAssigned() + "\",\"" + id + "\")";
            insertAfter = stmt;
        }

        if (stmt instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) stmt;
            if (isVoidReturn(invocation)) {
                insertAfter = invocation;
                snippet += invocation.getTarget()
                        + ",\"" + invocation.getTarget().toString().replace("\"", "\\\"") + "\",\"" + id + "\")";
            } else {
                String snippetStmt = "Object o_" + id + " = " + invocation.toString();
                CtStatement localVarSnippet = stmt.getFactory().Code().createCodeSnippetStatement(snippetStmt);
                stmt.replace(localVarSnippet);
                insertAfter = localVarSnippet;

                snippet += "o_" + id
                        + ",\"o_" + id + "\",\"" + id + "\")";

            }
        }
        CtStatement logStmt = stmt.getFactory().Code().createCodeSnippetStatement(snippet);
        insertAfter.insertAfter(logStmt);
    }

}
