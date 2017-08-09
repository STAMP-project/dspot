package fr.inria.stamp.minimization;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtVariableWriteImpl;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/08/17
 */
public class Minimization {

	private static final String classAssertion = "org.junit.Assert";

	public static CtMethod<?> minimize(CtMethod<?> methodToMinimize) {
		return removeUselessAssertion(inlineCtLocalVariable(methodToMinimize));
	}

	private static CtMethod<?> removeUselessAssertion(CtMethod<?> methodToMinimize) {
		methodToMinimize.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
			@Override
			public boolean matches(CtInvocation element) {
				return classAssertion.equals(element.getExecutable().getDeclaringType().toString());
			}
		}).stream()
				.filter(invocation ->
						invocation.getArguments()
								.stream()
								.allMatch(o -> o instanceof CtLiteral ||
										(o instanceof CtUnaryOperator &&
												((CtUnaryOperator) o).getOperand() instanceof CtLiteral)
								)
				)
				.forEach(invocation -> methodToMinimize.getBody().removeStatement(invocation));
		return methodToMinimize;
	}

	private static CtMethod<?> inlineCtLocalVariable(CtMethod<?> methodToMinimize) {
		final List<CtLocalVariable> localVariables =
				methodToMinimize.getElements(new TypeFilter<>(CtLocalVariable.class));
		localVariables.stream()
				.forEach(localVariable -> {
							final List<CtVariableAccess> variableAccesses = methodToMinimize.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class) {
								@Override
								public boolean matches(CtVariableAccess element) {
									return element.getVariable().equals(localVariable.getReference());
								}
							});
							if (variableAccesses.isEmpty()) {
								methodToMinimize.getBody().removeStatement(localVariable);
							} else if (variableAccesses.size() == 1) {
								variableAccesses.get(0).replace(localVariable.getAssignment());
								methodToMinimize.getBody().removeStatement(localVariable);
							} else {
								if (canBeInlined(variableAccesses)) {
									variableAccesses.forEach(ctVariableAccess ->
											ctVariableAccess.replace(localVariable.getAssignment())
									);
									methodToMinimize.getBody().removeStatement(localVariable);
								}
							}
						}
				);
		return methodToMinimize;
	}

	private static boolean canBeInlined(List<CtVariableAccess> variableAccesses) {
		for (int i = 0; i < variableAccesses.size() - 1; i++) {
			CtVariableAccess<?> variableAccess = variableAccesses.get(0);
			if (!canBeInlined.test(variableAccess)) {
				return false;
			}
		}
		return true;
	}

	private static final Predicate<CtVariableAccess> canBeInlined = ctVariableAccess ->
			!(ctVariableAccess instanceof CtVariableWriteImpl) && (
					ctVariableAccess.getParent(CtInvocation.class) == null ||
							ctVariableAccess.getParent(CtInvocation.class) != null &&
									classAssertion.equals(
											ctVariableAccess.getParent(CtInvocation.class)
													.getExecutable().getDeclaringType().toString())
			);
}
