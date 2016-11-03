package fr.inria.diversify.dspot;

import fr.inria.diversify.log.branch.Coverage;
import fr.inria.diversify.log.branch.MethodCoverage;
import fr.inria.diversify.logger.Pool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * User: Simon
 * Date: 16/12/15
 * Time: 16:39
 */
public class DSpotLogger {
    private static HashMap<Thread, DSpotLogger> logs = null;
    protected static List<Coverage> testCoverages = new ArrayList<Coverage>();
    private static File directory;

    protected Map<Integer, MethodCoverage> idToMethod;
    protected String currentTest;
    protected Stack<Integer> currentMethod;
    protected Stack<List<String>> currentBranch;
    protected int deep;

    protected static DSpotLogger getLog(Thread thread) {
        if ( logs == null ) {
            logs = new HashMap<Thread, DSpotLogger>(); }
        if ( logs.containsKey(thread) ) {
            return logs.get(thread);
        } else {
            DSpotLogger l = new DSpotLogger();
            logs.put(thread, l);
            return l;
        }
    }

    public DSpotLogger() {
        try {
            currentMethod = new Stack<Integer>();
            currentBranch = new Stack<List<String>>();
            loadInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void branch(Thread thread, String id) {
        getLog(thread).branch(id);
    }
    public void branch(String id) {
        List<String> branches = currentBranch.peek();
        if(!branches.contains(id)) {
            branches.add(id);
        }
    }

    public static void methodIn(Thread thread, String id) {
        getLog(thread).methodIn(id);
    }
    public void methodIn(String id) {
        deep++;
        currentMethod.push(Integer.parseInt(id));
        currentBranch.push(new LinkedList<String>());
    }

    public static void methodOut(Thread thread, String id) {
        getLog(thread).methodOut(id);
    }
    public void methodOut(String id) {
        deep--;

        Integer methodId = currentMethod.pop();
        MethodCoverage methodCoverage = idToMethod.get(methodId);

        methodCoverage.addCompressPath(deep, currentBranch.pop());

    }

    public static void writeTestStart(Thread thread, Object receiver, String testName) {
        getLog(thread).writeTestStart(receiver, testName);
    }
    protected void writeTestStart(Object receiver, String testName) {
        deep = 0;
        currentTest = receiver.getClass().getCanonicalName() + "." + testName;
        resetIdMethod();
    }


    public static void writeTestStart(Thread thread, String testName) {
        getLog(thread).writeTestStart(testName);
    }
    protected void writeTestStart(String testName) {
        deep = 0;
        currentTest =  testName;
        resetIdMethod();
    }

    public static void writeTestFinish(Thread thread) {
        getLog(thread).writeTestFinish();

    }
    public  void writeTestFinish() {
        if(currentTest != null) {
            testCoverages.add(new Coverage(currentTest, idToMethod));
            resetIdMethod();
        }

    }

    public void loadInfo() throws IOException {
        idToMethod = new HashMap<Integer, MethodCoverage>();
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

    }

    protected void resetIdMethod() {
        for(Integer key : idToMethod.keySet()) {
            MethodCoverage mc = idToMethod.get(key);
            if(mc.getAllPath().size() != 0) {
                idToMethod.put(key, new MethodCoverage(Pool.get(key), mc.getMethodName(), mc.getAllBranchId()));
            }
        }
    }

    public static void setLogDir(File dir) {
        directory = dir;
    }
}
