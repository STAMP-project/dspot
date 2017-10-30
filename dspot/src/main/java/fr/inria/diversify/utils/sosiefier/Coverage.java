package fr.inria.diversify.utils.sosiefier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Simon
 * Date: 24/04/15
 * Time: 10:47
 */
@Deprecated
public class Coverage {
    String name;
    Collection<MethodCoverage> methodCoverages;


    public Coverage(String testName, Map<Integer, MethodCoverage> idToMethod) {
        this.name = testName;
        this.methodCoverages = new ArrayList<>();
        for (Integer id : idToMethod.keySet()) {
            MethodCoverage mc = idToMethod.get(id);
            if (mc.allPath.size() != 0) {
                methodCoverages.add(mc);
            }
        }
    }

    public Coverage(String name) {
        this.name = name;
        this.methodCoverages = new ArrayList<>();
    }

    public double coverage() {
        double allBranch = 0;
        double branch = 0;

        for(MethodCoverage mc : methodCoverages) {
            allBranch += mc.getAllBranchId().size();
            branch += mc.getCoveredBranchCoverages().size();
        }

        return branch/allBranch;
    }

    public double coverage(Collection<String> classes) {
        double allBranch = 0;
        double branch = 0;

        for (MethodCoverage mc : methodCoverages) {
            if (classes.stream().anyMatch(cl -> mc.getMethodName().startsWith(cl + "_"))) {
                allBranch += mc.getAllBranchId().size();
                branch += mc.getCoveredBranchCoverages().size();
            }
        }

        return branch / allBranch;
    }

    public void info() {
        int count = 0;
        for(MethodCoverage mc : methodCoverages) {
            if(mc.coverage() != 0) {
                count++;
//                Log.info("{}: {}", mc.getMethodName(), mc.coverage());
            }
        }
//        Log.info("count: {}",count);
    }

    public MethodCoverage getMethodCoverage(String name) {
        return methodCoverages.stream()
                .filter(mc -> mc.getMethodName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public void merge(Coverage other) {
        for(MethodCoverage mc : other.methodCoverages) {
            String name = mc.getMethodName();
            if(getMethodCoverage(name) != null) {
                getMethodCoverage(name).merge(mc);
            } else {
                methodCoverages.add(mc);
            }
        }
    }


    public Set<String> getCoverageBranch() {
        Set<String> set = new HashSet<>();
        for(MethodCoverage mc : methodCoverages) {
            for(BranchCoverage branchCoverage : mc.coveredBranchCoverages) {
                set.add(mc.getMethodName()+ "." + branchCoverage.getId());
            }
        }
        return set;
    }

    public String getName() {
        return name;
    }

}
