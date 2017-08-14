package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.compare.ObjectLog;
import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import org.kevoree.log.Log;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.SpoonClassNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.inria.diversify.utils.AmplificationChecker.isAssert;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/3/17
 */
public class AssertGeneratorHelper {

    static boolean isVoidReturn(CtInvocation invocation) {
        return (invocation.getType() != null && (invocation.getType().equals(invocation.getFactory().Type().voidType()) ||
                invocation.getType().equals(invocation.getFactory().Type().voidPrimitiveType())));
    }

    static CtMethod<?> createTestWithLog(CtMethod test, final String filter) {
        CtMethod clone = AmplificationHelper.cloneMethodTest(test, "");
        clone.setSimpleName(test.getSimpleName() + "_withlog");
        final List<CtStatement> allStatement = clone.getElements(new TypeFilter<>(CtStatement.class));
        allStatement.stream()
                .filter(statement -> isStmtToLog(filter, statement))
                .forEach(statement ->
                        addLogStmt(statement,
                                test.getSimpleName() + "__" + indexOfByRef(allStatement, statement))
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

    private static boolean isGetter(CtInvocation invocation) {
        return invocation.getArguments().isEmpty() &&
                (invocation.getExecutable().getSimpleName().startsWith("is") ||
                        invocation.getExecutable().getSimpleName().startsWith("get") ||
                        invocation.getExecutable().getSimpleName().startsWith("should")
                );
    }

    private static boolean isStmtToLog(String filter, CtStatement statement) {
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
            return (filter.startsWith(targetType)
                    || !isVoidReturn(invocation)
                    && !isGetter(invocation));
        }
        if (statement instanceof CtLocalVariable ||
                statement instanceof CtAssignment ||
                statement instanceof CtVariableWrite) {
            final CtTypeReference type = ((CtTypedElement) statement).getType();
            if (type.getQualifiedName().startsWith(filter) ||
                    !type.isPrimitive()) {
                return true;
            } else {
                try {
                    return type.getActualClass() == String.class;
                } catch (SpoonClassNotFoundException e) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }


    @SuppressWarnings("unchecked")
    private static void addLogStmt(CtStatement stmt, String id) {
        if (stmt instanceof CtLocalVariable && ((CtLocalVariable) stmt).getDefaultExpression() == null) {
            return;
        }

        final CtTypeAccess<ObjectLog> typeAccess = stmt.getFactory().createTypeAccess(
                stmt.getFactory().Type().createReference(ObjectLog.class)
        );

        final CtExecutableReference objectLogExecRef = stmt.getFactory().createExecutableReference()
                .setStatic(true)
                .setDeclaringType(stmt.getFactory().Type().createReference(ObjectLog.class))
                .setSimpleName("log");
        objectLogExecRef.setType(stmt.getFactory().Type().voidPrimitiveType());

        final CtInvocation invocationToObjectLog = stmt.getFactory().createInvocation(typeAccess, objectLogExecRef);

        CtStatement insertAfter;
        if (stmt instanceof CtVariableWrite) {//TODO
            CtVariableWrite varWrite = (CtVariableWrite) stmt;
            insertAfter = stmt;
        } else if (stmt instanceof CtLocalVariable) {
            CtLocalVariable localVar = (CtLocalVariable) stmt;
            final CtVariableAccess variableRead = stmt.getFactory().createVariableRead(localVar.getReference(), false);// TODO checks static
            invocationToObjectLog.addArgument(variableRead);
            invocationToObjectLog.addArgument(stmt.getFactory().createLiteral(localVar.getSimpleName()));
            insertAfter = stmt;
        } else if (stmt instanceof CtAssignment) {
            CtAssignment localVar = (CtAssignment) stmt;
            invocationToObjectLog.addArgument(localVar.getAssigned());
            invocationToObjectLog.addArgument(stmt.getFactory().createLiteral(localVar.getAssigned().toString()));
            insertAfter = stmt;
        } else if (stmt instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) stmt;
            if (isVoidReturn(invocation)) {
                invocationToObjectLog.addArgument(invocation.getTarget());
                invocationToObjectLog.addArgument(stmt.getFactory().createLiteral(
                        invocation.getTarget().toString().replace("\"", "\\\""))
                );
                insertAfter = invocation;
            } else {
                final CtLocalVariable localVariable = stmt.getFactory().createLocalVariable(invocation.getType(),
                        "o_" + id, invocation.clone());
                try {
                    stmt.replace(localVariable);
                } catch (ClassCastException e) {
                    throw new RuntimeException(e);
                }
                invocationToObjectLog.addArgument(stmt.getFactory().createVariableRead(localVariable.getReference(), false));
                invocationToObjectLog.addArgument(stmt.getFactory().createLiteral("o_" + id));
                insertAfter = localVariable;
            }
        } else {
            throw new RuntimeException("Could not find the proper type to add log statement" + stmt.toString());
        }
        invocationToObjectLog.addArgument(stmt.getFactory().createLiteral(id));
        insertAfter.insertAfter(invocationToObjectLog);
    }

}
