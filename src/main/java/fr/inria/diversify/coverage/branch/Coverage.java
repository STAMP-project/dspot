package fr.inria.diversify.coverage.branch;

import fr.inria.diversify.transformation.Transformation;
import fr.inria.diversify.util.Log;
import spoon.reflect.cu.SourcePosition;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 24/04/15
 * Time: 10:47
 */
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

        for(MethodCoverage mc : methodCoverages) {
            if (classes.stream().anyMatch(cl -> mc.getMethodName().startsWith(cl+"_"))) {
                allBranch += mc.getAllBranchId().size();
                branch += mc.getCoveredBranchCoverages().size();

                if(!mc.getNotCoveredBranchId().isEmpty()) {
                    Log.debug("{} {} {}", mc.getMethodId(), mc.getMethodName(), mc.getNotCoveredBranchId());
                }
            }
        }

        return branch/allBranch;
    }

    public int distance(Coverage other) {
        int d = 0;
        for(MethodCoverage mc : methodCoverages) {
            d += mc.distance(other.getMethodCoverage(mc.getMethodName()));
        }

        return d;
    }

    public void info() {
        int count = 0;
        for(MethodCoverage mc : methodCoverages) {
            if(mc.coverage() != 0) {
                count++;
                Log.info("{}: {}", mc.getMethodName(), mc.coverage());
            }
        }
        Log.info("count: {}",count);
    }

    public MethodCoverage getMethodCoverage(String name) {
        return methodCoverages.stream()
                .filter(mc -> mc.getMethodName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public int nbDiffPath(Coverage other) {
        int d = 0;
        for(MethodCoverage mc : methodCoverages) {
            d += mc.nbDiffPath(other.getMethodCoverage(mc.getMethodName()));
        }
        return d;
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


    public Set<String> getCoverageBranchFor(Collection<String> classes) {
        Set<String> set = new HashSet<>();
        for(MethodCoverage mc : methodCoverages) {
            if (classes.stream().anyMatch(cl -> mc.getMethodName().startsWith(cl +"_"))) {
                for (BranchCoverage branchCoverage : mc.coveredBranchCoverages) {
                    set.add(mc.getMethodName() + "." + branchCoverage.getId());
                }
            }
        }

        return set;
    }

    public Set<String> getAllBranch() {
        Set<String> set = new HashSet<>();
        for(MethodCoverage mc : methodCoverages) {
            for(String branch : mc.allBranch) {
                set.add(mc.getMethodName()+ "." +branch);
            }
        }

        return set;
    }

    public Set<String> allBranchWithout(Coverage other) {
        Set<String> otherBranchs = other.getAllBranch();
        return getAllBranch().stream()
                .filter(b -> !otherBranchs.contains(b))
                .collect(Collectors.toSet());
    }

    public Set<String> getAllBranch(Collection<String> classes) {
        Set<String> set = new HashSet<>();
        for(MethodCoverage mc : methodCoverages) {
            if (classes.stream().anyMatch(cl -> mc.getMethodName().startsWith(cl+"_"))) {
                for (String branch : mc.allBranch) {
                    set.add(mc.getMethodName() + "." + branch);
                }
            }
        }

        return set;
    }


    public void csv(String fileName, Collection<Transformation> transformations, Map<String, SourcePosition> positions, Map<String, String> conditionsType) throws IOException {
        PrintWriter fileWriter = new PrintWriter(new FileWriter(fileName));
        fileWriter.append("class;method;branch;branchGlobalId;deep;nbOfPath;transformation;sosie;compile;branchConditionType\n");

        for (MethodCoverage mc : methodCoverages) {
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
                    fileWriter.append(mc.getDeclaringClass() + ";"
                            + mc.getMethodName() + ";" + branchCoverage.getId() + ";"
                            + mc.getMethodName() + "." + branchCoverage.getId() + ";"
                            + deep + ";"
                            + mc.getAllPath().size()
                            + trans.size()  + ";"
                            + sosie  + ";"
                            + compile + ";"
                            + conditionTypeForThisBranch(branchId, conditionsType) + "\n");
                }
            }
        }
        fileWriter.close();
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


    public Collection<MethodCoverage> getMethodCoverages() {
        return methodCoverages;
    }

    public BranchCoverage getBranch(String branch) {
        String[] split = branch.split("\\.");

        Integer methodId = Integer.parseInt(split[0]);

       return methodCoverages.stream()
                   .filter(mtc -> mtc.getMethodId().equals(methodId))
                   .map(mtc -> mtc.getBranch(split[1]))
                   .filter(br -> br != null)
                   .findFirst()
                   .orElse(null);
    }

    public Set<String> diff(Coverage other) {
        Set<String> branchs = getCoverageBranch();
        Set<String> otherBranchs = other.getCoverageBranch();

        Set<String> diff = otherBranchs.stream()
                .filter(branch -> !branch.contains(branch))
                .collect(Collectors.toSet());
        branchs.stream()
                .filter(branch -> !otherBranchs.contains(branch))
                .forEach(branch -> diff.add(branch));

        return diff;
    }

    public boolean containsAllBranch(Coverage other) {
        return getCoverageBranch().containsAll(other.getCoverageBranch());
    }

    public String getName() {
        return name;
    }

}
