package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/09/17
 */
public abstract class AbstractLiteralAmplifier<T> implements Amplifier {

    private static final String METADATA_KEY = "amplified";

    protected CtType<?> testClassToBeAmplified;

    protected final TypeFilter<CtLiteral<T>> LITERAL_TYPE_FILTER = new TypeFilter<CtLiteral<T>>(CtLiteral.class) {
        @Override
        public boolean matches(CtLiteral<T> literal) {
            try {
                if (literal.getMetadata(METADATA_KEY) != null && (boolean) literal.getMetadata(METADATA_KEY)) {
                    return false;
                }
                Class<?> clazzOfLiteral = null;
                if ((literal.getParent() instanceof CtInvocation &&
                        AmplificationChecker.isAssert((CtInvocation) literal.getParent()))
                        || isConcatenationOfLiteral(literal)
                        || literal.getParent(CtAnnotation.class) != null) {
                    return false;
                } else if (literal.getValue() == null) {
                    if (literal.getParent() instanceof CtInvocation<?>) {
                        final CtInvocation<?> parent = (CtInvocation<?>) literal.getParent();
                        clazzOfLiteral = parent.getExecutable()
                                .getDeclaration()
                                .getParameters()
                                .get(parent.getArguments().indexOf(literal))
                                .getType()
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
        private boolean isConcatenationOfLiteral(CtLiteral<T> literal) {
            CtElement currentElement = literal;
            while (currentElement.getParent() instanceof CtBinaryOperator) {
                currentElement = currentElement.getParent();
            }
            return currentElement.getParent() instanceof CtInvocation &&
                    AmplificationChecker.isAssert((CtInvocation) currentElement.getParent());
        }
    };

    @Override
    public Stream<CtMethod<?>> apply(final CtMethod<?> testMethod) {
        List<CtLiteral<T>> literals = testMethod.getElements(LITERAL_TYPE_FILTER);
        if (literals.isEmpty()) {
            return Stream.empty();
        }
        // we now reduce the literals list, see https://github.com/STAMP-project/dspot/issues/454
        final Integer maxIndex = literals.stream()
                .filter(this::hasBeenAmplified)
                .map(literals::indexOf)
                .max(Integer::compareTo)
                .orElse(-1);
        if (maxIndex > -1 && maxIndex <= literals.size()) {
            literals = literals.subList(maxIndex + 1, literals.size());
        }
        return literals.stream()
                .filter(literal -> !this.hasBeenAmplified(literal))
                // we now filter here, in order to keep marked elements, to reduce the list
                .flatMap(literal -> this.amplify(literal).stream()
                        .map(newValue -> replace(literal, newValue, testMethod))
                );
    }

    protected CtMethod<?> replace(CtLiteral<T> oldLiteral, T newValue, CtMethod<?> testMethod) {
        final T originalValue = oldLiteral.getValue();
        oldLiteral.setValue(newValue);
        oldLiteral.putMetadata(METADATA_KEY, true);
        oldLiteral.setDocComment(METADATA_KEY); // here, we use the DocComment since the Metadata are not cloned
        CtMethod<?> clone = AmplificationHelper.cloneTestMethodForAmp(testMethod, getSuffix());
        oldLiteral.setValue(originalValue);
        oldLiteral.setDocComment("");
        oldLiteral.putMetadata(METADATA_KEY, false);
        return clone;
    }


    // checks rather than a literal has been amplified
    private boolean hasBeenAmplified(CtLiteral<T> literal) {
        return (literal.getMetadata(METADATA_KEY) != null &&
                (boolean) literal.getMetadata(METADATA_KEY)) ||
                (literal.getDocComment() != null &&
                literal.getDocComment().equals(METADATA_KEY));
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
