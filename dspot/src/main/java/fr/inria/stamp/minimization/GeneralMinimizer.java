package fr.inria.stamp.minimization;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class GeneralMinimizer implements Minimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralMinimizer.class);

    @Override
    public CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized) {
        final CtMethod<?> clone = amplifiedTestToBeMinimized.clone();
        final long time = System.currentTimeMillis();
        inlineLocalVariable(clone);
        removeRedundantAssertions(clone);
        LOGGER.info("Reduce {}, {} statements to {} statements in {} ms.",
                amplifiedTestToBeMinimized.getSimpleName(),
                amplifiedTestToBeMinimized.getBody().getStatements().size(),
                clone.getBody().getStatements().size(),
                System.currentTimeMillis() - time
        );
        return clone;
    }

    private void removeRedundantAssertions(CtMethod<?> amplifiedTestToBeMinimized) {
        final List<CtInvocation<?>> assertions = amplifiedTestToBeMinimized.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation<?> element) {
                return AmplificationChecker.isAssert(element);
            }
        });
        final List<CtInvocation<?>> duplicatesAssertions = findDuplicates(assertions); // One of them might be removed
        final List<CtStatement> statements = amplifiedTestToBeMinimized.getBody().getStatements();
        final CtVariableReference variable = ((CtVariableRead<?>) duplicatesAssertions.get(0)
                .filterChildren(new TypeFilter<CtVariableRead<?>>(CtVariableRead.class))
                .first())
                .getVariable();
        duplicatesAssertions.forEach(duplicatesAssertion ->
                removeUselessDuplicateAssertions(
                        amplifiedTestToBeMinimized,
                        duplicatesAssertion,
                        statements,
                        variable
                )
        );
    }

    private void removeUselessDuplicateAssertions(CtMethod<?> amplifiedTestToBeMinimized, CtInvocation<?> duplicatesAssertion, List<CtStatement> statements, CtVariableReference variable) {
        boolean canBeRemoved = true;
        for (int i = statements.indexOf(duplicatesAssertion) + 1;
             i < statements.lastIndexOf(duplicatesAssertion) - 1; i++) {
            if (!AmplificationChecker.isAssert(statements.get(i))) {
                final CtVariableRead<?> first = (CtVariableRead<?>) statements.get(i)
                        .filterChildren(new TypeFilter<CtVariableRead<?>>(CtVariableRead.class) {
                            @Override
                            public boolean matches(CtVariableRead<?> element) {
                                return element.getVariable().equals(variable);
                            }
                        }).first();
                if (first != null) {
                    canBeRemoved = false;
                    break;
                }
            }
        }
        if (canBeRemoved) {
            amplifiedTestToBeMinimized.getBody().getStatements().remove(
                    statements.lastIndexOf(duplicatesAssertion)
            );
        }
    }

    private <T> List<T> findDuplicates(Collection<T> collection) {
        final Set<T> uniques = new HashSet<>();
        return collection.stream()
                .filter(e -> !uniques.add(e))
                .collect(Collectors.toList());
    }


    private void inlineLocalVariable(CtMethod<?> amplifiedTestToBeMinimized) {
        final List<CtLocalVariable> localVariables =
                amplifiedTestToBeMinimized.getElements(new TypeFilter<>(CtLocalVariable.class));
        final List<CtVariableRead> variableReads =
                amplifiedTestToBeMinimized.getElements(new TypeFilter<CtVariableRead>(CtVariableRead.class) {
                    @Override
                    public boolean matches(CtVariableRead element) {
                        return localVariables.stream()
                                .map(CtLocalVariable::getReference)
                                .anyMatch(localVariable ->
                                        localVariable.equals(element.getVariable())
                                );
                    }
                });
        // we can inline all local variables that are used one time and that have been generated by DSpot
        localVariables.stream()
                .filter(localVariable ->
                        variableReads.stream()
                                .map(CtVariableRead::getVariable)
                                .filter(variableRead -> variableRead.equals(localVariable.getReference()))
                                .count() == 1
                ).map(localVariable -> {
            variableReads.stream()
                    .filter(variableRead ->
                            variableRead.getVariable().equals(localVariable.getReference())
                    ).findFirst()
                    .get()
                    .replace(localVariable.getAssignment().clone());
            return localVariable;
        }).forEach(amplifiedTestToBeMinimized.getBody()::removeStatement);
        //TODO we can inline all local variables that are used only in assertion
    }

}
