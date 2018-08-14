package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.dspot.amplifier.value.ValueCreator;
import eu.stamp_project.dspot.amplifier.value.ValueCreatorHelper;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.RandomHelper;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBodyHolder;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/07/18
 */
public class AmplifierHelper {

    public static CtStatementList getParent(CtInvocation<?> invocationToBeCloned) {
        CtElement parent = invocationToBeCloned;
        while (!(parent.getParent() instanceof CtStatementList)) {
            parent = parent.getParent();
        }
        return (CtStatementList) parent.getParent();
    }

    public static CtExpression<?> createLocalVarRef(CtLocalVariable<?> var) {
        CtLocalVariableReference<?> varRef = var.getFactory().Code().createLocalVariableReference(var);
        return var.getFactory().Code().createVariableRead(varRef, false);
    }

    public static List<CtMethod<?>> findMethodsWithTargetType(CtTypeReference<?> type) {
        if (type == null || type.getTypeDeclaration() == null) {
            return Collections.emptyList();
        } else {
            return type.getTypeDeclaration().getMethods().stream()
                    .filter(method -> method.getModifiers().contains(ModifierKind.PUBLIC)) // TODO checks this predicate
                    // TODO we could also access to method with default or protected modifiers
                    .filter(method -> !method.getModifiers().contains(ModifierKind.STATIC)) // TODO checks this predicate
                    // TODO we can't amplify test on full static classes with this predicate
                    .filter(method -> !method.getModifiers().contains(ModifierKind.ABSTRACT)) // TODO checks this predicate
                    // TODO maybe we would like to call of abstract method, since the abstract would be implemented
                    // TODO inherited classes. However, the semantic of the test to be amplified may be to test the abstract class
                    .filter(method -> method.getParameters()
                            .stream()
                            .map(CtParameter::getType)
                            .allMatch(ValueCreatorHelper::canGenerateAValueForType)
                    ).collect(Collectors.toList());
        }
    }

    public static CtMethod<?> addInvocation(CtMethod<?> testMethod,
                                            CtMethod<?> methodToInvokeToAdd,
                                            CtExpression<?> target,
                                            CtStatement position,
                                            String suffix) {
        final Factory factory = testMethod.getFactory();
        CtMethod methodClone = CloneHelper.cloneTestMethodForAmp(testMethod, suffix);

        CtBodyHolder parent = methodClone.getElements(new TypeFilter<>(CtStatement.class))
                .stream()
                .filter(statement -> statement.equals(position))
                .findFirst()
                .get()
                .getParent(CtBodyHolder.class);

        if (!(parent.getBody() instanceof CtBlock)) {
            parent.setBody(factory.createCtBlock(parent.getBody()));
        }

        CtBlock body = (CtBlock) parent.getBody();

        List<CtParameter<?>> parameters = methodToInvokeToAdd.getParameters();
        List<CtExpression<?>> arguments = new ArrayList<>(parameters.size());

        methodToInvokeToAdd.getParameters().forEach(parameter -> {
            try {
                final CtLocalVariable<?> localVariable;
                if (methodToInvokeToAdd.getSimpleName().equals("equals") &&
                        RandomHelper.getRandom().nextFloat() >= 0.25F) {
                    localVariable = ValueCreator.createRandomLocalVar(target.getType(), parameter.getSimpleName());
                } else {
                    localVariable = ValueCreator.createRandomLocalVar(parameter.getType(), parameter.getSimpleName());
                }
                body.insertBegin(localVariable);
                arguments.add(factory.createVariableRead(localVariable.getReference(), false));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        CtExpression targetClone = target.clone();
        CtInvocation newInvocation = factory.Code().createInvocation(targetClone, methodToInvokeToAdd.getReference(), arguments);
        //DSpotUtils.addComment(newInvocation, "MethodGenerator", CtComment.CommentType.INLINE);
        body.insertEnd(newInvocation);
        return methodClone;
    }
}
