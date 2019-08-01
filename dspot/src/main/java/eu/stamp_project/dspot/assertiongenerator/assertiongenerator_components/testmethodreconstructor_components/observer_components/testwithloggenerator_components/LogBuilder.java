package eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.testmethodreconstructor_components.observer_components.testwithloggenerator_components;

import eu.stamp_project.compare.ObjectLog;
import eu.stamp_project.dspot.assertiongenerator.utils.AssertionGeneratorUtils;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import java.util.List;
import java.util.function.Predicate;

public class LogBuilder {

    private static final Predicate<CtStatement> shouldAddLogEndStatement = shouldAddLogEndStatement();

    /**
     * This method will add a log statement at the given statement AND at the end of the test.
     * @param stmt
     * @param id
     */
    @SuppressWarnings("unchecked")
    public static void addLogStmt(CtStatement stmt, String id) {
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
        if (shouldAddLogEndStatement.test(invocationToObjectLog) &&
                getSize(stmt.getParent(CtMethod.class).getBody()) + 1 < 65535) {
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
        if (getSize(stmt.getParent(CtMethod.class).getBody()) + 1 < 65535) {
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
