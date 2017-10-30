package fr.inria.diversify.utils.sosiefier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 22/04/15
 * Time: 15:49
 */
@Deprecated
public class MethodCoverage {
    protected final Integer methodId;
    protected final String methodName;
    protected final Set<String> allBranch;
    protected Set<List<String>> allPath;
    protected Set<BranchCoverage> coveredBranchCoverages;

    public MethodCoverage(Integer methodId, String methodName, String[] allBranch) {
        this.methodId = methodId;
        this.allBranch = new HashSet<>();
        Collections.addAll(this.allBranch, allBranch);
        this.methodName = methodName;
        allPath = new HashSet<>();
        coveredBranchCoverages = new HashSet<>();
    }

    public MethodCoverage(Integer methodId, String methodName, Set<String> allBranch) {
        this.methodId = methodId;
        this.methodName = methodName;
        this.allBranch = new HashSet<>(allBranch);
        allPath = new HashSet<>();
        coveredBranchCoverages = new HashSet<>();
    }

    public void addPath(int methodDeep, String[] path) {
        List<String> compressPath = new ArrayList<>(path.length);
        for(String p : path) {
            if(!compressPath.contains(p)) {
                compressPath.add(p);
            }
        }
        addCompressPath(methodDeep, compressPath);
    }

    public void addCompressPath(int methodDeep,  List<String> compressPath) {
        allPath.add(compressPath);

        for(String id : compressPath) {
            BranchCoverage existing = null;
            for(BranchCoverage branchCoverage : coveredBranchCoverages) {
                if(branchCoverage.getId().equals(id)) {
                    existing = branchCoverage;
                    break;
                }
            }
            if(existing == null) {
                existing = new BranchCoverage(id,methodDeep);
                coveredBranchCoverages.add(existing);
            } else {
                existing.addDeep(methodDeep);
            }
        }
    }

    public double coverage() {
        return ((double) coveredBranchCoverages.size()) / ((double) allBranch.size());
    }

    public Set<String> getNotCoveredBranchId() {
        Set<String> coveredBranchId = coveredBranchCoverages.stream()
                .map(b -> b.getId())
                .collect(Collectors.toSet());

        return allBranch.stream()
                .filter(branchId -> !coveredBranchId.contains(branchId))
                .collect(Collectors.toSet());
    }

    public String getMethodName() {
        return methodName;
    }

    public Set<String> getAllBranchId() {
        return allBranch;
    }

    public Set<String> getAllBranch() {
        return allBranch.stream()
                .map(branch -> methodId +"."+branch)
                .collect(Collectors.toSet());
    }

    public Set<List<String>> getAllPath() {
        return allPath;
    }

    public Set<BranchCoverage> getCoveredBranchCoverages() {
        return coveredBranchCoverages;
    }

    public Set<String> getCoveredBranchId() {
        return coveredBranchCoverages.stream()
                .map(b -> b.getId())
                .collect(Collectors.toSet());
    }

    public int distance(MethodCoverage other) {
        int d = 0;
        for(String branch : allBranch) {
            if(getCoveredBranchId().contains(branch) != other.getCoveredBranchId().contains(branch)) {
                d++;
//                Log.info("{} {} {}, this {}, other {}",getMethodId(), getMethodName(), branch, getNotCoveredBranchId() ,other.getNotCoveredBranchId());
            }
        }
        return d;
    }

    public int nbDiffPath(MethodCoverage other) {
        int count = 0;

        for(List<String> path : allPath) {
            if(!other.allPath.contains(path)) {
                count++;
            }
        }

        for(List<String> path : other.allPath) {
            if(!allPath.contains(path)) {
                count++;
            }
        }

        return count++;
    }

    public void merge(MethodCoverage other) {
        this.allPath.addAll(other.allPath);
        for(BranchCoverage otherBranchCoverage : other.coveredBranchCoverages) {
            BranchCoverage existing = null;
            for(BranchCoverage branchCoverage : coveredBranchCoverages) {
                if(branchCoverage.getId().equals(otherBranchCoverage.getId())) {
                    existing = branchCoverage;
                    break;
                }
            }
            if(existing == null) {
                coveredBranchCoverages.add(otherBranchCoverage);
            } else {
                existing.addAllDeep(otherBranchCoverage.getDeeps());
            }
        }
    }

    public Integer getMethodId() {
        return methodId;
    }

    public String getDeclaringClass() {
        return methodName.split("_")[0];
    }

    public BranchCoverage getBranch(String branchId) {
        return coveredBranchCoverages.stream()
                .filter(branch -> branch.getId().equals(branchId))
                .findFirst()
                .orElse(null);
    }
}
