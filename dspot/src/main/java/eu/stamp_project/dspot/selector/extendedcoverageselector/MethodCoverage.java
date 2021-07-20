package eu.stamp_project.dspot.selector.extendedcoverageselector;

import eu.stamp_project.dspot.common.configuration.TestTuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MethodCoverage {

    public List<Integer> lineCoverage;
    public String methodDescriptor;

    public MethodCoverage(List<Integer> lineCoverage, String methodDescriptor) {
        this.lineCoverage = lineCoverage;
        this.methodDescriptor = methodDescriptor;
    }

    /**
     * calculate 'diff' of the coverage values wrt.
     * @param that other method coverage
     * @return coverage diff between each of the lines
     *
     * Only works for two method coverages from the same method (same length in number of lines)
     */
    public MethodCoverage improvementDiffOver(MethodCoverage that) {
        List<Integer> betterAt = IntStream.range(0, lineCoverage.size())
                .mapToObj(i -> lineCoverage.get(i) - that.lineCoverage.get(i))
                .collect(Collectors.toList());
        return new MethodCoverage(betterAt, this.methodDescriptor);
    }

    /**
     * Only works for two method coverages from the same method (same length in number of lines)
     */
    public MethodCoverage accumulate(MethodCoverage toAdd) {
        return new MethodCoverage(IntStream.range(0, this.lineCoverage.size())
                .mapToObj(i -> Math.max(this.lineCoverage.get(i), toAdd.lineCoverage.get(i)))
                .collect(Collectors.toList()), this.methodDescriptor);
    }

    public Map<Integer, Integer> coveragePerLine() {
        Map<Integer, Integer> map = new HashMap<>();
        int index = -1;
        for (Integer instructionImprovement : lineCoverage) {
            index++;
            if (instructionImprovement <= 0) {
                continue;
            }
            map.put(index,instructionImprovement);
        }
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodCoverage that = (MethodCoverage) o;
        return Objects.equals(lineCoverage, that.lineCoverage) && Objects
                .equals(methodDescriptor, that.methodDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineCoverage, methodDescriptor);
    }
}
