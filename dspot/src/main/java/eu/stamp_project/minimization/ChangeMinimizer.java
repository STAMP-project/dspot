package eu.stamp_project.minimization;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.runner.test.Failure;
import eu.stamp_project.testrunner.runner.test.TestListener;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.sosiefier.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/03/18
 */
public class ChangeMinimizer extends GeneralMinimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeMinimizer.class);

    private CtType<?> testClass;

    private InputConfiguration configuration;

    private InputConfiguration configurationOfModifiedVersion;

    private Map<CtMethod<?>, Failure> failurePerAmplifiedTest;

    public ChangeMinimizer(CtType<?> testClass,
                           InputConfiguration configuration,
                           InputConfiguration configurationOfModifiedVersion,
                           Map<CtMethod<?>, Failure> failurePerAmplifiedTest) {
        this.configurationOfModifiedVersion = configurationOfModifiedVersion;
        this.testClass = testClass;
        this.configuration = configuration;
        this.failurePerAmplifiedTest = failurePerAmplifiedTest;
    }

    @Override
    public CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized) {
        final CtMethod<?> generalMinimize = super.minimize(amplifiedTestToBeMinimized);
        final CtMethod<?> changeMinimize = generalMinimize.clone();
        final long time = System.currentTimeMillis();
        final Failure failureToKeep = this.failurePerAmplifiedTest.get(amplifiedTestToBeMinimized);
        final List<CtInvocation> assertions = changeMinimize.filterChildren(AmplificationHelper.ASSERTIONS_FILTER).list();
        LOGGER.info("Minimizing {} assertions.", assertions.size());
        assertions.forEach(invocation -> {
                    DSpotUtils.printProgress(assertions.indexOf(invocation), assertions.size());
                    tryToRemoveAssertion(changeMinimize,
                            invocation,
                            failureToKeep
                    );
                }
        );
        LOGGER.info("Reduce {}, {} statements to {} statements in {} ms.",
                amplifiedTestToBeMinimized.getSimpleName(),
                generalMinimize.getBody().getStatements().size(),
                changeMinimize.getBody().getStatements().size(),
                System.currentTimeMillis() - time
        );

        // now that we reduce the amplified test, we must update the stack trace
        updateStackTrace(amplifiedTestToBeMinimized, changeMinimize);

        return changeMinimize;
    }

    private void updateStackTrace(CtMethod<?> amplifiedTestToBeMinimized, CtMethod<?> changeMinimize) {
        CtType<?> clone = this.testClass.clone();
        // must compile
        if (!printAndCompile(clone, changeMinimize)) {
            throw new RuntimeException("The minimizer created an uncompilable test method.");
        }
        // must have (the same?) failure
        try {
            final TestListener result = EntryPoint.runTests(
                    this.configuration.getFullClassPathWithExtraDependencies(),
                    clone.getQualifiedName(),
                    changeMinimize.getSimpleName());
            final Failure failure = result.getFailingTests().get(0);
            failurePerAmplifiedTest.remove(amplifiedTestToBeMinimized);
            failurePerAmplifiedTest.put(changeMinimize, failure);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void tryToRemoveAssertion(CtMethod<?> amplifiedTestToBeMinimized,
                                      CtInvocation<?> invocation,
                                      Failure failureToKeep) {
        final CtMethod<?> clone = amplifiedTestToBeMinimized.clone();
        clone.getBody().removeStatement(invocation);
        if (checkIfMinimizationIsOk(clone, failureToKeep)) {
            amplifiedTestToBeMinimized.getBody().removeStatement(invocation);
        }
    }

    private boolean checkIfMinimizationIsOk(CtMethod<?> amplifiedTestToBeMinimized, Failure failure) {
        CtType<?> clone = this.testClass.clone();
        // must compile
        if (!printAndCompile(clone, amplifiedTestToBeMinimized)) {
            return false;
        }
        try {
            final TestListener result = EntryPoint.runTests(
                    this.configurationOfModifiedVersion.getDependencies() +
                            AmplificationHelper.PATH_SEPARATOR +
                            DSpotUtils.PATH_TO_EXTRA_DEPENDENCIES_TO_DSPOT_CLASSES +
                            AmplificationHelper.PATH_SEPARATOR +
                            this.configuration.getAbsolutePathToTestClasses() +
                            AmplificationHelper.PATH_SEPARATOR +
                            this.configurationOfModifiedVersion.getAbsolutePathToClasses(),
                    clone.getQualifiedName(),
                    amplifiedTestToBeMinimized.getSimpleName());

            final List<Failure> failingTests = result.getFailingTests();
            return failingTests.contains(failure);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean printAndCompile(CtType<?> clone, CtMethod<?> amplifiedTestToBeMinimized) {
        clone.setParent(this.testClass.getParent());
        this.testClass.getMethods().stream()
                .filter(AmplificationChecker::isTest)
                .forEach(clone::removeMethod);
        clone.addMethod(amplifiedTestToBeMinimized);
        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.pathToTmpTestSources));
        return DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources,
                this.configuration.getFullClassPathWithExtraDependencies(),
                new File(this.configuration.getAbsolutePathToTestClasses()));
    }
}
