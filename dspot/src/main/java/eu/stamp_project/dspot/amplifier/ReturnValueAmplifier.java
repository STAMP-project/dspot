package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.Counter;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.TypeUtils;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtWildcardReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/07/18
 */
@SuppressWarnings("unchecked")
public class ReturnValueAmplifier implements Amplifier {

    @Override
    public Stream<CtMethod<?>> amplify(CtMethod<?> testMethod, int iteration) {
        List<CtInvocation> invocations = getInvocations(testMethod);
        final List<CtMethod<?>> ampMethods = new ArrayList<>();
        invocations.stream()
                .filter(invocation ->
                        !(invocation.getType() instanceof CtWildcardReference) &&
                                !TypeUtils.isPrimitive(invocation.getType()) &&
                                !TypeUtils.isString(invocation.getType())
                ).forEach(invocation -> {
            List<CtMethod<?>> methodsWithTargetType = AmplifierHelper.findMethodsWithTargetType(invocation.getType());
            if (!methodsWithTargetType.isEmpty()) {
                int indexOfInvocation = getIndexOf(testMethod, invocation);
                CtLocalVariable localVar = testMethod.getFactory().Code().createLocalVariable(
                        invocation.getType(),
                        "__DSPOT_invoc_" + indexOfInvocation,
                        invocation.clone());
                CtExpression<?> target = AmplifierHelper.createLocalVarRef(localVar);
                CtMethod methodClone = CloneHelper.cloneTestMethodForAmp(testMethod, ""); // no need to suffix here, since it will be recloned after that
                replaceInvocationByLocalVariable(
                        methodClone.getElements(new TypeFilter<>(CtStatement.class)).get(indexOfInvocation),
                        localVar
                );
                DSpotUtils.addComment(localVar, "StatementAdd: generate variable from return value", CtComment.CommentType.INLINE);
                ampMethods.addAll(methodsWithTargetType.stream()
                        .map(addMth -> AmplifierHelper.addInvocation(methodClone, addMth, target, localVar, "_rv"))
                        .collect(Collectors.toList()));
                Counter.updateInputOf(methodClone, 1);
            }
        });
        return ampMethods.stream();
    }

    private void replaceInvocationByLocalVariable(CtStatement invocationToBeReplaced, CtLocalVariable localVariable) {
        if (invocationToBeReplaced.getParent() instanceof CtBlock) {
            invocationToBeReplaced.replace(localVariable);
        } else {
            CtElement parent = invocationToBeReplaced.getParent();
            while (!(parent.getParent() instanceof CtBlock)) {
                parent = invocationToBeReplaced.getParent();
            }
            ((CtStatement) parent).insertBefore(localVariable);
            invocationToBeReplaced.replace(localVariable.getReference());
        }
    }

    private int getIndexOf(CtMethod originalMethod, CtInvocation originalInvocation) {
        final List<CtStatement> statements = originalMethod.getElements(new TypeFilter<>(CtStatement.class));
        int i;
        for (i = 0; i < statements.size(); i++) {
            if (statements.get(i) == originalInvocation) {
                return i;
            }
        }
        throw new RuntimeException("Could not find the statement: " +
                originalInvocation.toString() +
                " in " +
                originalMethod.getBody().toString());
    }

    // return all invocations inside the given method
    private List<CtInvocation> getInvocations(CtMethod<?> method) {
        List<CtInvocation> statements = Query.getElements(method, new TypeFilter(CtInvocation.class));
        return statements.stream()
                .filter(invocation -> invocation.getParent() instanceof CtBlock)
                //.filter(stmt -> stmt.getExecutable().getDeclaringType().getQualifiedName().startsWith(filter)) // filter on the name for amplify a specific type
                .collect(Collectors.toList());
    }

    @Override
    public void reset(CtType<?> testClass) {

    }
}
