package fr.inria.diversify.dspot.amplifier;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/09/17
 */
public abstract class AbstractLiteralAmplifier<T> implements Amplifier {

    protected CtType<?> testClassToBeAmplified;

    private final TypeFilter<CtLiteral<T>> LITERAL_TYPE_FILTER = new TypeFilter<CtLiteral<T>>(CtLiteral.class) {
        @Override
        public boolean matches(CtLiteral<T> literal) {
            try {
                Class<?> clazzOfLiteral = null;
                if ((literal.getParent() instanceof CtInvocation &&
                        AmplificationChecker.isAssert((CtInvocation) literal.getParent()))
                        || literal.getParent(CtAnnotation.class) != null) {
                    return false;
                } else if (literal.getValue() == null) {
                    if (literal.getParent() instanceof CtInvocation<?>) {
                        final CtInvocation<?> parent = (CtInvocation<?>) literal.getParent();
                        final CtExpression<?> ctExpression = parent
                                .getArguments()
                                .stream()
                                .filter(parameter -> parameter.equals(literal))
                                .findFirst()
                                .get();
                        clazzOfLiteral = parent.getExecutable()
                                .getDeclaration()
                                .getParameters()
                                .get(parent
                                        .getArguments()
                                        .indexOf(ctExpression)
                                ).getType()
                                .getActualClass(); // getting the class of the expected parameter
                    } else if (literal.getParent() instanceof CtAssignment) {
                        clazzOfLiteral = ((CtAssignment) literal.getParent())
                                .getAssigned()
                                .getType()
                                .getActualClass(); // getting the class of the assignee
                    } else if (literal.getParent() instanceof CtLocalVariable) {
                        clazzOfLiteral = ((CtLocalVariable) literal.getParent())
                                .getType()
                                .getActualClass(); // getting the class of the local variable
                    }
                } else {
                    clazzOfLiteral = literal.getValue().getClass();
                }
                return getTargetedClass().isAssignableFrom(clazzOfLiteral);
            } catch (Exception e) {
                // maybe need a warning ?
                return false;
            }
        }
    };

    @Override
    public List<CtMethod> apply(CtMethod testMethod) {
        List<CtLiteral<T>> literals = testMethod.getElements(LITERAL_TYPE_FILTER);
        if (literals.isEmpty()) {
            return Collections.emptyList();
        }
        return literals.stream()
                .flatMap(literal -> this.amplify(literal).stream()
                        .map(newValue -> {
                                    final T originalValue = literal.getValue();
                                    literal.setValue(newValue);
                                    CtMethod clone = AmplificationHelper.cloneTestMethodForAmp(testMethod, getSuffix());
                                    literal.setValue(originalValue);
                                    return clone;
                                })
                ).collect(Collectors.toList());
    }

    @Override
    public void reset(CtType testClass) {
        AmplificationHelper.reset();
        this.testClassToBeAmplified = testClass;
    }

    protected abstract Set<T> amplify(CtLiteral<T> existingLiteral);

    protected abstract String getSuffix();

    protected abstract Class<?> getTargetedClass();

}
