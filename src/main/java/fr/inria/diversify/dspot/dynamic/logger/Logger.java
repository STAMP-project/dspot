package fr.inria.diversify.dspot.dynamic.logger;



import java.io.File;
import java.util.HashMap;

/**
 * User: Simon
 * Date: 21/03/16
 * Time: 11:35
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

    public static void readField(Thread thread, String methodId, Object receiver, String fieldId) {
//        getLog(thread).readField(methodId, receiver, fieldId);
    }

    public static void writeField(Thread thread, String methodId, Object receiver, String fieldId) {
//        getLog(thread).writeField(methodId, receiver, fieldId);
    }

    public static void startLog(Thread thread, String methodId, Object... params) {
        getLog(thread).startLog(methodId, params);
    }

    public static void stopLog(Thread thread, String methodId, Object receiver) {
        getLog(thread).stopLog(methodId, receiver);
    }

    public static void logPrimitive(Thread thread, int methodId, int constructorId, int argIndex, Object value) {
        getLog(thread).logPrimitive(methodId, constructorId, argIndex, value);
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
}
