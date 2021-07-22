package eu.stamp_project.dspot.assertiongenerator.assertiongenerator;

import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.dspot.common.miscellaneous.CloneHelper;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/11/17
 */
@SuppressWarnings("unchecked")
public class AssertionRemover {

    private final int[] counter = new int[]{0};

    private Map<CtMethod<?>, List<CtLocalVariable<?>>> variableAssertedPerTestMethod = new HashMap<>();

    public Map<CtMethod<?>, List<CtLocalVariable<?>>> getVariableAssertedPerTestMethod() {
        return variableAssertedPerTestMethod;
    }

    /**
     * Removes all assertions from a test.
     *
     * @param testMethod Test method
     * @return Test's clone without any assertion
     */
    public CtMethod<?> removeAssertion(CtMethod<?> testMethod) {
        CtMethod<?> testWithoutAssertion = CloneHelper.cloneTestMethodNoAmp(testMethod);
        variableAssertedPerTestMethod.put(testWithoutAssertion,
                testWithoutAssertion
                        .getElements(TestFramework.ASSERTIONS_FILTER)
                        .stream()
                        .filter(invocation -> !(invocation.getParent() instanceof CtRHSReceiver)) // it means that the return type is used in the test.
                        .flatMap(invocation -> this.removeAssertion(invocation).stream())
                        .collect(Collectors.toList())
        );

        testWithoutAssertion.getElements(new TypeFilter<CtTry>(CtTry.class) {
            @Override
            public boolean matches(CtTry element) {
                return !(element instanceof CtTryWithResource);
            }
        }).forEach(ctTry -> ctTry.insertBefore((CtStatement) ctTry.getBody().clone()));
        testWithoutAssertion.getElements(
                new TypeFilter<CtTry>(CtTry.class) {
                    @Override
                    public boolean matches(CtTry element) {
                        return !(element instanceof CtTryWithResource);
                    }
                }
        ).forEach(testWithoutAssertion.getBody()::removeStatement);

        return testWithoutAssertion;
    }

    /**
     * Can be called after {@link AssertionRemover#removeAssertion(CtMethod)} to remove the statements that
     * previously were arguments of assertions all the way at
     * @param testMethod
     * @return
     */
    public CtMethod<?> removeArgumentsOfTrailingAssertions(CtMethod<?> testMethod) {
        List<CtStatement> testStatements = testMethod.getElements(new TypeFilter<>(CtStatement.class));

        for (int i = testStatements.size() - 1; i >= 0; i--) {
            CtStatement statement = testStatements.get(i);
            Object metadata = statement.getMetadata(AssertionGeneratorUtils.METADATA_WAS_IN_ASSERTION);
            if (metadata != null && (Boolean) metadata) {
                testMethod.getBody().removeStatement(statement);
            } else {
                break;
            }
        }

        return testMethod;
    }

    /**
     * Replaces an invocation with its arguments.
     *
     * @param invocation Invocation
     * @return the list of local variables extracted from assertions
     */
    public List<CtLocalVariable<?>> removeAssertion(CtInvocation<?> invocation) {
        List<CtLocalVariable<?>> variableReadsAsserted = new ArrayList<>();
        final Factory factory = invocation.getFactory();
        final TypeFilter<CtStatement> statementTypeFilter = new TypeFilter<CtStatement>(CtStatement.class) {
            @Override
            public boolean matches(CtStatement element) {
                return element.equals(invocation);
            }
        };
        if (!(invocation.getMetadata(AssertionGeneratorUtils.METADATA_ASSERT_AMPLIFICATION) != null &&
                (boolean) invocation.getMetadata(AssertionGeneratorUtils.METADATA_ASSERT_AMPLIFICATION))) {
            for (CtExpression<?> argument : invocation.getArguments()) {
                CtExpression clone = ((CtExpression) argument).clone();
                clone.getTypeCasts().clear();
                if (clone instanceof CtUnaryOperator) {
                    clone = ((CtUnaryOperator) clone).getOperand();
                }
                if (clone instanceof CtLambda) {
                    CtLambda lambda = ((CtLambda) clone);
                    if (lambda.getBody() != null) {
                        invocation.getParent(CtStatementList.class).insertBefore(
                                statementTypeFilter,
                                factory.createStatementList(lambda.getBody())
                        );
                    } else {
                        // in case of we have something like () -> "string"
                        if (lambda.getExpression() instanceof CtLiteral) {
                            continue;
                        }
                        // TODO check that we support all cases by casting into CtInvocation
                        final CtBlock block = factory.createBlock();
                        block.setStatements(Collections.singletonList((CtInvocation)lambda.getExpression().clone()));
                        invocation.getParent(CtStatementList.class).insertBefore(
                                statementTypeFilter, factory.createStatementList(block)
                        );
                    }
                } else if (clone instanceof CtStatement) {
                    invocation.getParent(CtStatementList.class).insertBefore(statementTypeFilter, (CtStatement) clone);
                    clone.putMetadata(AssertionGeneratorUtils.METADATA_WAS_IN_ASSERTION, true);
                } else if (!(clone instanceof CtLiteral || clone instanceof CtVariableRead)) {
                    // TODO EXPLAIN
                    CtTypeReference<?> typeOfParameter = clone.getType();
                    if (clone.getType()  == null || factory.Type().NULL_TYPE.equals(clone.getType())) {
                        typeOfParameter = factory.Type().createReference(Object.class);
                    }
                    final CtLocalVariable localVariable = factory.createLocalVariable(
                            typeOfParameter,
                            toCorrectJavaIdentifier(typeOfParameter.getSimpleName()) + "_" + counter[0]++,
                            clone
                    );
                    invocation.getParent(CtStatementList.class).insertBefore(statementTypeFilter, localVariable);
                    localVariable.putMetadata(AssertionGeneratorUtils.METADATA_WAS_IN_ASSERTION, true);
                } else if (clone instanceof CtVariableRead && !(clone instanceof CtFieldRead)) {
                    final CtVariableReference variable = ((CtVariableRead) clone).getVariable();
                    final List<CtLocalVariable> assertedVariables = invocation.getParent(CtBlock.class).getElements(
                            localVariable -> localVariable.getSimpleName().equals(variable.getSimpleName())
                            // here, we match the simple name
                            // since the type cannot match with generated elements
                            // for instance, if the original element is a primitive char,
                            // the generated element can be a Character
                            // and thus, the localVariable.getReference().equals(variable) returns false
                            // the contract on name holds since we control it, i.e. variables in
                            // assertions are extracted by us.
                    );
                    // TODO, we can maybe make a precondition on the invocation, and its parents to avoid to this.
                    if (!assertedVariables.isEmpty()) {
                        variableReadsAsserted.add(assertedVariables.get(0));
                    }
                }
            }
        }
        // must find the first statement list to remove the invocation from it, e.g. the block that contains the assertions
        // the assertion can be inside other stuff, than directly in the block
        CtStatement topStatement = AssertionGeneratorUtils.getTopStatement(invocation);
        ((CtStatementList) topStatement.getParent()).removeStatement(topStatement);
        return variableReadsAsserted;
    }

    private static String toCorrectJavaIdentifier(String name) {
        StringBuilder result = new StringBuilder("");
        for (int i = 0; i < name.length(); i++) {
            final char currentChar = name.charAt(i);
            if (!Character.isJavaIdentifierPart(currentChar)) {
                result.append("_");
            } else {
                result.append(currentChar);
            }
        }
        return result.toString();
    }
}
