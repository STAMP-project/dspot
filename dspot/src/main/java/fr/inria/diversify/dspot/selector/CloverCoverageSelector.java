package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.compilation.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import eu.stamp.project.testrunner.EntryPoint;
import fr.inria.stamp.coverage.clover.CloverExecutor;
import eu.stamp.project.testrunner.runner.coverage.Coverage;
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
                    CloverExecutor.executeAll(this.configuration, PATH_TO_COPIED_FILES);
            this.originalLineCoveragePerClass = new HashMap<>();
            final InputProgram program = this.configuration.getInputProgram();
            lineCoveragePerTestMethods.keySet().stream()
                    .map(lineCoveragePerTestMethods::get)
                    .forEach(lineCoveragePerTestMethod ->
                            lineCoveragePerTestMethod.keySet().forEach(className -> {
                                        final CtType<?> key = program.getFactory().Type().get(className);
                                        if (!this.originalLineCoveragePerClass.containsKey(key)) {
                                            this.originalLineCoveragePerClass.put(key, new HashSet<>());
                                        }
                                        this.originalLineCoveragePerClass.get(key).addAll(lineCoveragePerTestMethod.get(className));
                                    }
                            )
                    );

            final String classesOfProject = program.getProgramDir() + program.getClassesDir() +
                    AmplificationHelper.PATH_SEPARATOR + program.getProgramDir() + program.getTestClassesDir();

            final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(this.configuration)
                    .buildClasspath(program.getProgramDir()) +
                    AmplificationHelper.PATH_SEPARATOR + classesOfProject;

            this.initialCoverage = EntryPoint.runCoverageOnTestClasses(classpath, classesOfProject,
                    DSpotUtils.getAllTestClasses(configuration)
            );
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
        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(PATH_TO_COPIED_FILES));
        final Map<String, Map<String, List<Integer>>> lineCoveragePerTestMethods =
                CloverExecutor.execute(this.configuration, PATH_TO_COPIED_FILES, clone.getQualifiedName());
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
                                                                this.configuration.getInputProgram().getFactory().Type().get(className)
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

        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.pathToTmpTestSources));

        final String classesOfProject = program.getProgramDir() + program.getClassesDir() +
                AmplificationHelper.PATH_SEPARATOR + program.getProgramDir() + program.getTestClassesDir();

        final String classpath = AutomaticBuilderFactory.getAutomaticBuilder(this.configuration)
                .buildClasspath(program.getProgramDir()) +
                AmplificationHelper.PATH_SEPARATOR + classesOfProject;

        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources, classpath,
                new File(this.program.getProgramDir() + "/" + this.program.getTestClassesDir()));

        return EntryPoint.runCoverageOnTestClasses(classpath, classesOfProject,
                clone.getQualifiedName()
        );
    }

    private final static String PATH_TO_COPIED_FILES = "target/dspot/copy/";

    private void initDirectory() {
        // in order to run clover easily, we have to put all the sources and
        // test classes in the same folder.
        // also, we will print amplified test in the same folder
        final File destDir = new File(PATH_TO_COPIED_FILES);
        try {
            FileUtils.forceDelete(destDir);
        } catch (IOException ignored) {
            //ignored
        }
        try {
            FileUtils.copyDirectory(
                    new File(this.configuration.getInputProgram().getAbsoluteSourceCodeDir()),
                    destDir
            );
            FileUtils.copyDirectory(
                    new File(this.configuration.getInputProgram().getAbsoluteTestSourceCodeDir()),
                    destDir
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
