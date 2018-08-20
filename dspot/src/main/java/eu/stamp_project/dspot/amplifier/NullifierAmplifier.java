package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.AmplificationChecker;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/07/18
 */
public class NullifierAmplifier extends AbstractAmplifier<CtExpression<?>> {

    @Override
    public void reset(CtType<?> testClass) {
        // unused
    }

    @Override
    protected String getSuffix() {
        return "null";
    }

    @Override
    protected List<CtExpression<?>> getOriginals(CtMethod<?> testMethod) {
        return testMethod.getElements(new TypeFilter<CtExpression<?>>(CtExpression.class) {
            @Override
            public boolean matches(CtExpression<?> element) {
                if (element instanceof CtTargetedExpression || // we cannot nullify target expression (e.g. null.method())
                        "null".equals(element.toString())) {// the element is not already equals to null
                    return false;
                }
                if (element.getParent() instanceof CtInvocation) { // the element is an argument of a method call
                    final CtInvocation<?> parent = (CtInvocation<?>) element.getParent();
                    if (AmplificationChecker.isAssert(parent) || element.equals(parent.getTarget())) {
                        return false;
                    }
                    int i = 0;
                    for (; i < parent.getArguments().size(); i++) { // using a for loop with i to keep the correct index
                        if (parent.getArguments().get(i) == element) { // using == to compare the references
                            break;
                        }
                    }
                    final List<CtTypeReference<?>> parameters = parent.getExecutable().getParameters();
                    if (i >= parameters.size()) {
                        if (parameters.get(parameters.size() - 1) instanceof CtArrayTypeReference) {
                            return parameters.get(parameters.size() - 1).isPrimitive();
                        } else {
                            return false;
                        }
                    }
                    if (parameters.get(i) instanceof CtArrayTypeReference) {
                        return parameters.get(parameters.size() - 1).isPrimitive();
                    } else {
                        return !parameters
                                .get(i)
                                .isPrimitive();
                    }
                }
                if (element.getParent() instanceof CtLocalVariable && element instanceof CtLiteral<?>) {// the element is a defaultExpression
                    final CtTypedElement<?> parent = element.getParent(CtTypedElement.class);
                    return !parent.getType().isPrimitive();// the expected type is not primitive
                }
                return false;
            }
        });
    }

    @Override
    protected Set<CtExpression<?>> amplify(CtExpression<?> original, CtMethod<?> testMethod) {
        return Collections.singleton(testMethod.getFactory().createLiteral(null));
    }
}
