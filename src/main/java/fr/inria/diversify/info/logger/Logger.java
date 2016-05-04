package fr.inria.diversify.info.logger;


import java.io.File;
import java.util.HashMap;

/**
 * User: Simon
 * Date: 15/04/15
 */
public class Logger {
    private static StaticLogWriter staticLogWriter =  new StaticLogWriter();
    private static HashMap<Thread, LogWriter> logs = null;
    private static File logDir;
    /**
     * This is an option. By the default the verbose log is used.
     * @param log
     */
    public static void setLog(HashMap<Thread, LogWriter> log) {
        Logger.logs = log;
    }

    protected static LogWriter getLog() {
        return getLog(Thread.currentThread());
    }

    protected static LogWriter getLog(Thread thread) {
        if ( logs == null ) {
            logs = new HashMap<Thread, LogWriter>(); }
        if ( logs.containsKey(thread) ) {
            return logs.get(thread);
        } else {
            LogWriter l = new LogWriter(thread, logDir);
            logs.put(thread, l);
            return l;
        }
    }

    public static void logStmt(Thread thread, String logStmt) {
        staticLogWriter.logStmt(logStmt);
        getLog(thread).logStmt(logStmt);
    }

    public static void writeTestStart(Thread thread, Object receiver, String testName) {
        getLog(thread).writeTestStart(receiver.getClass().getCanonicalName() + "." + testName);
    }

    public static void writeTestStart(Thread thread, String testName) {
        getLog(thread).writeTestStart(testName);
    }

    public static void writeTestFinish(Thread thread) {
        getLog(thread).writeTestFinish();
    }

    public static void close() {
        if(logs != null) {
            for (LogWriter l : logs.values()) {
                l.close();
            }
        }
    }

    public static void setLogDir(File dir) {
        logDir = dir;
        StaticLogWriter.dir = dir;
    }

    public static void reset() {
        if(logs != null) {
            for(LogWriter logWriter: logs.values()) {
                logWriter.startLogging();
            }
            logs.clear();
        }
    }

    public static void stopLogging() {
        if ( logs != null ) {
            for(LogWriter logWriter: logs.values()) {
                logWriter.stopLogging();
            }
        }
    }

}
