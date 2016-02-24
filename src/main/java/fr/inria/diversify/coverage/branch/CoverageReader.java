package fr.inria.diversify.coverage.branch;

import fr.inria.diversify.profiling.logger.Pool;
import fr.inria.diversify.profiling.logger.KeyWord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * User: Simon
 * Date: 23/04/15
 * Time: 10:04
 */
public class CoverageReader {
    protected final String directory;


    public CoverageReader(String directory) {
        this.directory = directory;
    }


//    public Coverage load() throws IOException {
//        Map<Integer, MethodCoverage> idToMethod = loadInfo();
//        loadData(idToMethod);
//        return new Coverage(idToMethod.values());
//    }

    public List<Coverage> loadTest() throws IOException {
        Map<Integer, MethodCoverage> idToMethod =  loadInfo();
        return loadTestData(idToMethod);
    }

    protected List<Coverage> loadTestData(Map<Integer, MethodCoverage> idToMethod) throws IOException {
        File dir = new File(directory);
        List<Coverage> testCoverages = new ArrayList<>();

        for(File file : dir.listFiles()) {
            if(file.isFile() && file.getName().startsWith("log")) {
                List<Coverage> tmp = parseTestCoverageFile(file, idToMethod);
                testCoverages = mergeTestCoverageList(testCoverages, tmp);
            }
        }

        return testCoverages;
    }

    protected List<Coverage> mergeTestCoverageList(List<Coverage> list1, List<Coverage> list2) {
        for(Coverage tc : list2) {
            Coverage find = list1.stream()
                    .filter(t -> t.name.equals(tc.name))
                    .findFirst()
                    .orElse(null);

            if(find == null) {
                list1.add(tc);
            } else {
                find.merge(find);
            }
        }

        return list1;
    }

    protected List<Coverage> parseTestCoverageFile(File file, Map<Integer, MethodCoverage> idToMethod) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        List<Coverage> testCoverages = new ArrayList<>();
        List<String> currentTestCoverage = new LinkedList<>();
        String currentTest = null;

        String line = br.readLine();
        String logEntry = "";
        while (line != null) {
            line = br.readLine();
            logEntry = logEntry + line;
            if (logEntry.endsWith("$$")) {
                logEntry = logEntry.substring(0, logEntry.length() - 2);
                String[] split = logEntry.split(";");
                switch (split[0]) {
                    case KeyWord.testStartObservation:
                        currentTest = split[1];
                        currentTestCoverage = new LinkedList<>();
                        resetIdMethod(idToMethod);
                        break;
                    case KeyWord.testEndObservation:
                        if (currentTest != null) {
                            parseCoverage(currentTestCoverage, idToMethod);
                            testCoverages.add(new Coverage(currentTest, idToMethod));
                            currentTest = null;
                            resetIdMethod(idToMethod);
                        }
                        break;
                    case KeyWord.branchObservation:
                        currentTestCoverage.add(logEntry);
                        break;
                    default:
                        break;
                }
                logEntry = "";
            }
            if(logEntry.startsWith(KeyWord.testEndObservation) && currentTest != null) {
                parseCoverage(currentTestCoverage, idToMethod);
                testCoverages.add(new Coverage(currentTest, idToMethod));
                currentTest = null;
                resetIdMethod(idToMethod);
            }
        }

        return testCoverages;
    }

    protected void resetIdMethod(Map<Integer, MethodCoverage> idToMethod) {
        for(Integer key : idToMethod.keySet()) {
            MethodCoverage mc = idToMethod.get(key);
            if(mc.allPath.size() != 0) {
                idToMethod.put(key, new MethodCoverage(Pool.get(key), mc.getMethodName(), mc.getAllBranchId()));
            }
        }
    }

    public void loadData(Map<Integer, MethodCoverage> idToMethod) throws IOException {
        File dir = new File(directory);

        for(File file : dir.listFiles()) {
            if(file.isFile() && file.getName().startsWith("log")) {
                parseCoverageFile(file, idToMethod);
            }
        }
    }

    protected void parseCoverage(List<String> data, Map<Integer, MethodCoverage> idToMethod)  {

        for(String line : data) {
            parseCoverageLine(line, idToMethod);
        }
    }

    protected void parseCoverageFile(File file, Map<Integer, MethodCoverage> idToMethod) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line = br.readLine();
        String logEntry = "";
        while (line != null) {
            line = br.readLine();
            logEntry = logEntry + line;
            if (logEntry.endsWith("$$")) {
                logEntry = logEntry.substring(0, logEntry.length() - 2);
                if(logEntry.startsWith("P;")) {
                    parseCoverageLine(logEntry, idToMethod);
                }
                logEntry = "";
            }
        }
    }

    protected void parseCoverageLine(String line, Map<Integer, MethodCoverage> idToMethod) {
        String[] split = line.split(";");
        if(split.length != 1) {
            int methodId = Integer.parseInt(split[1]);
            int methodDeep = Pool.get(Integer.parseInt(split[2]));
            MethodCoverage methodCoverage = idToMethod.get(methodId);

            String[] path = new String[ split.length - 3];
            for(int i = 3; i < split.length; i++) {
                path[i - 3] = Pool.get(split[i]);
            }

             methodCoverage.addPath(methodDeep, path);
        }
    }

    public Map<Integer, MethodCoverage> loadInfo() throws IOException {
        Map<Integer, MethodCoverage> idToMethod = new HashMap<>();
        File infoFile = new File(directory + "/info");
        BufferedReader br = new BufferedReader(new FileReader(infoFile));

        String line = br.readLine();
        while (line != null) {
            if(!line.startsWith("id")) {
                String[] split = line.split(";");
                Integer methodId = Integer.parseInt(split[0]);
                String[] branches = new String[split.length - 2];
                for(int i = 2; i < split.length; i++) {
                    branches[i - 2] = Pool.get(split[i]);
                }
                MethodCoverage methodCoverage = new MethodCoverage(methodId, split[1], branches);
                idToMethod.put(methodId, methodCoverage);
            }
            line = br.readLine();
        }

        return idToMethod;
    }
}
