package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.compare.MethodsHandler;
import eu.stamp_project.compare.ObjectLog;
import eu.stamp_project.utils.CloneHelper;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtWildcardReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.SpoonClassNotFoundException;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/3/17
 */
public class AssertGeneratorHelper {

    static boolean containsObjectReferences(String candidate) {
        return candidate != null &&
                Pattern.compile("(\\w+\\.)*\\w@[a-f0-9]+").matcher(candidate).find();
    }

    static boolean isCorrectReturn(CtInvocation<?> invocation) {
        return invocation.getType() != null &&
                !(isVoidReturn(invocation)) &&
                !(invocation.getType() instanceof CtWildcardReference) &&
                invocation.getType().getTypeDeclaration() != null &&
                !("java.lang.Class".equals(invocation.getType().getTypeDeclaration().getQualifiedName()));
    }

    static boolean isVoidReturn(CtInvocation invocation) {
        return (invocation.getType().equals(invocation.getFactory().Type().voidType()) ||
                invocation.getType().equals(invocation.getFactory().Type().voidPrimitiveType()));
    }

    static CtMethod<?> createTestWithLog(CtMethod test, final String filter,
                                         List<CtLocalVariable<?>> ctVariableReads) {
        CtMethod clone = CloneHelper.cloneTestMethodNoAmp(test);
        clone.setSimpleName(test.getSimpleName() + "_withlog");
        final List<CtStatement> allStatement = clone.getElements(new TypeFilter<>(CtStatement.class));
        allStatement.stream()
                .filter(statement -> isStmtToLog(filter, statement) || ctVariableReads.contains(statement))
                .forEach(statement ->
                        addLogStmt(statement,
                                test.getSimpleName() + "__" + indexOfByRef(allStatement, statement))
                );
        return clone;
    }

    static void addAfterClassMethod(CtType<?> testClass) {
        // get AfterClassMethod is exist otherwise use initAfterClassMethod
        final Factory factory = testClass.getFactory();
        final CtMethod<?> afterClassMethod = testClass.getMethods()
                .stream()
                .filter(method ->
                        method.getAnnotations()
                                .stream()
                                .anyMatch(ctAnnotation ->
                                        "org.junit.AfterClass".equals(ctAnnotation.getAnnotationType().getQualifiedName())
                                )
                ).findFirst()
                .orElse(initAfterClassMethod(factory));
        final CtTypeReference<?> ctTypeReference = factory.createCtTypeReference(ObjectLog.class);
        final CtExecutableReference<?> reference = ctTypeReference
                .getTypeDeclaration()
                .getMethodsByName("save")
                .get(0)
                .getReference();
        afterClassMethod.getBody().insertEnd(
                factory.createInvocation(factory.createTypeAccess(ctTypeReference),
                        reference)
        );
        testClass.addMethod(afterClassMethod);
    }

    private static CtMethod<Void> initAfterClassMethod(Factory factory) {
        final CtMethod<Void> afterClassMethod = factory.createMethod();
        afterClassMethod.setType(factory.Type().VOID_PRIMITIVE);
        afterClassMethod.addModifier(ModifierKind.PUBLIC);
        afterClassMethod.addModifier(ModifierKind.STATIC);
        afterClassMethod.setSimpleName("afterClass");
        final CtAnnotation annotation = factory.createAnnotation();
        annotation.setAnnotationType(factory.Annotation().create("org.junit.AfterClass").getReference());
        afterClassMethod.addAnnotation(annotation);
        afterClassMethod.setBody(factory.createBlock());
        return afterClassMethod;
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
                MethodsHandler.isASupportedMethodName(invocation.getExecutable().getSimpleName());
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
            return (isCorrectReturn(invocation)
                    && !isGetter(invocation));
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

    private static int getSize(CtBlock<?> block) {
        return block.getStatements().size() +
                block.getStatements().stream()
                        .filter(statement -> statement instanceof CtBlock)
                        .mapToInt(childBlock -> AssertGeneratorHelper.getSize((CtBlock<?>) childBlock))
                        .sum();
    }

    // This method will add a log statement at the given statement AND at the end of the test.
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

        // clone the statement invocation for add it to the end of the tests
        CtInvocation invocationToObjectLogAtTheEnd = invocationToObjectLog.clone();
        invocationToObjectLogAtTheEnd.addArgument(stmt.getFactory().createLiteral(id + "___" + "end"));
        invocationToObjectLog.addArgument(stmt.getFactory().createLiteral(id));

        //TODO checks this if this condition is ok.
        if (getSize(stmt.getParent(CtMethod.class).getBody()) + 1 < 65535) {
            insertAfter.insertAfter(invocationToObjectLog);
        }

        // if between the two log statements there is only log statement, we do not add the log end statement
        if (shouldAddLogEndStatement.test(invocationToObjectLog) &&
                getSize(stmt.getParent(CtMethod.class).getBody()) + 1 < 65535) {
            stmt.getParent(CtBlock.class).insertEnd(invocationToObjectLogAtTheEnd);
        }
    }

    private static final Predicate<CtStatement> shouldAddLogEndStatement = statement -> {
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

    /**
     * Builds an invocation to <code>methodName</code> of {@link org.junit.Assert}.
     * This should be a correct method name such as assertEquals, assertTrue...
     *
     * @param factory    the factory used to build the invocation
     * @param methodName the name of the assertion method
     * @param arguments  the arguments of the assertion, <i>e.g.</i> the two element to be compared in {@link org.junit.Assert#assertEquals(Object, Object)}
     * @return a spoon node representing the invocation to the assertion, ready to be inserted in a test method
     */
    public static CtInvocation buildInvocation(Factory factory, String methodName, List<CtExpression> arguments) {
        final CtInvocation invocation = factory.createInvocation();
        final CtExecutableReference<?> executableReference = factory.Core().createExecutableReference();
        executableReference.setStatic(true);
        executableReference.setSimpleName(methodName);
        executableReference.setDeclaringType(factory.Type().createReference("org.junit.Assert"));
        invocation.setExecutable(executableReference);
        invocation.setArguments(arguments); // TODO
        invocation.setType(factory.Type().voidPrimitiveType());
        invocation.setTarget(factory.createTypeAccess(factory.Type().createReference("org.junit.Assert")));
        invocation.putMetadata(METADATA_ASSERT_AMPLIFICATION, true);
        return invocation;
    }

    public final static String METADATA_ASSERT_AMPLIFICATION = "A-Amplification";
}
