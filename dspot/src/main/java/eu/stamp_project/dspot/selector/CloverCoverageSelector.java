package eu.stamp_project.dspot.selector;

import eu.stamp_project.clover.CloverExecutor;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.runner.coverage.Coverage;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/12/17
 */
public class CloverCoverageSelector extends TakeAllSelector {

    private Map<CtType<?>, Set<Integer>> originalLineCoveragePerClass;

    private Coverage initialCoverage;

    @Override
    public void init(InputConfiguration configuration) {
        super.init(configuration);
        this.selectedAmplifiedTest.clear();
        this.initDirectory();
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(List<CtMethod<?>> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
            this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();
            final Map<String, Map<String, List<Integer>>> lineCoveragePerTestMethods =
                    CloverExecutor.executeAll(this.configuration, getPathToCopiedFiles());
            this.originalLineCoveragePerClass = new HashMap<>();
            lineCoveragePerTestMethods.keySet().stream()
                    .map(lineCoveragePerTestMethods::get)
                    .forEach(lineCoveragePerTestMethod ->
                            lineCoveragePerTestMethod.keySet().forEach(className -> {
                                        final CtType<?> key = configuration.getFactory().Type().get(className);
                                        if (!this.originalLineCoveragePerClass.containsKey(key)) {
                                            this.originalLineCoveragePerClass.put(key, new HashSet<>());
                                        }
                                        this.originalLineCoveragePerClass.get(key).addAll(lineCoveragePerTestMethod.get(className));
                                    }
                            )
                    );

            final String classpath = this.configuration.getDependencies()
                            + AmplificationHelper.PATH_SEPARATOR +
                            this.configuration.getClasspathClassesProject();

            try {
                this.initialCoverage = EntryPoint.runCoverageOnTestClasses(
                        classpath,
                        this.configuration.getClasspathClassesProject(),
                        DSpotUtils.getAllTestClasses(configuration)
                );
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
        if (testsToBeAmplified.size() > 1) {
            final List<CtMethod<?>> collect = testsToBeAmplified.stream()
                    .filter(this.selectedAmplifiedTest::contains)
                    .collect(Collectors.toList());
            if (collect.isEmpty()) {
                return testsToBeAmplified;
            } else {
                return collect;
            }
        } else {
            return testsToBeAmplified;
        }
    }

    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
        if (amplifiedTestToBeKept.isEmpty()) {
            return amplifiedTestToBeKept;
        }
        CtType<?> clone = this.currentClassTestToBeAmplified.clone();
        clone.setParent(this.currentClassTestToBeAmplified.getParent());
        this.currentClassTestToBeAmplified.getMethods().stream()
                .filter(AmplificationChecker::isTest)
                .forEach(clone::removeMethod);
        amplifiedTestToBeKept.forEach(clone::addMethod);

        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(getPathToCopiedFiles()));

        final Map<String, Map<String, List<Integer>>> lineCoveragePerTestMethods =
                CloverExecutor.execute(this.configuration, getPathToCopiedFiles(), clone.getQualifiedName());
        final List<CtMethod<?>> selectedTests = this.selectTests(clone, lineCoveragePerTestMethods);
        this.selectedAmplifiedTest.addAll(selectedTests);
        return selectedTests;
    }

    private List<CtMethod<?>> selectTests(CtType<?> clone, Map<String, Map<String, List<Integer>>> lineCoveragePerTestMethods) {
        return lineCoveragePerTestMethods.keySet()
                .stream()
                .filter(testMethodName ->
                        lineCoveragePerTestMethods.get(testMethodName)
                                .keySet()
                                .stream()
                                .anyMatch(className ->
                                        lineCoveragePerTestMethods.get(testMethodName).get(className)
                                                .stream()
                                                .anyMatch(executedLine ->
                                                        !this.originalLineCoveragePerClass.get(
                                                                this.configuration.getFactory().Type().get(className)
                                                        ).contains(executedLine)
                                                )
                                )
                )
                .map(clone::getMethodsByName)
                .map(ctMethods -> ctMethods.get(0))
                .collect(Collectors.toList());
    }

    @Override
    public void report() {
        final Coverage amplifiedCoverage = computeAmplifiedCoverage();
        final String nl = System.getProperty("line.separator");

        final StringBuilder report = new StringBuilder();
        report.append("======= REPORT =======").append(nl);
        report.append("Initial Coverage: ").append(this.initialCoverage.toString()).append(nl);
        report.append("The amplification results with: ").append(this.selectedAmplifiedTest.size())
                .append(" amplified test cases").append(nl);
        report.append("Amplified Coverage: ").append(amplifiedCoverage.toString()).append(nl);

        System.out.println(report.toString());
        File reportDir = new File(this.configuration.getOutputDirectory());
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }

        // TODO implement a JSON report

        if (this.currentClassTestToBeAmplified != null) {
            try (FileWriter writer = new FileWriter(this.configuration.getOutputDirectory() + "/" +
                    this.currentClassTestToBeAmplified.getQualifiedName() + "_clover_coverage.txt", false)) {
                writer.write(report.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        this.currentClassTestToBeAmplified = null;
    }

    private Coverage computeAmplifiedCoverage() {
        // computing the amplified test coverage
        CtType<?> clone = this.currentClassTestToBeAmplified.clone();
        clone.setParent(this.currentClassTestToBeAmplified.getParent());
        this.selectedAmplifiedTest.forEach(clone::addMethod);

        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.PATH_TO_AMPLIFIED_TEST_SRC));

        final String classpath =
                this.configuration.getDependencies()
                + AmplificationHelper.PATH_SEPARATOR +
                this.configuration.getClasspathClassesProject();

        DSpotCompiler.compile(this.configuration, DSpotCompiler.PATH_TO_AMPLIFIED_TEST_SRC, classpath,
                new File(this.configuration.getAbsolutePathToTestClasses()));

        try {
            return EntryPoint.runCoverageOnTestClasses(
                    classpath,
                    this.configuration.getClasspathClassesProject(),
                    clone.getQualifiedName()
            );
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private final static String PATH_TO_COPIED_FILES = "target/dspot/copy/";
    

	private String getPathToCopiedFiles() {
		return DSpotUtils.shouldAddSeparator.apply(this.configuration.getAbsolutePathToProjectRoot())
				+ PATH_TO_COPIED_FILES;
	}
	
    private void initDirectory() {
        // in order to run clover easily, we have to put all the sources and
        // test classes in the same folder.
        // also, we will print amplified test in the same folder
        final File destDir = new File(getPathToCopiedFiles());
        try {
            FileUtils.forceDelete(destDir);
        } catch (IOException ignored) {
            //ignored
        }
        try {
            FileUtils.copyDirectory(
                    new File(this.configuration.getAbsolutePathToSourceCode()),
                    destDir
            );
            FileUtils.copyDirectory(
                    new File(this.configuration.getAbsolutePathToTestSourceCode()),
                    destDir
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
