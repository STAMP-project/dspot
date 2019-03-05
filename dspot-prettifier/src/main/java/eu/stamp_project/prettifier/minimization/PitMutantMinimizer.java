package eu.stamp_project.prettifier.minimization;

import eu.stamp_project.minimization.GeneralMinimizer;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.pit.AbstractParser;
import eu.stamp_project.utils.pit.AbstractPitResult;
import eu.stamp_project.utils.pit.PitXMLResultParser;
import eu.stamp_project.utils.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class PitMutantMinimizer extends GeneralMinimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(eu.stamp_project.minimization.PitMutantMinimizer.class);

    private CtType<?> testClass;

    final List<CtMethod<?>> allTest;

    private AbstractParser parser;

    public PitMutantMinimizer(CtType<?> testClass) {
        this.testClass = testClass;
        this.parser = new PitXMLResultParser();
        this.allTest = TestFramework.getAllTest(testClass);

    }

    @Override
    public CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized) {
        final CtType<?> testClone = testClass.clone();
        this.testClass.getPackage().addType(testClone);
        allTest.stream().filter(test -> !test.equals(amplifiedTestToBeMinimized))
                .forEach(testClone::removeMethod);
        final List<AbstractPitResult> pitResultBeforeMinimization = printCompileAndRunPit(testClass);
        final List<CtInvocation<?>> assertions = amplifiedTestToBeMinimized.getElements(TestFramework.ASSERTIONS_FILTER);
        MethodAndListOfAssertions best = new MethodAndListOfAssertions(amplifiedTestToBeMinimized, assertions);
        List<MethodAndListOfAssertions> candidates = Collections.singletonList(best);
        while (!candidates.isEmpty()) {
            final int currentSize = best.assertions.size();
            candidates = candidates.stream()
                    .flatMap(candidate ->
                            this.explore(candidate.method, pitResultBeforeMinimization, candidate.assertions).stream()
                    ).filter(methodAndListOfAssertions ->
                            methodAndListOfAssertions.assertions.size() < currentSize
                    ).collect(Collectors.toList());
            if (!candidates.isEmpty()) {
                best = candidates.get(0);
            }
        }
        return best.method;
    }

    private class MethodAndListOfAssertions {
        final CtMethod<?> method;
        final List<CtInvocation<?>> assertions;

        MethodAndListOfAssertions(CtMethod<?> method, List<CtInvocation<?>> assertions) {
            this.method = method;
            this.assertions = assertions;
        }
    }

    private List<MethodAndListOfAssertions> explore(CtMethod<?> amplifiedTestToBeMinimized,
                                                    List<AbstractPitResult> pitResultBeforeMinimization,
                                                    List<CtInvocation<?>> assertions) {
        List<MethodAndListOfAssertions> clonesWithOneAssertionLess = new ArrayList<>();
        for (int i = 0; i < assertions.size(); i++) {
            final CtMethod<?> testMethodWithOneLessAssertion =
                    this.removeCloneAndInsert(assertions, amplifiedTestToBeMinimized, i);
            if (runPitAndCheck(testMethodWithOneLessAssertion, pitResultBeforeMinimization)) {
                final CtMethod<?> clone = amplifiedTestToBeMinimized.clone();
                clone.getBody().getStatements().remove(assertions.get(i));
                final ArrayList<CtInvocation<?>> copyAssertions = new ArrayList<>(assertions);
                copyAssertions.remove(assertions.get(i));
                clonesWithOneAssertionLess.add(new MethodAndListOfAssertions(clone, copyAssertions));
            }
        }
        return clonesWithOneAssertionLess;
    }

    @SuppressWarnings("unchecked")
    List<AbstractPitResult> printCompileAndRunPit(CtType<?> testClass) {
        DSpotUtils.printCtTypeToGivenDirectory(testClass, new File(DSpotCompiler.getPathToAmplifiedTestSrc()));
        final String classpath = InputConfiguration.get().getBuilder()
                .buildClasspath()
                + AmplificationHelper.PATH_SEPARATOR +
                InputConfiguration.get().getClasspathClassesProject()
                + AmplificationHelper.PATH_SEPARATOR + DSpotUtils.getAbsolutePathToDSpotDependencies();
        DSpotCompiler.compile(InputConfiguration.get(),
                DSpotCompiler.getPathToAmplifiedTestSrc(),
                classpath,
                new File(InputConfiguration.get().getAbsolutePathToTestClasses())
        );
        InputConfiguration.get().getBuilder().runPit(testClass);
        return parser.parseAndDelete(
                InputConfiguration.get().getAbsolutePathToProjectRoot() +
                        InputConfiguration.get().getBuilder().getOutputDirectoryPit()
        );
    }

    private boolean runPitAndCheck(CtMethod<?> method, List<AbstractPitResult> pitResultBeforeMinimization) {
        final CtType<?> clone = this.testClass.clone();
        this.testClass.getPackage().addType(clone);
        clone.addMethod(method);
        final List<AbstractPitResult> resultMinimized = printCompileAndRunPit(clone);
        if (pitResultBeforeMinimization.size() != resultMinimized.size()) {
            throw new RuntimeException("Something is wrong, both mutation analysis gave different number of mutants.");
        }
        for (int i = 0; i < pitResultBeforeMinimization.size(); i++) {
            final AbstractPitResult before = pitResultBeforeMinimization.get(i);
            final AbstractPitResult after = resultMinimized.get(i);
            if (!before.equals(after) ||
                    (before.getStateOfMutant() == AbstractPitResult.State.KILLED &&
                            before.getStateOfMutant() != after.getStateOfMutant())) {
                return false;
            }
        }
        return true;
    }

    CtMethod<?> removeCloneAndInsert(final List<CtInvocation<?>> assertions, CtMethod<?> amplifiedTestToBeMinimized, int indexOfAssertion) {
        final int index = amplifiedTestToBeMinimized.getBody().getStatements().indexOf(assertions.get(indexOfAssertion));
        amplifiedTestToBeMinimized.getBody().removeStatement(assertions.get(indexOfAssertion));
        final CtMethod<?> clone = amplifiedTestToBeMinimized.clone();
        amplifiedTestToBeMinimized.getBody().addStatement(index, assertions.get(indexOfAssertion).clone());
        return clone;
    }
}
