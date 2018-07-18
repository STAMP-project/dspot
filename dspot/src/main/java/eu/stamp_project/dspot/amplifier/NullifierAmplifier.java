package eu.stamp_project.dspot.amplifier;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
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
                return (element.getParent() instanceof CtLocalVariable || // the element is a defaultExpression
                        element.getParent() instanceof CtInvocation)  // the element is an argument of a method call
                        &&
                        !"null".equals(element.toString()); // the element is not already equals to null
            }
        });
    }

    @Override
    protected Set<CtExpression<?>> amplify(CtExpression<?> original, CtMethod<?> testMethod) {
        return Collections.singleton(testMethod.getFactory().createLiteral(null));
    }
}
