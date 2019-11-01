package eu.stamp_project.prettifier.minimization;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.prettifier.Main;
import eu.stamp_project.prettifier.output.report.ReportJSON;
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
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class PitMutantMinimizer implements Minimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PitMutantMinimizer.class);

    private CtType<?> testClass;

    private CtType<?> currentTestClass;

    private final List<CtMethod<?>> allTest;

    private AbstractParser parser;

    private List<Integer> numbersOfNonAssertionsBefore = new ArrayList<>();

    private List<Integer> numbersOfNonAssertionsAfter = new ArrayList<>();

    private List<Integer> numbersOfAssertionsBefore = new ArrayList<>();

    private List<Integer> numbersOfAssertionsAfter = new ArrayList<>();

    private List<Long> timesMinimizationInMillis = new ArrayList<>();

    private List<Long> timesMinimizationPitInMillis = new ArrayList<>();

    private List<Long> timesMinimizationOnStatementAfterLastAssertionInMillis = new ArrayList<>();

    private AutomaticBuilder builder;

    private String classpathClassesProject;

    private String absolutePathToProjectRoot;

    private String absolutePathToTestClasses;

    public PitMutantMinimizer(CtType<?> testClass,
                              AutomaticBuilder automaticBuilder,
                              String pathToProjectRoot,
                              String classpathProject,
                              String absolutePathToTestClasses) {
        this.testClass = testClass;
        this.parser = new PitXMLResultParser();
        this.allTest = TestFramework.getAllTest(testClass);
        this.builder = automaticBuilder;
        this.classpathClassesProject = classpathProject;
        this.absolutePathToProjectRoot = pathToProjectRoot;
        this.absolutePathToTestClasses = absolutePathToTestClasses;
    }

    @Override
    public CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized) {

        LOGGER.info("Pit Minimization of {}", amplifiedTestToBeMinimized.getSimpleName());

        // statistics before minimization
        final List<CtInvocation<?>> assertions = amplifiedTestToBeMinimized.getElements(TestFramework.ASSERTIONS_FILTER);
        final int numberOfAssertionBefore = assertions.size();
        final List<CtStatement> statements = new ArrayList<>(amplifiedTestToBeMinimized.getBody().getStatements());
        statements.removeAll(assertions);
        final int numberOfNonAssertionsBefore = statements.size();
        final int numberOfStatementBefore = amplifiedTestToBeMinimized.getBody().getStatements().size();

        // minimize
        final long time = System.currentTimeMillis();
        // compute current mutation score: must keep it
        this.currentTestClass = cloneAndRemoveAllTestsButTheGivenOne(amplifiedTestToBeMinimized);
        final List<AbstractPitResult> pitResultBeforeMinimization = printCompileAndRunPit(this.currentTestClass);

        // start the minimization
        MethodAndListOfAssertions best = new MethodAndListOfAssertions(amplifiedTestToBeMinimized, assertions);
        MethodAndListOfAssertions candidate = best;
        while (candidate != null && best.assertions.size() > 1) {
            candidate = this.explore(best.method, pitResultBeforeMinimization, best.assertions);
            if (candidate != null) {
                best = candidate;
            }
        }
        final long elapsedTime = System.currentTimeMillis() - time;
        // now remove all the statement after the last assertion    since it seems that these statement are not useful.
        final long timeMinimizationOfStatementsAfterLastAssertion = System.currentTimeMillis();
        final int indexOfLastAssertionInWholeBody = best.getIndexOfLastAssertionInWholeBody();
        while (best.method.getBody().getStatements().size() != indexOfLastAssertionInWholeBody + 1) {
            best.method.getBody().getStatements().remove(indexOfLastAssertionInWholeBody + 1);
        }
        final long elapsedTimeMinimizationOfStatementsAfterLastAssertion = System.currentTimeMillis() - timeMinimizationOfStatementsAfterLastAssertion;

        // saving statistics
        this.timesMinimizationInMillis.add(elapsedTime + elapsedTimeMinimizationOfStatementsAfterLastAssertion);
        this.timesMinimizationPitInMillis.add(elapsedTime);
        this.timesMinimizationOnStatementAfterLastAssertionInMillis.add(elapsedTimeMinimizationOfStatementsAfterLastAssertion);
        this.numbersOfNonAssertionsBefore.add(numberOfNonAssertionsBefore);
        this.numbersOfAssertionsBefore.add(numberOfAssertionBefore);
        this.numbersOfNonAssertionsAfter.add(best.getNumberOfNonAssertions());
        this.numbersOfAssertionsAfter.add(best.getNumberOfAssertion());
        LOGGER.info("Reduced {} assertions to {} in {} millis.", numberOfAssertionBefore, best.getNumberOfAssertion(), elapsedTime);
        LOGGER.info("Removed {} statements after the last remaining assertion in {} millis.",
                numberOfNonAssertionsBefore - best.getNumberOfNonAssertions(), elapsedTimeMinimizationOfStatementsAfterLastAssertion
        );
        LOGGER.info("Total reduction from {} statements to {} statements, including assertions, in {} millis.",
                numberOfStatementBefore, best.getNumberOfStatement(),
                elapsedTime + elapsedTimeMinimizationOfStatementsAfterLastAssertion
        );
        return best.method;
    }

    private CtType<?> cloneAndRemoveAllTestsButTheGivenOne(CtMethod<?> amplifiedTestToBeMinimized) {
        final CtType<?> testClone = testClass.clone();
        this.testClass.getPackage().addType(testClone);
        this.allTest.stream().filter(test -> !test.equals(amplifiedTestToBeMinimized))
                .forEach(testClone::removeMethod);
        return testClone;
    }

    @Override
    public void updateReport(ReportJSON report) {
        report.pitMinimizationJSON.medianTimeMinimizationInMillis = Main.getMedian(this.timesMinimizationInMillis);
        report.pitMinimizationJSON.medianNumberOfAssertionsBefore = Main.getMedian(this.numbersOfAssertionsBefore);
        report.pitMinimizationJSON.medianNumberOfAssertionsAfter = Main.getMedian(this.numbersOfAssertionsAfter);
        report.pitMinimizationJSON.medianNumberOfNonAssertionBefore = Main.getMedian(this.numbersOfNonAssertionsBefore);
        report.pitMinimizationJSON.medianNumberOfNonAssertionAfter = Main.getMedian(this.numbersOfNonAssertionsAfter);
        report.pitMinimizationJSON.medianTimePitMinimization = Main.getMedian(this.timesMinimizationPitInMillis);
        report.pitMinimizationJSON.medianTimeMinimizationOfStatementsAfterLastAssertionsInMillis = Main.getMedian(this.timesMinimizationOnStatementAfterLastAssertionInMillis);

    }

    private class MethodAndListOfAssertions {
        final CtMethod<?> method;
        final List<CtInvocation<?>> assertions;

        MethodAndListOfAssertions(CtMethod<?> method, List<CtInvocation<?>> assertions) {
            this.method = method;
            this.assertions = assertions;
        }

        public CtInvocation<?> getLastAssertion() {
            return this.assertions.get(this.assertions.size() - 1);
        }

        public int getIndexOfLastAssertionInWholeBody() {
            return this.method.getBody().getStatements().indexOf(this.getLastAssertion());
        }

        public int getNumberOfAssertion() {
            return this.assertions.size();
        }

        public int getNumberOfNonAssertions() {
            final List<CtStatement> statements = new ArrayList<>(this.method.getBody().getStatements());
            statements.removeAll(this.assertions);
            return statements.size();
        }

        public int getNumberOfStatement() {
            return this.method.getBody().getStatements().size();
        }
    }

    private MethodAndListOfAssertions explore(CtMethod<?> amplifiedTestToBeMinimized,
                                              List<AbstractPitResult> pitResultBeforeMinimization,
                                              List<CtInvocation<?>> assertions) {
        for (int i = assertions.size() - 1; i >= 0; i--) {
            final CtMethod<?> testMethodWithOneLessAssertion =
                    this.removeCloneAndInsert(assertions, amplifiedTestToBeMinimized, i);
            final List<AbstractPitResult> minimizedPitResult = runPit(testMethodWithOneLessAssertion, pitResultBeforeMinimization);
            if (check(pitResultBeforeMinimization, minimizedPitResult)) {
                final CtMethod<?> clone = amplifiedTestToBeMinimized.clone();
                clone.getBody().getStatements().remove(assertions.get(i));
                final ArrayList<CtInvocation<?>> copyAssertions = new ArrayList<>(assertions);
                copyAssertions.remove(assertions.get(i));
                return new MethodAndListOfAssertions(clone, copyAssertions);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    List<AbstractPitResult> printCompileAndRunPit(CtType<?> testClass) {
        DSpotUtils.printCtTypeToGivenDirectory(testClass, new File(DSpotCompiler.getPathToAmplifiedTestSrc()));
        final String classpath = this.builder
                .buildClasspath()
                + AmplificationHelper.PATH_SEPARATOR +
                this.classpathClassesProject
                + AmplificationHelper.PATH_SEPARATOR + DSpotUtils.getAbsolutePathToDSpotDependencies();
        DSpotCompiler.compile(
                DSpotCompiler.getPathToAmplifiedTestSrc(),
                classpath,
                new File(this.absolutePathToTestClasses)
        );
        this.builder.runPit(testClass);
        return parser.parseAndDelete(
                this.absolutePathToProjectRoot + this.builder.getOutputDirectoryPit()
        );
    }

    private List<AbstractPitResult> runPit(CtMethod<?> method, List<AbstractPitResult> pitResultBeforeMinimization) {
        final CtType<?> clone = this.currentTestClass.clone();
        this.currentTestClass.getPackage().addType(clone);
        clone.addMethod(method);
        final List<AbstractPitResult> resultMinimized = printCompileAndRunPit(clone);
        if (pitResultBeforeMinimization.size() != resultMinimized.size()) {
            throw new RuntimeException("Something is wrong, both mutation analysis gave different number of mutants.");
        }
        return resultMinimized;
    }

    private boolean check(List<AbstractPitResult> pitResultBeforeMinimization, List<AbstractPitResult> resultMinimized) {
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
