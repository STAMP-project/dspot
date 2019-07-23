package eu.stamp_project.dspot.assertgenerator.components.testmethodreconstructor.observer;

import eu.stamp_project.compare.MethodsHandler;
import eu.stamp_project.compare.ObjectLog;
import eu.stamp_project.dspot.assertgenerator.components.utils.AssertionGeneratorUtils;
import eu.stamp_project.utils.CloneHelper;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtWildcardReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.SpoonClassNotFoundException;
import java.util.List;
import java.util.function.Predicate;

public class ObserverUtils {

    private static final Predicate<CtStatement> shouldAddLogEndStatement = shouldAddLogEndStatement();

    /**
     *
     * @param test
     * @param filter
     * @param ctVariableReads
     * @return
     */
    public static CtMethod<?> createTestWithLog(CtMethod test, final String filter,
                                                List<CtLocalVariable<?>> ctVariableReads) {
        CtMethod clone = CloneHelper.cloneTestMethodNoAmp(test);
        clone.setSimpleName(test.getSimpleName() + "_withlog");
        final List<CtStatement> allStatement = clone.getElements(new TypeFilter<>(CtStatement.class));
        allStatement.stream()
                .filter(statement ->
                        (ObserverUtils.isStmtToLog(filter, statement) ||
                                ctVariableReads.contains(statement)) &&
                                isNotFromPreviousAmplification(allStatement, statement, test)
                ).forEach(statement ->
                addLogStmt(statement,
                        test.getSimpleName() + "__" + indexOfByRef(allStatement, statement))
        );
        return clone;
    }

