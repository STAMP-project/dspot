package fr.inria.diversify.info.logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Simon
 * Date: 29/04/16
 * Time: 15:42
 */
public class StaticLogWriter {
    ///Directory where the log is being stored
    protected static File dir;

    protected static PrintWriter fileWriter;

    protected static Map<String, Integer> stmtExecutionCount = new HashMap<String, Integer>();

    public StaticLogWriter() {
        if (dir == null) {
            initDir();
        }
        StaticShutdownHookLog shutdownHook = new StaticShutdownHookLog();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * Initializes the directory where the files for each thread are going to be stored
     */
    protected void initDir() {
        if(dir == null) {
            String logDirName = "log";
            dir = new File(logDirName);
            while (!isLogDir(dir)) {
                logDirName = "../" + logDirName;
                dir = new File(logDirName);
            }
        }
    }

    protected boolean isLogDir(File dir) {
        if(dir.exists()) {
            for(File fileInDir : dir.listFiles()) {
                if(fileInDir.getName().equals("info")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void logStmt(String stmtId) {
        synchronized (stmtExecutionCount) {
            if (!stmtExecutionCount.containsKey(stmtId)) {
                stmtExecutionCount.put(stmtId, 0);
            }
            stmtExecutionCount.put(stmtId, stmtExecutionCount.get(stmtId) + 1);
        }
    }

    public static void close(){
        try {
            getFileWriter();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(fileWriter != null) {
            fileWriter.write("id;nbExec;nbTest\n");
            for(String stmtId : stmtExecutionCount.keySet()) {
                fileWriter.write(stmtId + ";" + stmtExecutionCount.get(stmtId));
                if(LogWriter.stmtTest.containsKey(stmtId)) {
                    fileWriter.write(";" + LogWriter.stmtTest.get(stmtId).size() + "\n");
                } else {
                    fileWriter.write(";0\n");
                }
            }
            fileWriter.close();
        }
    }

    protected static synchronized PrintWriter getFileWriter() throws IOException, InterruptedException {
        if (fileWriter == null) {
            String fileName = dir.getAbsolutePath() + "/staticLog_" + System.currentTimeMillis();
            fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
        }
        return fileWriter;
    }

}
