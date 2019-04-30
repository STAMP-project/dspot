package eu.stamp_project.prettifier.minimization;

import eu.stamp_project.prettifier.Main;
import eu.stamp_project.prettifier.output.report.ReportJSON;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;

import static eu.stamp_project.test_framework.TestFramework.ASSERTIONS_FILTER;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class GeneralMinimizer implements Minimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralMinimizer.class);

    private List<Long> timesMinimizationInMillis = new ArrayList<>();

    @Override
    public CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized) {
        final CtMethod<?> clone = amplifiedTestToBeMinimized.clone();
        final long time = System.currentTimeMillis();
        LOGGER.info("Inlining one time used variables...");
        inlineLocalVariable(clone);
        LOGGER.info("Remove redundant assertions...");
        removeRedundantAssertions(clone);
        final long elapsedTime = System.currentTimeMillis() - time;
        this.timesMinimizationInMillis.add(elapsedTime);
        LOGGER.info("Reduce {}, {} statements to {} statements in {} ms.",
                amplifiedTestToBeMinimized.getSimpleName(),
                amplifiedTestToBeMinimized.getBody().getStatements().size(),
                clone.getBody().getStatements().size(),
                elapsedTime
        );
        return clone;
    }

    private List<Long> timesRemoveRedundantAssertionsInMillis = new ArrayList<>();

    private List<Integer> numbersOfAssertionsBefore = new ArrayList<>();

    private List<Integer> numbersOfAssertionsAfter = new ArrayList<>();

    private void removeRedundantAssertions(CtMethod<?> amplifiedTestToBeMinimized) {
        this.numbersOfAssertionsBefore.add(amplifiedTestToBeMinimized.getElements(ASSERTIONS_FILTER).size());
        final long time = System.currentTimeMillis();
        amplifiedTestToBeMinimized.getElements(new TypeFilter<>(CtBlock.class))
                .forEach(block -> {
                    final List<CtInvocation<?>> assertions =
                            block.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                                @Override
                                public boolean matches(CtInvocation<?> element) {
                                    return element.getParent(CtBlock.class).equals(block) &&
                                            TestFramework.get().isAssert(element);
                                }
                            });
                    final List<CtInvocation<?>> duplicatesAssertions = findDuplicates(assertions); // One of them might be removed
                    final List<CtStatement> statements = block.getStatements();
                    duplicatesAssertions.forEach(duplicatesAssertion -> {
                        DSpotUtils.printProgress(duplicatesAssertions.indexOf(duplicatesAssertion), duplicatesAssertions.size());
                        removeUselessDuplicateAssertions(
                                block,
                                duplicatesAssertion,
                                statements
                        );
                    });
                });
        final long elapsedTime = System.currentTimeMillis() - time;
        this.numbersOfAssertionsAfter.add(amplifiedTestToBeMinimized.getElements(ASSERTIONS_FILTER).size());
        this.timesRemoveRedundantAssertionsInMillis.add(elapsedTime);
    }

    private void removeUselessDuplicateAssertions(CtBlock<?> block,
                                                  CtInvocation<?> duplicatesAssertion,
                                                  List<CtStatement> statements) {
        final CtVariableReference variable = ((CtVariableRead<?>) duplicatesAssertion
                .filterChildren(new TypeFilter<CtVariableRead<?>>(CtVariableRead.class))
                .first())
                .getVariable();
        boolean canBeRemoved = true;
        for (int i = statements.indexOf(duplicatesAssertion) + 1;
             i < statements.lastIndexOf(duplicatesAssertion); i++) {
            if (!TestFramework.get().isAssert(statements.get(i))) {
                final CtVariableRead<?> first = statements.get(i)
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
            block.getStatements().remove(statements.lastIndexOf(duplicatesAssertion));
        }
    }

    private <T> List<T> findDuplicates(Collection<T> collection) {
        final Set<T> uniques = new HashSet<>();
        return collection.stream()
                .filter(e -> !uniques.add(e))
                .collect(Collectors.toList());
    }

    private static final class LOCAL_VARIABLE_READ_FILTER extends TypeFilter<CtVariableRead> {
        private CtLocalVariableReference localVariableReference;

        LOCAL_VARIABLE_READ_FILTER(CtLocalVariable localVariable) {
            super(CtVariableRead.class);
            this.localVariableReference = localVariable.getReference();
        }

        @Override
        public boolean matches(CtVariableRead element) {
            return localVariableReference.equals(element.getVariable());
        }
    }

    private List<Integer> numbersOfLocalVariablesBefore = new ArrayList<>();

    private List<Integer> numbersOfLocalVariablesAfter = new ArrayList<>();

    private List<Long> timesToInlineLocalVariables = new ArrayList<>();

    private void inlineLocalVariable(CtMethod<?> amplifiedTestToBeMinimized) {
        final long time = System.currentTimeMillis();
        final List<CtLocalVariable> localVariables =
                amplifiedTestToBeMinimized.getElements(new TypeFilter<>(CtLocalVariable.class));
        final int nbLocalVariables = localVariables.size();

        final List<CtVariableRead> variableReads = localVariables.stream().map(LOCAL_VARIABLE_READ_FILTER::new)
                .flatMap(filter -> amplifiedTestToBeMinimized.getElements(filter).stream())
                .collect(Collectors.toList());

        // we can inline all local variables that are used one time and that have been generated by DSpot
        final List<CtLocalVariable> oneTimeUsedLocalVariable = localVariables.stream()
                .filter(localVariable ->
                        variableReads.stream()
                                .map(CtVariableRead::getVariable)
                                .filter(variableRead -> variableRead.equals(localVariable.getReference()))
                                .count() == 1
                ).collect(Collectors.toList());
        oneTimeUsedLocalVariable.stream().map(localVariable -> {
            DSpotUtils.printProgress(oneTimeUsedLocalVariable.indexOf(localVariable), oneTimeUsedLocalVariable.size());
            variableReads.stream()
                    .filter(variableRead ->
                            variableRead.getVariable().equals(localVariable.getReference())
                    ).findFirst()
                    .get()
                    .replace(localVariable.getAssignment().clone());
            return localVariable;
        }).forEach(amplifiedTestToBeMinimized.getBody()::removeStatement);
        //TODO we can inline all local variables that are used only in assertion
        final long elapsedTime = System.currentTimeMillis() - time;
        this.numbersOfLocalVariablesBefore.add(nbLocalVariables);
        this.numbersOfLocalVariablesAfter.add(amplifiedTestToBeMinimized.getElements(new TypeFilter<>(CtLocalVariable.class)).size());
        this.timesToInlineLocalVariables.add(elapsedTime);
    }

    @Override
    public void updateReport(ReportJSON report) {
        // inline
        report.generalMinimizationJSON.inlineLocalVariables.medianNumberOfLocalVariablesBefore = Main.getMedian(this.numbersOfLocalVariablesBefore);
        report.generalMinimizationJSON.inlineLocalVariables.medianNumberOfLocalVariablesAfter = Main.getMedian(this.numbersOfLocalVariablesAfter);
        report.generalMinimizationJSON.inlineLocalVariables.medianTimeInlineLocalVariablesInMillis = Main.getMedian(this.timesToInlineLocalVariables);
        // remove redundant assertion
        report.generalMinimizationJSON.removeRedundantAssertions.medianNumberOfAssertionsBefore = Main.getMedian(this.numbersOfAssertionsBefore);
        report.generalMinimizationJSON.removeRedundantAssertions.medianNumberOfAssertionsAfter = Main.getMedian(this.numbersOfAssertionsAfter);
        report.generalMinimizationJSON.removeRedundantAssertions.medianTimeMinimizationInMillis = Main.getMedian(this.timesRemoveRedundantAssertionsInMillis);
        // time minimization
        report.generalMinimizationJSON.medianTimeMinimizationInMillis = Main.getMedian(this.timesMinimizationInMillis);
    }

}

