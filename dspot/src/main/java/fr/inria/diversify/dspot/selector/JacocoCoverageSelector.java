package fr.inria.diversify.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp.project.testrunner.EntryPoint;
import eu.stamp.project.testrunner.runner.coverage.Coverage;
import eu.stamp.project.testrunner.runner.coverage.CoveragePerTestMethod;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.selector.json.coverage.TestCaseJSON;
import fr.inria.diversify.dspot.selector.json.coverage.TestClassJSON;
import fr.inria.diversify.utils.Counter;
import fr.inria.diversify.utils.compilation.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

    @Override
    public void init(InputConfiguration configuration) {
        super.init(configuration);
        this.selectedAmplifiedTest.clear();
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(List<CtMethod<?>> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
            this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();
            final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(this.configuration).buildClasspath(this.program.getProgramDir());
            final String targetClasses = this.program.getProgramDir() + (this.program.getProgramDir().endsWith("/") ? "" : "/") + this.program.getClassesDir() +
                    AmplificationHelper.PATH_SEPARATOR +
                    this.program.getProgramDir() + (this.program.getProgramDir().endsWith("/") ? "" : "/") + this.program.getTestClassesDir();
            try {
                initialCoverage = EntryPoint.runCoverageOnTestClasses(
                        classpath + AmplificationHelper.PATH_SEPARATOR + targetClasses,
                        targetClasses,
                        this.currentClassTestToBeAmplified.getQualifiedName()
                );
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
            this.selectedToBeAmplifiedCoverageResultsMap = null;
            this.selectedAmplifiedTest.clear();
        }
        final CoveragePerTestMethod coveragePerTestMethod = computeCoverageForGivenTestMethdods(testsToBeAmplified);
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

    private CoveragePerTestMethod computeCoverageForGivenTestMethdods(List<CtMethod<?>> testsToBeAmplified) {
        final String[] methodNames = testsToBeAmplified.stream().map(CtNamedElement::getSimpleName).toArray(String[]::new);
        final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(this.configuration).buildClasspath(this.program.getProgramDir());
        final String targetClasses = this.program.getProgramDir() + (this.program.getProgramDir().endsWith("/") ? "" : "/") + this.program.getClassesDir() +
                AmplificationHelper.PATH_SEPARATOR +
                this.program.getProgramDir() + (this.program.getProgramDir().endsWith("/") ? "" : "/") + this.program.getTestClassesDir();
        try {
            return EntryPoint.runCoveragePerTestMethods(
                    classpath + AmplificationHelper.PATH_SEPARATOR + targetClasses,
                    targetClasses,
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
        final CoveragePerTestMethod coveragePerTestMethod = computeCoverageForGivenTestMethdods(amplifiedTestToBeKept);
        final List<String> pathExecuted = new ArrayList<>();
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
                })
                .collect(Collectors.toList());

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
    public void report() {
        final String nl = System.getProperty("line.separator");
        StringBuilder report = new StringBuilder();
        report.append(nl).append("======= REPORT =======").append(nl);
        report.append("Initial instruction coverage: ").append(this.initialCoverage.getInstructionsCovered())
                .append(" / ").append(this.initialCoverage.getInstructionsTotal()).append(nl)
                .append(String.format("%.2f", 100.0D * ((double) this.initialCoverage.getInstructionsCovered() /
                        (double) this.initialCoverage.getInstructionsTotal()))).append("%").append(nl);
        report.append("Amplification results with ").append(this.selectedAmplifiedTest.size())
                .append(" amplified tests.").append(nl);

        final CtType<?> clone = this.currentClassTestToBeAmplified.clone();
        this.currentClassTestToBeAmplified.getPackage().addType(clone);
        this.selectedAmplifiedTest.forEach(clone::addMethod);
        try {
            FileUtils.deleteDirectory(new File(DSpotCompiler.pathToTmpTestSources));
        } catch (IOException ignored) {
            //ignored
        }
        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.pathToTmpTestSources));
        this.currentClassTestToBeAmplified.getPackage().removeType(clone);

        final String fileSeparator = System.getProperty("file.separator");
        final String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration)
                .buildClasspath(this.program.getProgramDir())
                + System.getProperty("path.separator") +
                this.program.getProgramDir() + fileSeparator + this.program.getClassesDir()
                + System.getProperty("path.separator") +
                this.program.getProgramDir() + fileSeparator + this.program.getTestClassesDir();

        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources, classpath,
                new File(this.program.getProgramDir() + fileSeparator + this.program.getTestClassesDir()));

        final String targetClasses = this.program.getProgramDir() + (this.program.getProgramDir().endsWith("/") ? "" : "/") + this.program.getClassesDir() +
                AmplificationHelper.PATH_SEPARATOR +
                this.program.getProgramDir() + (this.program.getProgramDir().endsWith("/") ? "" : "/") + this.program.getTestClassesDir();
        try {
            final Coverage coverageResults = EntryPoint.runCoverageOnTestClasses(
                    classpath,
                    targetClasses,
                    this.currentClassTestToBeAmplified.getQualifiedName()
            );
            report.append("Amplified instruction coverage: ").append(coverageResults.getInstructionsCovered())
                    .append(" / ").append(coverageResults.getInstructionsTotal()).append(nl)
                    .append(String.format("%.2f", 100.0D * ((double) coverageResults.getInstructionsCovered() /
                            (double) coverageResults.getInstructionsTotal()))).append("%").append(nl);
            System.out.println(report.toString());
            File reportDir = new File(this.configuration.getOutputDirectory());
            if (!reportDir.exists()) {
                reportDir.mkdir();
            }
            try (FileWriter writer = new FileWriter(this.configuration.getOutputDirectory() +
                    fileSeparator + this.currentClassTestToBeAmplified.getQualifiedName()
                    + "_jacoco_instr_coverage_report.txt", false)) {
                writer.write(report.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            jsonReport(coverageResults);
            this.currentClassTestToBeAmplified = null;
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

    }

    private void jsonReport(Coverage coverageResults) {
        TestClassJSON testClassJSON;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(this.configuration.getOutputDirectory() + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "_jacoco_instr_coverage.json");
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
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(testClassJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
