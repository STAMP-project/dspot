package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.Counter;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Stream;


public class TestMethodCallAdder implements Amplifier {

    public Stream<CtMethod<?>> amplify(CtMethod<?> method, int iteration) {
        if (method.getDeclaringType() != null) {
            final List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                @Override
                public boolean matches(CtInvocation<?> element) {
                    return AmplificationChecker.canBeAdded(element) &&
                            !TestFramework.get().isAssert(element);
                }
            });
            return invocations.stream().map(invocation -> apply(method, invocation));
        } else {
            return Stream.empty();
        }
    }

    @Override
    public void reset(CtType<?> testClass) {
        AmplificationHelper.reset();
    }

    private CtStatement getRightInsertionPoint(CtInvocation<?> invocation) {
        CtStatement currentPoint = invocation;
        while (! (currentPoint.getParent(CtStatement.class) instanceof CtBlock<?>)
                && ! (currentPoint instanceof CtBlock<?>)) {
            currentPoint = currentPoint.getParent(CtStatement.class);
        }
        return currentPoint;
    }

    private CtMethod<?> apply(CtMethod<?> method, CtInvocation<?> invocation) {
        final CtInvocation<?> invocationToBeInserted = invocation.clone();
        removeAllTypeCast(invocationToBeInserted);
        final CtStatement insertionPoint = this.getRightInsertionPoint(invocation);
        insertionPoint.insertBefore(invocationToBeInserted);
        final CtMethod<?> clone = CloneHelper.cloneTestMethodForAmp(method, "_add");
        AmplifierHelper.getParent(invocationToBeInserted).getStatements().remove(invocationToBeInserted);
        Counter.updateInputOf(clone, 1);
        return clone;
    }

    private void removeTypeCastIfSame(CtExpression<?> element) {
        if (!element.getTypeCasts().isEmpty() &&
                (element.getType() == null ||
                element.getType().equals(element.getTypeCasts().get(0)))) {
            element.getTypeCasts().clear();
        }
    }

    private void removeAllTypeCast(CtInvocation<?> invocation) {
        invocation.getTypeCasts().clear();
        removeTypeCastIfSame(invocation.getTarget());
        CtInvocation<?> current = invocation;
        while (current.getTarget() instanceof CtInvocation<?>) {
            current = (CtInvocation<?>) current.getTarget();
            removeTypeCastIfSame(current);
        }
        removeTypeCastIfSame(current);
    }

}
