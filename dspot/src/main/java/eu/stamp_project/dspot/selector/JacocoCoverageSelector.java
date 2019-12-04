package eu.stamp_project.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReportImpl;
import eu.stamp_project.dspot.common.report.output.selector.coverage.json.TestCaseJSON;
import eu.stamp_project.dspot.common.report.output.selector.coverage.json.TestClassJSON;
import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.Counter;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;

import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/17
 */
public class JacocoCoverageSelector extends TakeAllSelector {

    private Map<String, Coverage> selectedToBeAmplifiedCoverageResultsMap;

    private Coverage initialCoverage;

    private List<String> pathExecuted = new ArrayList<>();

    private TestSelectorElementReport lastReport;

    public JacocoCoverageSelector(AutomaticBuilder automaticBuilder,
                                  UserInput configuration) {
        super(automaticBuilder, configuration);
    }

    @Override
    public boolean init() {
        super.init();
        this.selectedAmplifiedTest.clear();
        return true;
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(CtType<?> classTest, List<CtMethod<?>> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null) {
            this.currentClassTestToBeAmplified = classTest;
            try {
                this.initialCoverage = EntryPoint.runCoverage(
                        classpath + AmplificationHelper.PATH_SEPARATOR + targetClasses,
                        this.targetClasses,
                        this.currentClassTestToBeAmplified.getQualifiedName()
                );
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
            this.selectedToBeAmplifiedCoverageResultsMap = null;
            this.selectedAmplifiedTest.clear();
        }
        final CoveragePerTestMethod coveragePerTestMethod = computeCoverageForGivenTestMethods(testsToBeAmplified);
        final List<String> pathExecuted = new ArrayList<>();
        final List<CtMethod<?>> filteredTests = testsToBeAmplified.stream()
                .filter(ctMethod -> ctMethod != null &&
                        coveragePerTestMethod.getCoverageOf(ctMethod.getSimpleName()) != null)
                .filter(ctMethod -> {
                    final String pathByExecInstructions = coveragePerTestMethod.getCoverageOf(ctMethod.getSimpleName()).getExecutionPath();
                    if (pathExecuted.contains(pathByExecInstructions)) {
                        return false;
                    } else {
                        pathExecuted.add(pathByExecInstructions);
                        return true;
                    }
                }).collect(Collectors.toList());
        if (this.selectedToBeAmplifiedCoverageResultsMap == null) {
            final List<String> filteredMethodNames = filteredTests.stream()
                    .map(CtNamedElement::getSimpleName)
                    .collect(Collectors.toList());
            this.selectedToBeAmplifiedCoverageResultsMap = coveragePerTestMethod.getCoverageResultsMap()
                    .keySet()
                    .stream()
                    .filter(filteredMethodNames::contains)
                    .collect(Collectors.toMap(Function.identity(), coveragePerTestMethod.getCoverageResultsMap()::get));
        }
        return filteredTests;
    }

    private CoveragePerTestMethod computeCoverageForGivenTestMethods(List<CtMethod<?>> testsToBeAmplified) {
        final String[] methodNames = testsToBeAmplified.stream().map(CtNamedElement::getSimpleName).toArray(String[]::new);
        try {
            return EntryPoint.runCoveragePerTestMethods(
                    this.classpath + AmplificationHelper.PATH_SEPARATOR + this.targetClasses,
                    this.targetClasses,
                    this.currentClassTestToBeAmplified.getQualifiedName(),
                    methodNames
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
        if (amplifiedTestToBeKept.isEmpty()) {
            return amplifiedTestToBeKept;
        }
        final CoveragePerTestMethod coveragePerTestMethod = computeCoverageForGivenTestMethods(amplifiedTestToBeKept);
        final List<CtMethod<?>> methodsKept = amplifiedTestToBeKept.stream()
                .filter(ctMethod -> {
                    final String simpleNameOfFirstParent = getFirstParentThatHasBeenRun(ctMethod).getSimpleName();
                    return this.selectedToBeAmplifiedCoverageResultsMap.get(simpleNameOfFirstParent) == null ||
                            coveragePerTestMethod.getCoverageOf(ctMethod.getSimpleName()).isBetterThan(
                                    this.selectedToBeAmplifiedCoverageResultsMap.get(simpleNameOfFirstParent));
                })
                .filter(ctMethod -> {
                    final String pathByExecInstructions = coveragePerTestMethod.getCoverageOf(ctMethod.getSimpleName()).getExecutionPath();
                    if (pathExecuted.contains(pathByExecInstructions)) {
                        return false;
                    } else {
                        pathExecuted.add(pathByExecInstructions);
                        return true;
                    }
                }).collect(Collectors.toList());

        this.selectedToBeAmplifiedCoverageResultsMap.putAll(methodsKept.stream()
                .map(CtNamedElement::getSimpleName)
                .collect(
                        Collectors.toMap(Function.identity(), coveragePerTestMethod.getCoverageResultsMap()::get)
                )
        );

        this.selectedAmplifiedTest.addAll(new ArrayList<>(methodsKept));
        return methodsKept;
    }

    protected CtMethod<?> getFirstParentThatHasBeenRun(CtMethod<?> test) {
        CtMethod<?> currentParent = AmplificationHelper.getAmpTestParent(test);
        while (AmplificationHelper.getAmpTestParent(currentParent) != null) {
            if (this.selectedToBeAmplifiedCoverageResultsMap.get(currentParent.getSimpleName()) != null) {
                return currentParent;
            } else {
                currentParent = AmplificationHelper.getAmpTestParent(currentParent);
            }
        }
        return currentParent;
    }

    @Override
    public TestSelectorElementReport report() {
        if(currentClassTestToBeAmplified == null) {
            return lastReport;
        }
        // 1 textual report
        StringBuilder report = new StringBuilder()
                .append("Initial instruction coverage: ")
                .append(this.initialCoverage.getInstructionsCovered())
                .append(" / ")
                .append(this.initialCoverage.getInstructionsTotal())
                .append(AmplificationHelper.LINE_SEPARATOR)
                .append(String.format("%.2f", 100.0D * ((double) this.initialCoverage.getInstructionsCovered() /
                        (double) this.initialCoverage.getInstructionsTotal()))).append("%").append(AmplificationHelper.LINE_SEPARATOR)
                .append("Amplification results with ")
                .append(this.selectedAmplifiedTest.size())
                .append(" amplified tests.")
                .append(AmplificationHelper.LINE_SEPARATOR);

        // compute the new coverage obtained by the amplification
        final CtType<?> clone = this.currentClassTestToBeAmplified.clone();
        this.currentClassTestToBeAmplified.getPackage().addType(clone);
        this.selectedAmplifiedTest.forEach(clone::addMethod);
        try {
            FileUtils.deleteDirectory(new File(DSpotCompiler.getPathToAmplifiedTestSrc()));
        } catch (IOException ignored) {
            //ignored
        }
        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.getPathToAmplifiedTestSrc()));
        DSpotCompiler.compile(DSpotCompiler.getPathToAmplifiedTestSrc(),
                this.classpath,
                new File(this.pathToTestClasses)
        );
        try {
            final Coverage coverageResults = EntryPoint.runCoverage(
                    this.classpath,
                    this.targetClasses,
                    this.currentClassTestToBeAmplified.getQualifiedName()
            );
            report.append("Amplified instruction coverage: ")
                    .append(coverageResults.getInstructionsCovered())
                    .append(" / ")
                    .append(coverageResults.getInstructionsTotal())
                    .append(AmplificationHelper.LINE_SEPARATOR)
                    .append(String.format("%.2f", 100.0D * ((double) coverageResults.getInstructionsCovered() /
                            (double) coverageResults.getInstructionsTotal())))
                    .append("%")
                    .append(AmplificationHelper.LINE_SEPARATOR);
            lastReport = new TestSelectorElementReportImpl(report.toString(), jsonReport(coverageResults), Collections.emptyList(), "");

            return lastReport;
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            this.currentClassTestToBeAmplified = null;
        }

    }

    private TestClassJSON jsonReport(Coverage coverageResults) {
        TestClassJSON testClassJSON;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(this.outputDirectory + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "report.json");
        if (file.exists()) {
            try {
                testClassJSON = gson.fromJson(new FileReader(file), TestClassJSON.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            testClassJSON = new TestClassJSON(this.currentClassTestToBeAmplified.getQualifiedName(),
                    this.currentClassTestToBeAmplified.getMethods().size(),
                    this.initialCoverage.getInstructionsCovered(), this.initialCoverage.getInstructionsTotal(),
                    coverageResults.getInstructionsCovered(), coverageResults.getInstructionsTotal()
            );
        }
        this.selectedAmplifiedTest.forEach(ctMethod ->
                new TestCaseJSON(ctMethod.getSimpleName(),
                        Counter.getInputOfSinceOrigin(ctMethod),
                        Counter.getAssertionOfSinceOrigin(ctMethod),
                        this.selectedToBeAmplifiedCoverageResultsMap.get(ctMethod.getSimpleName()).getInstructionsCovered(),
                        this.selectedToBeAmplifiedCoverageResultsMap.get(ctMethod.getSimpleName()).getInstructionsTotal()
                )
        );
//      TODO
//        CollectorConfig.getInstance().getInformationCollector().reportSelectorInformation(testClassJSON.toString());
        return testClassJSON;
    }
}
