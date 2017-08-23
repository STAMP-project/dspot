package fr.inria.diversify.logger;


import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class LogWriter {
    private boolean fullPath = false;
    private PrintWriter fileWriter;

    private Map<String, Integer> stmtCounts;

    private Map<Class, ClassObserver> classesObservers;

    private boolean log = true;

    private boolean isObserve = false;

    private boolean logMethodCall = true;

    private boolean writeVar = true;

    //Thread containing the test
    private final Thread thread;

    //current deep in the heap
    private int deep;

    private PathBuilder pathBuilder;

    ///Directory where the log is being stored
    protected File dir = null;

    protected boolean inTest = false;

    /**
     * Constructor for the logger
     */
    public LogWriter(Thread thread, File logDir) {
        dir = logDir;
        if (dir == null) {
            initDir();
        }
        initOptions();
        pathBuilder = new PathBuilder(fullPath);
        classesObservers = new HashMap<Class, ClassObserver>();
        stmtCounts = new HashMap<String, Integer>();

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
                fullPath = Boolean.parseBoolean(propertiesOrGetDefault(properties,"fullPath", "false"));
                logMethodCall = Boolean.parseBoolean(propertiesOrGetDefault(properties,"logMethodCall", "true"));
                writeVar = Boolean.parseBoolean(propertiesOrGetDefault(properties,"writeVar", "true"));
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

    public void close(){
        if(fileWriter != null) {
            fileWriter.append(KeyWord.endLine);
            fileWriter.append("\n");

            synchronized (fileWriter) {
                for (String id : stmtCounts.keySet()) {
                    StringBuilder sb = new StringBuilder();

                    sb.append(KeyWord.statementLog);
                    sb.append(KeyWord.simpleSeparator);
                    sb.append(id);
                    sb.append(KeyWord.simpleSeparator);
                    sb.append(stmtCounts.get(id) + "");
                    sb.append(KeyWord.endLine);
                    sb.append("\n");

                    fileWriter.append(sb.toString());
                }
            }
            fileWriter.close();
        }
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

    public void branch(String id) {
        if(log && !isObserve && inTest) {
            pathBuilder.addbranch(id);
        }
    }

    public void methodIn(String methodId) {
        if(log && !isObserve && inTest) {
            deep++;
            if(logMethodCall) {
                try {
                    PrintWriter fileWriter = getFileWriter();
                    fileWriter.append(KeyWord.endLine);
                    fileWriter.append("\n");
                    fileWriter.append(KeyWord.methodCallObservation);
                    fileWriter.append(KeyWord.simpleSeparator);
                    fileWriter.append(deep + "");
                    fileWriter.append(KeyWord.simpleSeparator);
                    fileWriter.append(methodId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            pathBuilder.newPath();
        }
    }

    public void methodOut(String id) {
        if(log && !isObserve && inTest) {
            try {
                pathBuilder.printPath(id, deep, getFileWriter());
            } catch (Exception e) {}
            deep--;
        }
    }

    public void testIn(String testName, Object receiver) {
        testIn(receiver.getClass().getCanonicalName() + "." + testName);
    }

    public void testIn(String testName) {
        inTest = true;
        if(log && !isObserve) {
            try {
                PrintWriter fileWriter = getFileWriter();
                fileWriter.append(KeyWord.endLine);
                fileWriter.append("\n");
                fileWriter.append(KeyWord.testStartObservation);
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(testName);
            } catch (Exception e) {}
        }
    }

    public void testOut() {
        inTest = false;
        if(log && !isObserve) {
            try {
                pathBuilder.clear();
                PrintWriter fileWriter = getFileWriter();
                fileWriter.append(KeyWord.endLine);
                fileWriter.append("\n");
                fileWriter.append(KeyWord.testEndObservation);
            } catch (Exception e) {}
        }
    }


    public void beforeAssert(int methodId, int idAssert) {
        if(log && !isObserve) {
            try {
                PrintWriter fileWriter = getFileWriter();
                fileWriter.append(KeyWord.endLine);
                fileWriter.append("\n");
                fileWriter.append(KeyWord.assertBefore);
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(methodId + "");
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(idAssert + "");
            } catch (Exception e) {}
        }
    }

    public void afterAssert(int methodId, int idAssert) {
        if(log && !isObserve) {
            try {
                PrintWriter fileWriter = getFileWriter();
                fileWriter.append(KeyWord.endLine);
                fileWriter.append("\n");
                fileWriter.append(KeyWord.assertAfter);
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(methodId + "");
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(idAssert + "");
            } catch (Exception e) {}
        }
    }


    public void writeVar(String  methodId, Object... var) {
        if(log && !isObserve && writeVar) {
            isObserve = true;
            try {
                StringBuilder string = new StringBuilder();
                string.append(KeyWord.endLine);
                fileWriter.append("\n");
                string.append(KeyWord.variableObservation);
                string.append(KeyWord.simpleSeparator);
                string.append(deep + "");
                string.append(KeyWord.simpleSeparator);
                string.append(methodId);
//                string.append(KeyWord.simpleSeparator);
//                string.append(localPositionId);

                String varsString = buildVars(var);
                if(varsString.isEmpty())
                    return;

                string.append(varsString);

                PrintWriter fileWriter = getFileWriter();
                fileWriter.append(string.toString());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isObserve = false;
            }
        }
    }


    protected String buildVars(Object[] vars) {
        StringBuilder varsString = new StringBuilder();

        for (int i = 0; i < vars.length / 2; i = i + 2) {
            try {
                String varName = vars[i].toString();
                String value;
                if (vars[i + 1] == null) {
                    value = "null";
                } else {
                    value = vars[i + 1].toString();
                }
                if(value.length() > 1000) {
                    value = vars[i + 1].getClass().getCanonicalName() + value.length();
                }
                varsString.append(KeyWord.separator);
                varsString.append(varName);
                varsString.append(KeyWord.separator);
                varsString.append(value);
            } catch (Exception e) {
            }
        }
        return KeyWord.simpleSeparator + varsString.substring(KeyWord.separator.length(), varsString.length());
    }


    public void logAssertArgument(int idAssert, Object invocation) {
        if(log && !isObserve) {
            isObserve = true;
            try {
                StringBuilder string = new StringBuilder();
                string.append(KeyWord.endLine);
                fileWriter.append("\n");
                string.append(KeyWord.assertObservation);
                string.append(KeyWord.simpleSeparator);
                string.append(idAssert + "");

                PrintWriter fileWriter = getFileWriter();
                string.append(KeyWord.simpleSeparator);
                observe(invocation, fileWriter);

                fileWriter.append(string.toString());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isObserve = false;
            }
        }
    }

    protected void observe(Object object, PrintWriter writer) throws IOException, InterruptedException {
        Class objectClass;
        if(object == null) {
            objectClass = null;
        } else {
            objectClass = object.getClass();
        }
        if(!classesObservers.containsKey(objectClass)) {
            classesObservers.put(objectClass, new ClassObserver(objectClass));
        }
        classesObservers.get(objectClass).observe(object,writer);
    }

    protected synchronized PrintWriter getFileWriter() throws IOException, InterruptedException {
        if (fileWriter == null) {
            String fileName = getThreadLogFilePath(thread) + "_" + System.currentTimeMillis();
            fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
        }
        return fileWriter;
    }

    public void writeCatch(String methodId, String localPositionId, Object exception) {
        if(log && !isObserve) {
            isObserve = true;
            try {
                PrintWriter fileWriter = getFileWriter();

                fileWriter.append(KeyWord.endLine);
                fileWriter.append("\n");
                fileWriter.append(KeyWord.catchObservation);
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(deep + "");
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(methodId);
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(localPositionId);
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(exception.getClass().getCanonicalName());
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(exception.toString());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isObserve = false;
            }
        }
    }

    public void writeThrow(String methodId, String localPositionId, Object exception) {
        if(log && !isObserve) {
            isObserve = true;
            try {
                PrintWriter fileWriter = getFileWriter();

                fileWriter.append(KeyWord.endLine);
                fileWriter.append("\n");
                fileWriter.append(KeyWord.throwObservation);
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(deep + "");
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(methodId);
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(localPositionId);
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(exception.getClass().getCanonicalName());
                fileWriter.append(KeyWord.simpleSeparator);
                fileWriter.append(exception.toString());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isObserve = false;
            }
        }
    }

    protected void startLogging() {log = true;}
    protected void stopLogging() {
        log = false;
    }


    public void stmtLog(String id) {
        if(log && !isObserve) {
            if(!stmtCounts.containsKey(id)) {
                stmtCounts.put(id, 0);
            }
            stmtCounts.put(id, stmtCounts.get(id) + 1);
        }
    }
}
