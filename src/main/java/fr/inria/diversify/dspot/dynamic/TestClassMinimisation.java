package fr.inria.diversify.dspot.dynamic;

import fr.inria.diversify.dspot.ClassWithLoggerBuilder;
import fr.inria.diversify.log.LogReader;
import fr.inria.diversify.log.TestCoverageParser;
import fr.inria.diversify.log.TestGraphReader;
import fr.inria.diversify.log.branch.Coverage;
import fr.inria.diversify.log.graph.Graph;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.TestRunner;
import fr.inria.diversify.util.FileUtils;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 15/11/16
 * Time: 10:21
 */
public class TestClassMinimisation {
    protected TestRunner testRunnerWithBranchLogger;
    protected ClassWithLoggerBuilder classWithLoggerBuilder;
    protected File logDir;
    protected InputProgram inputProgram;

    protected List<Coverage> currentBranchCoverage;
    protected List<Graph> currentGraphsCoverage;

    public TestClassMinimisation(InputProgram inputProgram, TestRunner testRunnerWithBranchLogger, String logDir) {
        this.testRunnerWithBranchLogger = testRunnerWithBranchLogger;
        this.inputProgram = inputProgram;
        this.logDir = new File(logDir);
        this.classWithLoggerBuilder = new ClassWithLoggerBuilder(inputProgram.getFactory());
    }

    public CtType minimiseTests(CtType classTest) {
        inputProgram.getFactory().Type().get(Runnable.class);

        CtType cl = classWithLoggerBuilder.buildClassWithLogger(classTest, classTest.getMethods());
        try {
            fr.inria.diversify.logger.Logger.reset();
            fr.inria.diversify.logger.Logger.setLogDir(logDir);
            testRunnerWithBranchLogger.runTests(cl, cl.getMethods());
            loadInfo();

            Set<String> mthsSubSet = currentBranchCoverage.stream()
                    .collect(Collectors.groupingBy(c -> c.getCoverageBranch()))
                    .values().stream()
                    .map(value -> value.stream().findAny().get())
                    .map(c -> c.getName())
                    .collect(Collectors.toSet());

            mthsSubSet.addAll(currentGraphsCoverage.stream()
                    .collect(Collectors.groupingBy(c -> c.getEdges()))
                    .values().stream()
                    .map(value -> value.stream().findAny().get())
                    .map(c -> c.getName())
                    .collect(Collectors.toSet()));

            Set<CtMethod> mths = new HashSet<>(classTest.getMethods());
            mths.stream()
                    .filter(mth -> !mthsSubSet.contains(classTest.getQualifiedName() + "." + mth.getSimpleName()))
                    .forEach(mth -> classTest.removeMethod(mth));

        } catch (Exception e) {}

        return classTest;
    }

    protected List<Coverage> loadInfo() throws IOException {
        List<Coverage> branchCoverage = null;
        try {
            LogReader logReader = new LogReader(logDir.getAbsolutePath());
            TestCoverageParser coverageParser = new TestCoverageParser();
            TestGraphReader graphParser = new TestGraphReader();
            logReader.addParser(graphParser);
            logReader.addParser(coverageParser);
            logReader.readLogs();

            currentBranchCoverage = coverageParser.getResult();
            currentGraphsCoverage = graphParser.getResult();
        } catch (Throwable e) {}

        deleteLogFile();

        return branchCoverage;
    }

    protected void deleteLogFile() throws IOException {
        for(File file : logDir.listFiles()) {
            if(!file.getName().equals("info")) {
                FileUtils.forceDelete(file);
            }
        }
    }
}
