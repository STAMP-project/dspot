package fr.inria.diversify.utils.sosiefier;

import fr.inria.diversify.logger.KeyWord;
import fr.inria.diversify.logger.Pool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * User: Simon
 * Date: 30/06/16
 * Time: 11:28
 */
@Deprecated
public class TestCoverageParser extends LogParser<List<Coverage>> {
    protected Map<Integer, MethodCoverage> idToMethod;

    protected String currentTest;
    protected List<String> currentTestCoverage;


    public TestCoverageParser() {
        idToMethod = new HashMap<>();
        result = new ArrayList<>();
    }

    @Override
    public void readLogLine(String logLine) {
        String[] split = logLine.split(KeyWord.simpleSeparator);
        switch (split[0]) {
            case KeyWord.testStartObservation:
                currentTest = split[1];
                currentTestCoverage = new LinkedList<>();
                resetIdMethod();
                break;
            case KeyWord.testEndObservation:
                if (currentTest != null) {
                    parseCoverage();
                    addCoverage(new Coverage(currentTest, idToMethod));
                    currentTest = null;
                    resetIdMethod();
                }
                break;
            case KeyWord.branchObservation:
                currentTestCoverage.add(logLine);
                break;
        }
    }


    protected void addCoverage(Coverage toAdd) {
        Coverage find = result.stream()
                .filter(t -> toAdd.getName().equals(t.getName()))
                .findFirst()
                .orElse(null);

        if(find == null) {
            result.add(toAdd);
        } else {
            find.merge(toAdd);
        }
    }

    protected void resetIdMethod() {
        for(Integer key : idToMethod.keySet()) {
            MethodCoverage mc = idToMethod.get(key);
            if(mc.getAllPath().size() != 0) {
                idToMethod.put(key, new MethodCoverage(Pool.get(key), mc.getMethodName(), mc.getAllBranchId()));
            }
        }
    }

    protected void parseCoverage()  {
        for(String line : currentTestCoverage) {
            parseCoverageLine(line);
        }
    }

    protected void parseCoverageLine(String line) {
        String[] split = line.split(KeyWord.simpleSeparator);
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

    @Override
    public void init(File dir) throws IOException {
        idToMethod = new HashMap<>();
        File infoFile = new File(dir + "/info");
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
    }

    @Override
    public void newLogFile(File file) {}
}
