package fr.inria.diversify.info.logger;



import java.io.*;
import java.util.*;


public class LogWriter {
    private PrintWriter fileWriter;


    private boolean log = true;

    //Thread containing the test
    private final Thread thread;

    //current deep in the heap
    private int deep;


    ///Directory where the log is being stored
    protected File dir = null;

    protected static Map<String, Set<String>> stmtTest = new HashMap<String, Set<String>>();
    protected String currentTest;
    /**
     * Constructor for the logger
     */
    public LogWriter(Thread thread, File logDir) {
        dir = logDir;
        if (dir == null) {
            initDir();
        }
        initOptions();

        ShutdownHookLog shutdownHook = new ShutdownHookLog();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        this.thread = thread;
    }

    protected void initOptions() {
        try {
            File propertiesFile = new File(dir.getAbsolutePath() + "/options");
            if(propertiesFile.exists()) {
                Properties properties = new Properties();
                properties.load(new FileInputStream(propertiesFile));

            }
        } catch (IOException e) {
            System.err.println("fr.inria.logger: error with properties file");
        }
    }

    protected String propertiesOrGetDefault(Properties properties, String key, String defaultValue) {
        if(properties.containsKey(key)) {
            return (String) properties.get(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets the loggin path for the current thread
     *
     * @param thread Thread to log
     * @return The path with the log file
     */
    public String getThreadLogFilePath(Thread thread) {
        return dir.getAbsolutePath() + "/" + getThreadFileName(thread);
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

    /**
     * Returns the file name of the file where this thread's log is being stored
     *
     * @param thread
     * @return Relative filename of the file where this thread's log is being stored
     */
    protected String getThreadFileName(Thread thread) {
        return "log" + thread.getName();
    }

    //Thread containing the test
    public Thread getThread() {
        return thread;
    }

    protected synchronized PrintWriter getFileWriter() throws IOException, InterruptedException {
        if (fileWriter == null) {
            String fileName = getThreadLogFilePath(thread) + "_" + System.currentTimeMillis();
            fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
        }
        return fileWriter;
    }


    protected void startLogging() {log = true;}
    protected void stopLogging() {
        log = false;
    }

    public void close() {

    }

    public void logStmt(String stmtId) {
        if(currentTest != null) {
            synchronized (stmtTest) {
                if (!stmtTest.containsKey(stmtId)) {
                    stmtTest.put(stmtId, new HashSet<String>());
                }
                stmtTest.get(stmtId).add(currentTest);
            }
        }
    }

    public void writeTestStart(String testName) {
        currentTest = testName;
    }

    public void writeTestFinish() {
        currentTest = null;
    }
}
