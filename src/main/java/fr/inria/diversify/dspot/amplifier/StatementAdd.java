package fr.inria.diversify.dspot.amplifier;


import fr.inria.diversify.dspot.value.ValueCreator;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.runner.InputProgram;
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

/**
 * User: Simon
 * Date: 18/11/16
 * Time: 10:40
 */
public class StatementAdd implements Amplifier {

	private String filter;
	private Set<CtMethod> methods;
	private Map<CtType, Boolean> hasConstructor;
	private Factory factory;
	private final int[] count = {0};

	@Deprecated //The fact that StatementAdd Amplifier need the input program is a conception issue IMHO
	public StatementAdd(InputProgram program) {
		this.factory = program.getFactory();
		this.filter = "";
		this.hasConstructor = new HashMap<>();
	}

	public StatementAdd(InputProgram program, String filter) {
		this.factory = program.getFactory();
		this.filter = filter;
		this.hasConstructor = new HashMap<>();
	}

	public StatementAdd(Factory factory, String filter) {
		this.factory = factory;
		this.filter = filter;
		this.hasConstructor = new HashMap<>();
	}

	@Override
	public List<CtMethod> apply(CtMethod method) {
		count[0] = 0;
		List<CtInvocation> invocations = getInvocation(method);
		final List<CtMethod> ampMethods = invocations.stream()
				.filter(invocation -> invocation.getExecutable().getDeclaration() != null &&
						!((CtMethod) invocation.getExecutable().getDeclaration()).getModifiers().contains(ModifierKind.STATIC))
				.flatMap(invocation ->
						findMethodsWithTargetType(invocation.getTarget().getType()).stream()
								.map(addMth -> addInvocation(method, addMth, invocation.getTarget(), invocation, AmplificationHelper.getRandom().nextBoolean()))
								.collect(Collectors.toList()).stream())
				.collect(Collectors.toList());

		// use the existing invocation to add new invocation

		// use the potential parameters to generate new invocation

		invocations.stream()
				.filter(invocation -> !TypeUtils.isPrimitive(invocation.getType()) || !TypeUtils.isString(invocation.getType()))
				.forEach(invocation -> {
					List<CtMethod> methodsWithTargetType = findMethodsWithTargetType(invocation.getType());
					if (!methodsWithTargetType.isEmpty()) {
						CtLocalVariable localVar = factory.Code().createLocalVariable(
								invocation.getType(),
								"invoc_" + count[0]++,
								invocation);
						CtExpression<?> target = createLocalVarRef(localVar);
						CtMethod methodClone = AmplificationHelper.cloneMethod(method, "");
						CtStatement stmt = findInvocationIn(methodClone, invocation);
						stmt.replace(localVar);

						ampMethods.addAll(methodsWithTargetType.stream()
								.map(addMth -> addInvocation(methodClone, addMth, target, localVar, false))
								.collect(Collectors.toList()));
					}
				});

		//  use the return value of the first generation to generate

		return ampMethods;
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

	private CtMethod addInvocation(CtMethod mth, CtMethod mthToAdd, CtExpression target, CtStatement position, boolean before) {
		CtMethod methodClone = AmplificationHelper.cloneMethod(mth, "_sd");
		CtBlock body = methodClone.getBody();

		List<CtParameter> parameters = mthToAdd.getParameters();
		List<CtExpression<?>> arguments = new ArrayList<>(parameters.size());
		for (int i = 0; i < parameters.size(); i++) {
			try {
				CtParameter parameter = parameters.get(i);
				CtLocalVariable localVariable = factory.createLocalVariable();
				localVariable.setSimpleName(parameter.getSimpleName() + "_" + count[0]++);
				localVariable.setType(parameter.getType());
				localVariable.setDefaultExpression(ValueCreator.getRandomValue(parameter.getType()));
				body.insertBegin(localVariable);
				arguments.add(factory.createVariableRead(localVariable.getReference(), false));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		CtExpression targetClone = target.clone();
		CtInvocation newInvocation = factory.Code().createInvocation(targetClone, mthToAdd.getReference(), arguments);

		CtStatement stmt = findInvocationIn(methodClone, position);
		if (before) {
			stmt.insertBefore(newInvocation);
		} else {
			stmt.insertAfter(newInvocation);
		}

		return methodClone;
	}

	private CtStatement findInvocationIn(CtMethod method, CtStatement invocation) {
		List<CtStatement> statements = Query.getElements(method, new TypeFilter(CtStatement.class));
		return statements.stream()
				.filter(s -> s.toString().equals(invocation.toString()))
				.findFirst().orElse(null);
	}

	private CtExpression<?> createLocalVarRef(CtLocalVariable var) {
		CtLocalVariableReference varRef = factory.Code().createLocalVariableReference(var);
		CtVariableAccess varRead = factory.Code().createVariableRead(varRef, false);
		return varRead;
	}

	private List<CtMethod> findMethodsWithTargetType(CtTypeReference type) {
		if (type == null) {
			return Collections.emptyList();
		} else {
			return methods.stream()
					.filter(mth -> mth.getDeclaringType().getReference().getQualifiedName().equals(type.getQualifiedName()))
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
				.filter(mth -> {
					List<CtParameter> parameters = mth.getParameters();
					return parameters.stream()
							.map(param -> param.getType())
							.allMatch(param -> TypeUtils.isPrimitive(param)
									|| TypeUtils.isString(param)
									|| TypeUtils.isPrimitiveArray(param)
									|| TypeUtils.isPrimitiveCollection(param)
									|| TypeUtils.isPrimitiveMap(param)
									|| isSerializable(param));
				})
				.collect(Collectors.toSet());
	}

	private boolean isSerializable(CtTypeReference type) {
		if (!hasConstructor.containsKey(type.getDeclaration())) {
			if (type.getDeclaration() instanceof CtClass) {
				CtClass cl = (CtClass) type.getDeclaration();
				hasConstructor.put(type.getDeclaration(),
						cl.isTopLevel() && hasConstructorCall(cl));
			} else {
				hasConstructor.put(type.getDeclaration(), false);
			}
		}
		return hasConstructor.get(type.getDeclaration());
	}

	private boolean hasConstructorCall(CtClass target) {
		CtTypeReference ref = target.getReference();
		return target.getFactory().Class().getAll(false).stream()
				.filter(type -> type.getReference().isSubtypeOf(ref))
				.filter(type -> type instanceof CtClass)
				.map(cl ->
						target.isTopLevel() && !target.getModifiers().contains(ModifierKind.ABSTRACT)
						&& (ValueCreator.getRandomValue(target.getReference()) != null)
				)
				.anyMatch(Objects::nonNull);
	}
}
