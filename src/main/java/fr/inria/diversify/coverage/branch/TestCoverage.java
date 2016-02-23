package fr.inria.diversify.coverage.branch;


import fr.inria.diversify.transformation.Transformation;
import spoon.reflect.cu.SourcePosition;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 28/04/15
 * Time: 14:17
 */
public class TestCoverage {
    String testName;
    Coverage coverage;


    public TestCoverage(String testName, Map<Integer, MethodCoverage> idToMethod) {
        this.testName = testName;
        List<MethodCoverage> list = new ArrayList<>();
        for (Integer id : idToMethod.keySet()) {
            MethodCoverage mc = idToMethod.get(id);
            if (mc.allPath.size() != 0) {
                list.add(mc);
            }
        }
        coverage = new Coverage(list);
    }

    public Set<String> diff(TestCoverage other) {
        Set<String> branchs = coverage.getCoverageBranch();
        Set<String> otherBranchs = other.coverage.getCoverageBranch();

        Set<String> diff = otherBranchs.stream()
                .filter(branch -> !branch.contains(branch))
                .collect(Collectors.toSet());
        branchs.stream()
                .filter(branch -> !otherBranchs.contains(branch))
                .forEach(branch -> diff.add(branch));

        return diff;
    }

    public void merge(TestCoverage other) {
        coverage.merge(other.coverage);
    }

    public boolean containsAllBranch(TestCoverage other) {
        return coverage.getCoverageBranch().containsAll(other.coverage.getCoverageBranch());
    }

    public String getTestName() {
        return testName;
    }

    public Coverage getCoverage() {
        return coverage;
    }

    public void csv(PrintWriter fileWriter, Collection<Transformation> transformations, Map<String, SourcePosition> positions, Map<String, String> conditionsType) throws IOException {

        for (MethodCoverage mc : coverage.getMethodCoverages()) {
            for (BranchCoverage branchCoverage : mc.getCoveredBranchCoverages()) {
                for (int deep : branchCoverage.deeps) {
                    String branchId = mc.getMethodId() + "." + branchCoverage.getId();
                    Set<Transformation> trans = transformationForThisBranch(branchId, transformations , positions);
                    long sosie = trans.stream()
                            .filter(t -> t.isSosie())
                            .count();
                    long compile = trans.stream()
                            .filter(t -> t.getStatus() >= -1)
                            .count();
                    fileWriter.append(testName + ";"
                            + mc.getDeclaringClass() + ";"
                            + mc.getMethodName() + ";" + branchCoverage.getId() + ";"
                            + mc.getMethodName() + "." + branchCoverage.getId() + ";"
                            + deep + ";"
                            + trans.size()  + ";"
                            + sosie  + ";"
                            + compile + ";"
                            + conditionTypeForThisBranch(branchId, conditionsType) + "\n");

                }
            }
        }
    }

    protected String conditionTypeForThisBranch(String branchId, Map<String, String> conditionsType) {
        return conditionsType.getOrDefault(branchId, "none");

    }

    protected Set<Transformation> transformationForThisBranch(String branchId, Collection<Transformation> transformations, Map<String, SourcePosition> positions) {
        SourcePosition branchPosition = positions.get(branchId);
        if(branchPosition == null) {
            return new HashSet<>();
        }

        return transformations.parallelStream()
                .filter(transformation -> transformation.getPositions().stream()
                        .anyMatch(transPosition -> branchPosition.getCompilationUnit().equals(transPosition.getCompilationUnit())
                                && branchPosition.getSourceStart() <= transPosition.getSourceStart()
                                && branchPosition.getSourceEnd() >= transPosition.getSourceEnd())
                )
                .collect(Collectors.toSet());
    }

    public Set<String> getAllBranch() {
        Set<String> branchs = new HashSet<>();

        for (MethodCoverage mc : coverage.getMethodCoverages()) {
            for (BranchCoverage branchCoverage : mc.getCoveredBranchCoverages()) {
                branchs.add(mc.getMethodId() + "." + branchCoverage.getId());
            }
        }
        return branchs;
    }

    public Set<String> getCoveredBranch() {
        return coverage.getCoverageBranch();
    }
}
