package fr.inria.diversify.dspot.amplifier;


import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.TypeUtils;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User: Simon
 * Date: 18/11/16
 * Time: 10:40
 */
public class StatementAdd implements Amplifier {

	private boolean shouldGenerateNewObject = true;// TODO
	private int counterGenerateNewObject = 0;
	private String filter;
	private Set<CtMethod> methods;

	public StatementAdd() {
		this.filter = "";
	}

	public StatementAdd(String filter) {
		this.filter = filter;
	}

	@Override
	public List<CtMethod> apply(CtMethod method) {
		// reuse existing object in test to add call to methods
		final List<CtMethod> useExistingObject = useExistingObject(method); // original
		// use results of existing method call to generate new statement.
		final List<CtMethod> useReturnValuesOfExistingMethodCall = useReturnValuesOfExistingMethodCall(method);  // original
		useExistingObject.addAll(useReturnValuesOfExistingMethodCall);
		if (shouldGenerateNewObject) {
			useExistingObject.addAll(generateOnNewObjects(method));
		}
		return useExistingObject;
	}

	private List<CtMethod> generateOnNewObjects(CtMethod method) {
		// generate new objects
		final List<CtMethod> generateNewObjects = generateNewObjects(method);
		final List<CtMethod> methodsWithMoreStatement = generateNewObjects.stream() // + generated at previous step
				.flatMap(ctMethod -> useExistingObject(ctMethod).stream())
				.collect(Collectors.toList());
		methodsWithMoreStatement.addAll(generateNewObjects.stream() // + generated at previous step
				.flatMap(ctMethod -> useReturnValuesOfExistingMethodCall(ctMethod).stream())
				.collect(Collectors.toList()));
		return methodsWithMoreStatement;
	}

	private List<CtMethod> generateNewObjects(CtMethod method) {
		List<CtLocalVariable<?>> existingObjects = getExistingObjects(method);
		final Stream<? extends CtMethod<?>> gen_o1 = existingObjects.stream() // must use tmp variable because javac is confused
				.flatMap(localVariable -> ValueCreator.generateAllConstructionOf(localVariable.getType()).stream())
				.map(ctExpression -> {
							final CtMethod<?> clone = AmplificationHelper.cloneMethodTest(method, "_sd");
							clone.getBody().insertBegin(
									clone.getFactory().createLocalVariable(
											ctExpression.getType(), "gen_o" + counterGenerateNewObject++, ctExpression
									)
							);
							return clone;
						}
				);
		return gen_o1.collect(Collectors.toList());
	}

	private List<CtMethod> useExistingObject(CtMethod method) {
		List<CtLocalVariable<?>> existingObjects = getExistingObjects(method);
		return existingObjects.stream()
				.flatMap(existingObject -> findMethodsWithTargetType(existingObject.getType()).stream()
						.map(methodToBeAdd ->
								addInvocation(method,
										methodToBeAdd,
										createLocalVarRef(existingObject),
										existingObject)
						).collect(Collectors.toList()).stream()
				).collect(Collectors.toList());
	}

	private List<CtMethod> useReturnValuesOfExistingMethodCall(CtMethod method) {
		final int[] count = new int[1];
		count[0] = 0;
		List<CtInvocation> invocations = getInvocation(method);
		final List<CtMethod> ampMethods = new ArrayList<>();
		invocations.stream()
				.filter(invocation -> !TypeUtils.isPrimitive(invocation.getType()) || !TypeUtils.isString(invocation.getType()))
				.forEach(invocation -> {
					List<CtMethod> methodsWithTargetType = findMethodsWithTargetType(invocation.getType());
					if (!methodsWithTargetType.isEmpty()) {
						CtLocalVariable localVar = method.getFactory().Code().createLocalVariable(
								invocation.getType(),
								"invoc_" + count[0]++,
								invocation);
						CtExpression<?> target = createLocalVarRef(localVar);
						CtMethod methodClone = AmplificationHelper.cloneMethodTest(method, "");
						CtStatement stmt = findInvocationIn(methodClone, invocation);
						stmt.replace(localVar);

						ampMethods.addAll(methodsWithTargetType.stream()
								.map(addMth -> addInvocation(methodClone, addMth, target, localVar))
								.collect(Collectors.toList()));
					}
				});
		return ampMethods;
	}

