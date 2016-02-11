package fr.inria.diversify.profiling.logger;


import java.io.File;
import java.util.HashMap;

/**
 * User: Simon
 * Date: 15/04/15
 */
public class Logger {
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
        if ( logs == null ) { logs = new HashMap<Thread, LogWriter>(); }
        if ( logs.containsKey(thread) ) {
            return logs.get(thread);
        } else {
            LogWriter l = new LogWriter(thread, logDir);
            logs.put(thread, l);
            return l;
        }
    }

    public static void logAssertArgument(Thread thread, int idAssert, Object invocation) {
        getLog(thread).logAssertArgument(idAssert,invocation);
    }

    public static void branch(Thread thread, String id) {
        getLog(thread).branch(id);
    }

    public static void logTransformation(Thread thread, String id) {
        getLog(thread).logTransformation(id);
    }

    public static void methodIn(Thread thread, String id) {
        getLog(thread).methodIn(id);
    }

    public static void methodOut(Thread thread, String id) {
        getLog(thread).methodOut(id);
    }

    public static void writeField(Thread thread, String methodId, String varId, Object var) {
        getLog(thread).writeVar(methodId, varId, var);
    }

    public static void writeTestStart(Thread thread, Object receiver, String testName) {
        getLog(thread).writeTestStart(testName, receiver);
    }

    public static void writeTestStart(Thread thread, String testName) {
        getLog(thread).writeTestStart(testName);
    }

    public static void writeTestFinish(Thread thread) {
        getLog(thread).writeTestFinish();
    }

    public static void writeCatch(Thread thread, String methodId, String localPositionId, Object exception) {
        getLog(thread).writeCatch(methodId, localPositionId, exception);
    }

    public static void writeThrow(Thread thread, String methodId, String localPositionId, Object exception) {
        getLog(thread).writeThrow(methodId, localPositionId, exception);
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
    }

    public static void reset() {
        if(logs != null) {
            logs.clear();
        }
    }

}
