package eu.stamp_project.dspot.selector.extendedcoverageselector;

import eu.stamp_project.testrunner.listener.Coverage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExtendedCoverage {

    private ProjectCoverageMap instructionsProjectCoverageMap;

    public ExtendedCoverage(Coverage coverage) {

        this.instructionsProjectCoverageMap = new ProjectCoverageMap();
        String[] classes = coverage.getExecutionPath().split("-");
        for (String aClass : classes) {
            String[] split = aClass.split(":");
            if (split.length >= 2) {
                String className = split[0].replaceAll("/","\\.");
                instructionsProjectCoverageMap.addClassCoverage(className, createClassCoverageMap(split[1]));
            }
        }
        
        this.instructionsProjectCoverageMap = cleanAllZeroValuesFromMap(this.instructionsProjectCoverageMap);
    }

    public ExtendedCoverage(ProjectCoverageMap instructionsProjectCoverageMap) {
        this.instructionsProjectCoverageMap = instructionsProjectCoverageMap;
    }

    private ClassCoverageMap createClassCoverageMap(String perMethodData) {
        ClassCoverageMap classCoverageMap = new ClassCoverageMap();
        String[] perMethod = perMethodData.split("\\|");
        for (String s : perMethod) {
            String[] split = s.split("\\+");
            String methodName = split[0];
            String methodDescriptor = split[1];
            List<Integer> coveredLines =
                    Arrays.stream(split[2].split(",")).map(Integer::parseInt).collect(Collectors.toList());
            MethodCoverage methodCoverage = new MethodCoverage(coveredLines, methodDescriptor);
            classCoverageMap.addMethodCoverage(methodName, methodCoverage);
        }
        return classCoverageMap;
    }

    public ProjectCoverageMap getInstructionsProjectCoverageMap() {
        return instructionsProjectCoverageMap;
    }

    private ProjectCoverageMap cleanAllZeroValuesFromMap(ProjectCoverageMap map) {
        ProjectCoverageMap projectCoverageMap = new ProjectCoverageMap();
        map.classCoverageMaps.forEach((className, classCoverage) -> {
            ClassCoverageMap classCoverageMap = new ClassCoverageMap();
            classCoverage.methodCoverageMap.forEach((methodName, methodCoverage) -> {
                if (!methodCoverage.lineCoverage.stream().allMatch(i -> i == 0)) {
                    classCoverageMap.addMethodCoverage(methodName, methodCoverage);
                }
            });
            if (!classCoverageMap.methodCoverageMap.isEmpty()) {
                projectCoverageMap.addClassCoverage(className,classCoverageMap);
            }
        });
        return projectCoverageMap;
    }

    public boolean isBetterThan(ExtendedCoverage that) {
        if (that == null) {
            return true;
        }
        ProjectCoverageMap instructionDiff = this.instructionsProjectCoverageMap.improvementDiffOver(
                that.instructionsProjectCoverageMap);

        return !instructionDiff.classCoverageMaps.isEmpty();
    }

    public void accumulate(ExtendedCoverage toAdd) {
        this.instructionsProjectCoverageMap = this.instructionsProjectCoverageMap.accumulate(
                toAdd.instructionsProjectCoverageMap);
    }

    public CoverageImprovement coverageImprovementOver(ExtendedCoverage other) {
        ProjectCoverageMap instructionDiff = this.instructionsProjectCoverageMap.improvementDiffOver(
                other.instructionsProjectCoverageMap);
        return new CoverageImprovement(instructionDiff);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedCoverage that = (ExtendedCoverage) o;
        return Objects.equals(instructionsProjectCoverageMap, that.instructionsProjectCoverageMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instructionsProjectCoverageMap);
    }

    public ExtendedCoverage clone() {
        return new ExtendedCoverage(this.instructionsProjectCoverageMap);
    }

    @Override
    public String toString() {
        return "ExtendedCoverage{" + "instructionsProjectCoverageMap=" + instructionsProjectCoverageMap + '}';
    }
}