	private List<CtLocalVariable<?>> getExistingObjects(CtMethod method) {
		return method.getElements(new TypeFilter<CtLocalVariable<?>>(CtLocalVariable.class) {
			@Override
			public boolean matches(CtLocalVariable<?> element) {
				return element.getType() != null &&
						!element.getType().isPrimitive() &&
						element.getType().getDeclaration() != null;
			}
		});
	}

	@Override
	public CtMethod applyRandom(CtMethod method) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset(CtType testClass) {
		AmplificationHelper.reset();
		initMethods(testClass);
	}

	private CtMethod addInvocation(CtMethod<?> testMethod, CtMethod<?> methodToInvokeToAdd, CtExpression<?> target, CtStatement position) {
		final Factory factory = testMethod.getFactory();
		CtMethod methodClone = AmplificationHelper.cloneMethodTest(testMethod, "_sd");

		CtBlock body = methodClone.getElements(new TypeFilter<>(CtStatement.class))
				.stream()
				.filter(statement -> statement.equals(position))
				.findFirst()
				.get()
				.getParent(CtBlock.class);

		List<CtParameter<?>> parameters = methodToInvokeToAdd.getParameters();
		List<CtExpression<?>> arguments = new ArrayList<>(parameters.size());

		methodToInvokeToAdd.getParameters().forEach(parameter -> {
					try {
						CtLocalVariable<?> localVariable = ValueCreator.createRandomLocalVar(parameter.getType(), parameter.getSimpleName());
						body.insertBegin(localVariable);
						arguments.add(factory.createVariableRead(localVariable.getReference(), false));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
		CtExpression targetClone = target.clone();
		CtInvocation newInvocation = factory.Code().createInvocation(targetClone, methodToInvokeToAdd.getReference(), arguments);
		DSpotUtils.addComment(newInvocation, "StatementAdd: add invocation of a method", CtComment.CommentType.INLINE);
		body.insertEnd(newInvocation);
		return methodClone;
	}

	private CtStatement findInvocationIn(CtMethod method, CtStatement invocation) {
		List<CtStatement> statements = Query.getElements(method, new TypeFilter(CtStatement.class));
		return statements.stream()
				.filter(s -> s.toString().equals(invocation.toString()))
				.findFirst().orElse(null);
	}

	private CtExpression<?> createLocalVarRef(CtLocalVariable var) {
		CtLocalVariableReference varRef = var.getFactory().Code().createLocalVariableReference(var);
		CtVariableAccess varRead = var.getFactory().Code().createVariableRead(varRef, false);
		return varRead;
	}

	private List<CtMethod> findMethodsWithTargetType(CtTypeReference type) {
		if (type == null) {
			return Collections.emptyList();
		} else {
			return methods.stream()
					.filter(method ->
							method.getDeclaringType().getReference().getQualifiedName()
									.equals(type.getQualifiedName())
					)
					.filter(method -> method.getModifiers().contains(ModifierKind.PUBLIC))
					.collect(Collectors.toList());
		}
	}

	private List<CtInvocation> getInvocation(CtMethod method) {
		List<CtInvocation> statements = Query.getElements(method, new TypeFilter(CtInvocation.class));
		return statements.stream()
				.filter(invocation -> invocation.getParent() instanceof CtBlock)
				.filter(stmt -> stmt.getExecutable().getDeclaringType().getQualifiedName().startsWith(filter)) // filter on the name for amplify a specific type
				.collect(Collectors.toList());
	}

	private void initMethods(CtType testClass) {
		methods = AmplificationHelper.computeClassProvider(testClass).stream()
				.flatMap(cl -> {
					Set<CtMethod> allMethods = cl.getAllMethods();
					return allMethods.stream();
				})
				.filter(mth -> !mth.getModifiers().contains(ModifierKind.ABSTRACT))//TODO abstract
				.filter(mth -> !mth.getModifiers().contains(ModifierKind.PRIVATE))
				.filter(mth -> mth.getBody() != null)
				.filter(mth -> !mth.getBody().getStatements().isEmpty())
				.collect(Collectors.toSet());
	}
}
