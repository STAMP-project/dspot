package eu.stamp_project.dspot.amplifier.amplifiers;

import eu.stamp_project.testrunner.test_framework.TestFramework;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/09/17
 */
public abstract class AbstractLiteralAmplifier<T> extends AbstractAmplifier<CtExpression<T>> {

    protected CtType<?> testClassToBeAmplified;

    protected final TypeFilter<CtExpression<T>> LITERAL_TYPE_FILTER = new TypeFilter<CtExpression<T>>(CtExpression.class) {
        @Override
        public boolean matches(CtExpression<T> candidate) {
            if (! (candidate instanceof CtLiteral)) {
                return false;
            }
            CtLiteral<T> literal = (CtLiteral<T>) candidate;
            try {

                // don't keep candidates inside assertions and annotations
                Class<?> clazzOfLiteral = null;
                if ((literal.getParent() instanceof CtInvocation &&
                        TestFramework.get().isAssert((CtInvocation) literal.getParent()))
                        || isConcatenationOfLiteral(literal)
                        || literal.getParent(CtAnnotation.class) != null) {
                    return false;
                } else if (literal.getValue() == null) {
                    clazzOfLiteral = getNullClass(literal);
                } else {
                    clazzOfLiteral = literal.getValue().getClass();
                }
                return getTargetedClass().isAssignableFrom(clazzOfLiteral);
            } catch (Exception e) {

                // todo maybe need a warning ?
                return false;
            }

        }
        private boolean isConcatenationOfLiteral(CtLiteral<T> literal) {
            CtElement currentElement = literal;
            while (currentElement.getParent() instanceof CtBinaryOperator) {
                currentElement = currentElement.getParent();
            }
            return currentElement.getParent() instanceof CtInvocation &&
                    TestFramework.get().isAssert((CtInvocation) literal.getParent());
        }
    };

    protected Class getNullClass(CtExpression<T> literal){

        // getting the class of the expected parameter
        if (literal.getParent() instanceof CtInvocation<?>) {
            final CtInvocation<?> parent = (CtInvocation<?>) literal.getParent();
            return parent.getExecutable()
                    .getDeclaration()
                    .getParameters()
                    .get(parent.getArguments().indexOf(literal))
                    .getType()
                    .getActualClass();

            // getting the class of the assignee
        } else if (literal.getParent() instanceof CtAssignment) {
            return ((CtAssignment) literal.getParent())
                    .getAssigned()
                    .getType()
                    .getActualClass();

            // getting the class of the local variable
        } else if (literal.getParent() instanceof CtLocalVariable) {
            return ((CtLocalVariable) literal.getParent())
                    .getType()
                    .getActualClass();
        }
        return null;
    }

    @Override
    protected List<CtExpression<T>> getOriginals(CtMethod<?> testMethod) {
        return testMethod.getElements(LITERAL_TYPE_FILTER);
    }

    @Override
    public void reset(CtType testClass) {
        AmplificationHelper.reset();
        this.testClassToBeAmplified = testClass;
    }

    protected abstract Class<?> getTargetedClass();


}