    private static boolean isStmtToLog(String filter, CtStatement statement) {
        if (!(statement.getParent() instanceof CtBlock)) {
            return false;
        }

        // contract: for now, we do not log values inside loop
        if (statement.getParent(CtLoop.class) != null) {
            return false;
        }
        if (statement instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) statement;
            return (invocation.getMetadata(AssertionGeneratorUtils.METADATA_WAS_IN_ASSERTION) != null &&
                    (Boolean) invocation.getMetadata(AssertionGeneratorUtils.METADATA_WAS_IN_ASSERTION)) ||
                    (isCorrectReturn(invocation) && !isGetter(invocation));
        }
        if (statement instanceof CtLocalVariable ||
                statement instanceof CtAssignment ||
                statement instanceof CtVariableWrite) {
            if (statement instanceof CtNamedElement) {
                if (((CtNamedElement) statement).getSimpleName()
                        .startsWith("__DSPOT_")) {
                    return false;
                }
            }
            final CtTypeReference type = ((CtTypedElement) statement).getType();
            if (type.getQualifiedName().startsWith(filter)) {
                return true;
            } else {
                try {
                    return type.getTypeDeclaration().getQualifiedName()
                            .equals("java.lang.String");
                } catch (SpoonClassNotFoundException e) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static boolean isGetter(CtInvocation invocation) {
        return invocation.getArguments().isEmpty() &&
                MethodsHandler.isASupportedMethodName(invocation.getExecutable().getSimpleName());
    }

    private static boolean isCorrectReturn(CtInvocation<?> invocation) {
        return invocation.getType() != null &&
                !(AssertionGeneratorUtils.isVoidReturn(invocation)) &&
                !(invocation.getType() instanceof CtWildcardReference) &&
                invocation.getType().getTypeDeclaration() != null &&
                !("java.lang.Class".equals(invocation.getType().getTypeDeclaration().getQualifiedName()));
    }

    // This method aims at infer from the name of the local variables if it came from a previous amplification.
    // see the use case available here: https://github.com/STAMP-project/dspot/issues/825
    private static boolean isNotFromPreviousAmplification(final List<CtStatement> allStatement,
                                                          CtStatement statement,
                                                          CtMethod<?> test) {
        final String id = test.getSimpleName() + "__" + ObserverUtils.indexOfByRef(allStatement, statement);
        return test.getElements(new TypeFilter<CtLocalVariable>(CtLocalVariable.class) {
            @Override
            public boolean matches(CtLocalVariable element) {
                return element.getSimpleName().endsWith(id);
            }
        }).isEmpty();
    }

    private static int indexOfByRef(List<CtStatement> statements, CtStatement statement) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) == statement) {
                return i;
            }
        }
        return -1;
    }

    // This method will add a log statement at the given statement AND at the end of the test.
    @SuppressWarnings("unchecked")
    private static void addLogStmt(CtStatement stmt, String id) {
        if (stmt instanceof CtLocalVariable && ((CtLocalVariable) stmt).getDefaultExpression() == null) {
            return;
        }

        final Factory factory = stmt.getFactory();

        final CtTypeAccess<ObjectLog> typeAccess = factory.createTypeAccess(
                factory.Type().createReference(ObjectLog.class)
        );

        final CtExecutableReference objectLogExecRef = factory.createExecutableReference()
                .setStatic(true)
                .setDeclaringType(factory.Type().createReference(ObjectLog.class))
                .setSimpleName("log");
        objectLogExecRef.setType(factory.Type().voidPrimitiveType());

        final CtInvocation invocationToObjectLog = factory.createInvocation(typeAccess, objectLogExecRef);

        CtStatement insertAfter;
        if (stmt instanceof CtVariableWrite) {//TODO
            insertAfter = stmt;
        } else if (stmt instanceof CtLocalVariable) {
            insertAfter = localVariableLogStmt(stmt,factory,invocationToObjectLog);
        } else if (stmt instanceof CtAssignment) {
            insertAfter = assignmentLogStmt(stmt,factory,invocationToObjectLog);
        } else if (stmt instanceof CtInvocation) {
            insertAfter = invocationLogStmt(stmt,factory,invocationToObjectLog,id);
        } else if (stmt instanceof CtConstructorCall) {
            insertAfter = constructorCallLogStmt(stmt,factory,invocationToObjectLog,id);
        } else {
            throw new RuntimeException("Could not find the proper type to add log statement" + stmt.toString());
        }
        CtInvocation invocationToObjectLogAtTheEnd = insertStatement(stmt,factory,invocationToObjectLog,id,insertAfter);

        // if between the two log statements there is only log statement, we do not add the log end statement
        if (ObserverUtils.shouldAddLogEndStatement.test(invocationToObjectLog) &&
                ObserverUtils.getSize(stmt.getParent(CtMethod.class).getBody()) + 1 < 65535) {
            stmt.getParent(CtBlock.class).insertEnd(invocationToObjectLogAtTheEnd);
        }
    }

    private static CtStatement localVariableLogStmt(CtStatement stmt, Factory factory, CtInvocation invocationToObjectLog){
        CtLocalVariable localVar = (CtLocalVariable) stmt;
        final CtVariableAccess variableRead = factory.createVariableRead(localVar.getReference(), false);// TODO checks static
        invocationToObjectLog.addArgument(variableRead);
        invocationToObjectLog.addArgument(factory.createLiteral(localVar.getSimpleName()));
        return stmt;
    }

    private static CtStatement assignmentLogStmt(CtStatement stmt, Factory factory, CtInvocation invocationToObjectLog){
        CtAssignment localVar = (CtAssignment) stmt;
        invocationToObjectLog.addArgument(localVar.getAssigned());
        invocationToObjectLog.addArgument(factory.createLiteral(localVar.getAssigned().toString()));
        return stmt;
    }

    private static CtStatement invocationLogStmt(CtStatement stmt, Factory factory,
                                                 CtInvocation invocationToObjectLog, String id) {

        // in case of a new Something() or a method call,
        // we put the new Something() in a local variable
        // then we replace it by the an access to this local variable
        // and we add a log statement on it
        CtInvocation invocation = (CtInvocation) stmt;
        if (AssertionGeneratorUtils.isVoidReturn(invocation)) {
            invocationToObjectLog.addArgument(invocation.getTarget());
            invocationToObjectLog.addArgument(factory.createLiteral(
                    invocation.getTarget().toString().replace("\"", "\\\""))
            );
            return invocation;
        } else {
            final CtLocalVariable localVariable = factory.createLocalVariable(
                    AssertionGeneratorUtils.getCorrectTypeOfInvocation(invocation),
                    "o_" + id,
                    invocation.clone()
            );
            try {
                stmt.replace(localVariable);
            } catch (ClassCastException e) {
                throw new RuntimeException(e);
            }
            invocationToObjectLog.addArgument(factory.createVariableRead(localVariable.getReference(), false));
            invocationToObjectLog.addArgument(factory.createLiteral("o_" + id));
            return localVariable;
        }
    }

    private static CtStatement constructorCallLogStmt(CtStatement stmt, Factory factory,
                                                      CtInvocation invocationToObjectLog, String id) {
        final CtConstructorCall constructorCall = (CtConstructorCall<?>) stmt;
        final CtLocalVariable<?> localVariable = factory.createLocalVariable(
                constructorCall.getType(),
                "o_" + id,
                constructorCall.clone()
        );
        try {
            AssertionGeneratorUtils.getTopStatement(stmt).insertBefore(localVariable);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
        try {
            stmt.replace(factory.createVariableRead(localVariable.getReference(), false));
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
        invocationToObjectLog.addArgument(factory.createVariableRead(localVariable.getReference(), false));
        invocationToObjectLog.addArgument(factory.createLiteral("o_" + id));
        return localVariable;
    }

    // clone the statement invocation for add it to the end of the tests
    private static CtInvocation insertStatement(CtStatement stmt, Factory factory,
                                 CtInvocation invocationToObjectLog, String id, CtStatement insertAfter){
        CtInvocation invocationToObjectLogAtTheEnd = invocationToObjectLog.clone();
        invocationToObjectLogAtTheEnd.addArgument(factory.createLiteral(id + "___" + "end"));
        invocationToObjectLog.addArgument(factory.createLiteral(id));

        //TODO checks this if this condition is ok.
        if (ObserverUtils.getSize(stmt.getParent(CtMethod.class).getBody()) + 1 < 65535) {
            insertAfter.insertAfter(invocationToObjectLog);
        }
        return invocationToObjectLogAtTheEnd;
    }

    private static int getSize(CtBlock<?> block) {
        return block.getStatements().size() +
                block.getStatements().stream()
                        .filter(statement -> statement instanceof CtBlock)
                        .mapToInt(childBlock -> getSize((CtBlock<?>) childBlock))
                        .sum();
    }

    private static Predicate<CtStatement> shouldAddLogEndStatement() {
        return statement -> {
            final List<CtStatement> statements = statement.getParent(CtBlock.class).getStatements();
            for (int i = statements.indexOf(statement) + 1; i < statements.size(); i++) {
                if (!(statements.get(i) instanceof CtInvocation) ||
                        !((CtInvocation) statements.get(i)).getTarget().equals(statement.getFactory().createTypeAccess(
                                statement.getFactory().Type().createReference(ObjectLog.class)))) {
                    return true;
                }
            }
            return false;
        };
    }
}
