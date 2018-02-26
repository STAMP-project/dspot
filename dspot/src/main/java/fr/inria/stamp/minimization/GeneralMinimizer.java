package fr.inria.stamp.minimization;

import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class GeneralMinimizer implements Minimizer {

    @Override
    public CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized) {
        inlineLocalVariable(amplifiedTestToBeMinimized);
        // TODO implement
        return amplifiedTestToBeMinimized;
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
        // we can inline all local variables that are used one time
        localVariables.stream()
                .filter(localVariable ->
                        variableReads.stream()
                                .map(CtVariableRead::getVariable)
                                .filter(variableRead ->
                                        variableRead.equals(localVariable.getReference())
                                ).count() == 1
                ).map(localVariable -> {
            variableReads.stream()
                    .filter(variableRead ->
                            variableRead.getVariable().equals(localVariable.getReference())
                    ).findFirst()
                    .get()
                    .replace(localVariable.getAssignment().clone());
            return localVariable;
        }).forEach(amplifiedTestToBeMinimized.getBody()::removeStatement);
    }

}
