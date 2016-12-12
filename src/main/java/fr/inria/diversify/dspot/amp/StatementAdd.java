package fr.inria.diversify.dspot.amp;


import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.value.Value;
import fr.inria.diversify.dspot.value.ValueFactory;
import fr.inria.diversify.log.branch.Coverage;
import fr.inria.diversify.utils.CtTypeUtils;
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
    protected String filter;
    protected Set<CtMethod> methods;
    protected Map<CtType, Boolean> hasConstructor;
    protected ValueFactory valueFactory;
    protected Factory factory;
    protected final int[] count = {0};

    public StatementAdd(Factory factory, ValueFactory valueFactory, String filter) {
        this.valueFactory = valueFactory;
        this.factory = factory;
        this.filter = filter;
        this.hasConstructor = new HashMap<>();

    }

    @Override
    public List<CtMethod> apply(CtMethod method) {
        count[0] = 0;
        List<CtInvocation> invocations = getInvocation(method);
        final List<CtMethod> ampMethods = invocations.stream()
                .filter(invocation -> !((CtMethod) invocation.getExecutable().getDeclaration()).getModifiers().contains(ModifierKind.STATIC))
                .flatMap(invocation ->
                        findMethodsWithTargetType(invocation.getTarget().getType()).stream()
                                .map(addMth -> addInvocation(method, addMth, invocation.getTarget(), invocation, AmplificationHelper.getRandom().nextBoolean()))
                                .collect(Collectors.toList()).stream())
                .collect(Collectors.toList());

        // use the existing invocation to add new invocation

        // use the potential parameters to generate new invocation

        invocations.stream()
                .filter(invocation -> !CtTypeUtils.isPrimitive(invocation.getType()) || !CtTypeUtils.isString(invocation.getType()))
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

    private CtMethod addInvocation(CtMethod mth, CtMethod mthToAdd, CtExpression target, CtStatement position, boolean before) {
        CtMethod methodClone = AmplificationHelper.cloneMethod(mth, "_sd");
        CtBlock body = methodClone.getBody();

        List<CtParameter> parameters = mthToAdd.getParameters();
        List<CtExpression<?>> arg = new ArrayList<>(parameters.size());
        for(int i = 0; i < parameters.size(); i++) {
            try {
                CtParameter parameter = parameters.get(i);
                Value value = valueFactory.getValueType(parameter.getType()).getRandomValue(true);
                CtLocalVariable localVar = factory.Code().createLocalVariable(
                        generateStaticType(parameter.getType(),value.getDynamicType()),
                        parameter.getSimpleName()+ "_" + count[0]++,
                        null);
                body.getStatements().add(0, localVar);
                localVar.setParent(body);
                arg.add(createLocalVarRef(localVar));
                value.initLocalVar(body, localVar);
            } catch (Exception e) {
//                e.printStackTrace();
//                Log.debug("");
            }
        }

        CtExpression targetClone = factory.Core().clone(target);
        CtInvocation newInvocation = factory.Code().createInvocation(targetClone, mthToAdd.getReference(), arg);

        CtStatement stmt = findInvocationIn(methodClone, position);
        if(before) {
            stmt.insertBefore(newInvocation);
        } else {
            stmt.insertAfter(newInvocation);
        }

        return methodClone;
    }

    protected CtStatement findInvocationIn(CtMethod method, CtStatement invocation) {
        List<CtStatement> statements = Query.getElements(method, new TypeFilter(CtStatement.class));
        return statements.stream()
                .filter(s -> s.toString().equals(invocation.toString()))
                .findFirst().orElse(null);
    }

    protected CtExpression<?> createLocalVarRef(CtLocalVariable var) {
        CtLocalVariableReference varRef = factory.Code().createLocalVariableReference(var);
        CtVariableAccess varRead = factory.Code().createVariableRead(varRef, false);

        return varRead;
    }

    protected CtTypeReference generateStaticType(CtTypeReference parameterType, String dynamicTypeName) {
        CtTypeReference type = factory.Core().clone(parameterType);
        type.getActualTypeArguments().clear();

        if((dynamicTypeName.contains("<") || dynamicTypeName.contains(">"))
                && !(dynamicTypeName.contains("<null") || dynamicTypeName.contains("null>"))) {

            String[] genericTypes = dynamicTypeName.substring(dynamicTypeName.indexOf("<") + 1, dynamicTypeName.length() - 1).split(", ");
            Arrays.stream(genericTypes)
                    .forEach(genericType -> type.getActualTypeArguments().add(factory.Type().createReference(genericType)));
        }
        return type;
    }

    @Override
    public CtMethod applyRandom(CtMethod method) {
        return null;
    }

    public void reset(Coverage coverage, CtType testClass) {
        AmplificationHelper.reset();
        initMethods(testClass);
    }

    protected List<CtMethod> findMethodsWithTargetType(CtTypeReference type) {
        if(type == null) {
            return new ArrayList<>(0);
        } else {
            return methods.stream()
                    .filter(mth -> mth.getDeclaringType().getReference().getQualifiedName().equals(type.getQualifiedName()))
                    .collect(Collectors.toList());
        }
    }

    protected  List<CtInvocation> getInvocation(CtMethod method) {
        List<CtInvocation> statements = Query.getElements(method, new TypeFilter(CtInvocation.class));
        return statements.stream()
                .filter(invocation -> invocation.getParent() instanceof CtBlock)
                .filter(stmt -> stmt.getExecutable().getDeclaringType().getQualifiedName().startsWith(filter)) // filter on the name for amplify a specific type
                .collect(Collectors.toList());
    }

    protected void initMethods(CtType testClass) {
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
                            .allMatch(param -> CtTypeUtils.isPrimitive(param)
                                    || CtTypeUtils.isString(param)
                                    || CtTypeUtils.isPrimitiveArray(param)
                                    || CtTypeUtils.isPrimitiveCollection(param)
                                    || CtTypeUtils.isPrimitiveMap(param)
                                    || isSerializable(param));
                })
                .collect(Collectors.toSet());
    }

    protected boolean isSerializable(CtTypeReference type) {
        if(!hasConstructor.containsKey(type.getDeclaration())) {
            if(type.getDeclaration() instanceof CtClass) {
                CtClass cl = (CtClass) type.getDeclaration();
                hasConstructor.put(type.getDeclaration(),
                        cl.isTopLevel() && valueFactory.hasConstructorCall(cl, true));
            } else {
                hasConstructor.put(type.getDeclaration(), false);
            }
        }
        return hasConstructor.get(type.getDeclaration());
    }
}
