package fr.inria.diversify.sosiefier.util;

/**
 * User: Simon
 * Date: 9/4/13
 * Time: 2:40 PM
 */

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

@Deprecated
public class Log {

    private static boolean GET_CALLER_CLASS_SUN_AVAILABLE = false;
    private static boolean printCaller = false;

    static public void setPrintCaller(Boolean addCaller) {
        if (addCaller) {
            try {
                sun.reflect.Reflection.getCallerClass(2);
                GET_CALLER_CLASS_SUN_AVAILABLE = addCaller;
                printCaller = addCaller;
            } catch (NoClassDefFoundError e) {
            } catch (NoSuchMethodError e) {
            } catch (Throwable e) {
                System.err.println("Unexpected exception while initializing Sun Caller");
                e.printStackTrace();
            }
        } else {
            printCaller = false;
            GET_CALLER_CLASS_SUN_AVAILABLE = false;
        }
    }


    /**
     * No logging at all.
     */
    static public final int LEVEL_NONE = 6;
    /**
     * Critical errors. The application may no longer work correctly.
     */
    static public final int LEVEL_ERROR = 5;
    /**
     * Important warnings. The application will continue to work correctly.
     */
    static public final int LEVEL_WARN = 4;
    /**
     * Informative messages. Typically used for deployment.
     */
    static public final int LEVEL_INFO = 3;
    /**
     * Debug messages. This level is useful during development.
     */
    static public final int LEVEL_DEBUG = 2;
    /**
     * Trace messages. A lot of information is logged, so this level is usually only needed when debugging a problem.
     */
    static public final int LEVEL_TRACE = 1;

    /**
     * The level of messages that will be logged. Compiling this and the booleans below as "final" will cause the compiler to
     * remove all "if (Log.info) ..." type statements below the set level.
     */
    static private int level = LEVEL_INFO;

    /**
     * True when the ERROR level will be logged.
     */
    static public boolean ERROR = level <= LEVEL_ERROR;
    /**
     * True when the WARN level will be logged.
     */
    static public boolean WARN = level <= LEVEL_WARN;
    /**
     * True when the INFO level will be logged.
     */
    static public boolean INFO = level <= LEVEL_INFO;
    /**
     * True when the DEBUG level will be logged.
     */
    static public boolean DEBUG = level <= LEVEL_DEBUG;
    /**
     * True when the TRACE level will be logged.
     */
    static public boolean TRACE = level <= LEVEL_TRACE;

    /**
     * Sets the level to log. If a version of this class is being used that has a final log level, this has no affect.
     */
    static public void set(int level) {
        // Comment out method contents when compiling fixed level JARs.
        Log.level = level;
        ERROR = level <= LEVEL_ERROR;
        WARN = level <= LEVEL_WARN;
        INFO = level <= LEVEL_INFO;
        DEBUG = level <= LEVEL_DEBUG;
        TRACE = level <= LEVEL_TRACE;
    }

    static public void NONE() {
        set(LEVEL_NONE);
    }

    static public void ERROR() {
        set(LEVEL_ERROR);
    }

    static public void WARN() {
        set(LEVEL_WARN);
    }

    static public void INFO() {
        set(LEVEL_INFO);
    }

    static public void DEBUG() {
        set(LEVEL_DEBUG);
    }

    static public void TRACE() {
        set(LEVEL_TRACE);
    }

    /**
     * Sets the logger that will write the log messages.
     */
    static public void setLogger(Logger logger) {
        Log.logger = logger;
    }

    private Log() {

    }

    private Log(Logger plogger) {
        logger = plogger;
    }

    static public Log getLog(String category) {
        Logger newLogger = new Logger();
        newLogger.setCategory(category);
        return new Log(newLogger);
    }

    static private Logger logger = new Logger();

    private static final char beginParam = '{';
    private static final char endParam = '}';

    private static String processMessage(String message, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (p1 == null) {
            return message;
        }
        StringBuilder buffer = null;
        boolean previousCharfound = false;
        int param = 0;
        for (int i = 0; i < message.length(); i++) {
            char currentChar = message.charAt(i);
            if (previousCharfound) {
                if (currentChar == endParam) {
                    param++;
                    switch (param) {
                        case 1: {
                            buffer = new StringBuilder();
                            buffer.append(message.substring(0, i - 1));
                            buffer.append(p1);
                        }
                        break;
                        case 2: {
                            buffer.append(p2);
                        }
                        break;
                        case 3: {
                            buffer.append(p3);
                        }
                        break;
                        case 4: {
                            buffer.append(p4);
                        }
                        break;
                        case 5: {
                            buffer.append(p5);
                        }
                        break;
                    }
                    previousCharfound = false;
                } else {
                    if (buffer != null) {
                        message.charAt(i - 1);
                        buffer.append(currentChar);
                    }
                    previousCharfound = false;
                }
            } else {
                if (currentChar == beginParam) {
                    previousCharfound = true; //next round
                } else {
                    if (buffer != null) {
                        buffer.append(currentChar);
                    }
                }
            }
        }
        if (buffer != null) {
            return buffer.toString();
        } else {
            return message;
        }
    }

    static public void error(String message) {
        if (ERROR) logger.log(LEVEL_ERROR, message, null);
    }

    static public void error(String message, Throwable ex) {
        if (ERROR) logger.log(LEVEL_ERROR, message, ex);
    }

    static public void error(String message, Throwable ex, Object p1) {
        if (ERROR) {
            error(processMessage(message, p1, null, null, null, null), ex);
        }
    }

    static public void error(String message, Throwable ex, Object p1, Object p2) {
        if (ERROR) {
            error(processMessage(message, p1, p2, null, null, null), ex);
        }
    }

    static public void error(String message, Throwable ex, Object p1, Object p2, Object p3) {
        if (ERROR) {
            error(processMessage(message, p1, p2, p3, null, null), ex);
        }
    }

    static public void error(String message, Throwable ex, Object p1, Object p2, Object p3, Object p4) {
        if (ERROR) {
            error(processMessage(message, p1, p2, p3, p4, null), ex);
        }
    }

    static public void error(String message, Throwable ex, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (ERROR) {
            error(processMessage(message, p1, p2, p3, p4, p5), ex);
        }
    }

    static public void error(String message, Object p1) {
        if (ERROR) {
            error(processMessage(message, p1, null, null, null, null), null);
        }
    }

    static public void error(String message, Object p1, Object p2) {
        if (ERROR) {
            error(processMessage(message, p1, p2, null, null, null), null);
        }
    }

    static public void error(String message, Object p1, Object p2, Object p3) {
        if (ERROR) {
            error(processMessage(message, p1, p2, p3, null, null), null);
        }
    }

    static public void error(String message, Object p1, Object p2, Object p3, Object p4) {
        if (ERROR) {
            error(processMessage(message, p1, p2, p3, p4, null), null);
        }
    }

    static public void error(String message, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (ERROR) {
            error(processMessage(message, p1, p2, p3, p4, p5), null);
        }
    }


    /* WARN */
    static public void warn(String message, Throwable ex) {
        if (WARN) logger.log(LEVEL_WARN, message, ex);
    }

    static public void warn(String message) {
        if (WARN) logger.log(LEVEL_WARN, message, null);
    }

    static public void warn(String message, Throwable ex, Object p1) {
        if (WARN) {
            warn(processMessage(message, p1, null, null, null, null), ex);
        }
    }

    static public void warn(String message, Throwable ex, Object p1, Object p2) {
        if (WARN) {
            warn(processMessage(message, p1, p2, null, null, null), ex);
        }
    }

    static public void warn(String message, Throwable ex, Object p1, Object p2, Object p3) {
        if (WARN) {
            warn(processMessage(message, p1, p2, p3, null, null), ex);
        }
    }

    static public void warn(String message, Throwable ex, Object p1, Object p2, Object p3, Object p4) {
        if (WARN) {
            warn(processMessage(message, p1, p2, p3, p4, null), ex);
        }
    }

    static public void warn(String message, Throwable ex, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (WARN) {
            warn(processMessage(message, p1, p2, p3, p4, p5), ex);
        }
    }

    static public void warn(String message, Object p1) {
        if (WARN) {
            warn(processMessage(message, p1, null, null, null, null), null);
        }
    }

    static public void warn(String message, Object p1, Object p2) {
        if (WARN) {
            warn(processMessage(message, p1, p2, null, null, null), null);
        }
    }

    static public void warn(String message, Object p1, Object p2, Object p3) {
        if (WARN) {
            warn(processMessage(message, p1, p2, p3, null, null), null);
        }
    }

    static public void warn(String message, Object p1, Object p2, Object p3, Object p4) {
        if (WARN) {
            warn(processMessage(message, p1, p2, p3, p4, null), null);
        }
    }

    static public void warn(String message, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (WARN) {
            warn(processMessage(message, p1, p2, p3, p4, p5), null);
        }
    }

    /* INFO */
    static public void info(String message, Throwable ex) {
        if (INFO) logger.log(LEVEL_INFO, message, ex);
    }

    static public void info(String message) {
        if (INFO) logger.log(LEVEL_INFO, message, null);
    }

    static public void info(String message, Throwable ex, Object p1) {
        if (INFO) {
            info(processMessage(message, p1, null, null, null, null), ex);
        }
    }

    static public void info(String message, Throwable ex, Object p1, Object p2) {
        if (INFO) {
            info(processMessage(message, p1, p2, null, null, null), ex);
        }
    }

    static public void info(String message, Throwable ex, Object p1, Object p2, Object p3) {
        if (INFO) {
            info(processMessage(message, p1, p2, p3, null, null), ex);
        }
    }

    static public void info(String message, Throwable ex, Object p1, Object p2, Object p3, Object p4) {
        if (INFO) {
            info(processMessage(message, p1, p2, p3, p4, null), ex);
        }
    }

    static public void info(String message, Throwable ex, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (INFO) {
            info(processMessage(message, p1, p2, p3, p4, p5), ex);
        }
    }

    static public void info(String message, Object p1) {
        if (INFO) {
            info(processMessage(message, p1, null, null, null, null), null);
        }
    }

    static public void info(String message, Object p1, Object p2) {
        if (INFO) {
            info(processMessage(message, p1, p2, null, null, null), null);
        }
    }

    static public void info(String message, Object p1, Object p2, Object p3) {
        if (INFO) {
            info(processMessage(message, p1, p2, p3, null, null), null);
        }
    }

    static public void info(String message, Object p1, Object p2, Object p3, Object p4) {
        if (INFO) {
            info(processMessage(message, p1, p2, p3, p4, null), null);
        }
    }

    static public void info(String message, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (INFO) {
            info(processMessage(message, p1, p2, p3, p4, p5), null);
        }
    }


    /* DEBUG */
    static public void debug(String message, Throwable ex) {
        if (DEBUG) logger.log(LEVEL_DEBUG, message, ex);
    }

    static public void debug(String message) {
        if (DEBUG) logger.log(LEVEL_DEBUG, message, null);
    }

    static public void debug(String message, Throwable ex, Object p1) {
        if (DEBUG) {
            debug(processMessage(message, p1, null, null, null, null), ex);
        }
    }

    static public void debug(String message, Throwable ex, Object p1, Object p2) {
        if (DEBUG) {
            debug(processMessage(message, p1, p2, null, null, null), ex);
        }
    }

    static public void debug(String message, Throwable ex, Object p1, Object p2, Object p3) {
        if (DEBUG) {
            debug(processMessage(message, p1, p2, p3, null, null), ex);
        }
    }

    static public void debug(String message, Throwable ex, Object p1, Object p2, Object p3, Object p4) {
        if (DEBUG) {
            debug(processMessage(message, p1, p2, p3, p4, null), ex);
        }
    }

    static public void debug(String message, Throwable ex, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (DEBUG) {
            debug(processMessage(message, p1, p2, p3, p4, p5), ex);
        }
    }

    static public void debug(String message, Object p1) {
        if (DEBUG) {
            debug(processMessage(message, p1, null, null, null, null), null);
        }
    }

    static public void debug(String message, Object p1, Object p2) {
        if (DEBUG) {
            debug(processMessage(message, p1, p2, null, null, null), null);
        }
    }

    static public void debug(String message, Object p1, Object p2, Object p3) {
        if (DEBUG) {
            debug(processMessage(message, p1, p2, p3, null, null), null);
        }
    }

    static public void debug(String message, Object p1, Object p2, Object p3, Object p4) {
        if (DEBUG) {
            debug(processMessage(message, p1, p2, p3, p4, null), null);
        }
    }

    static public void debug(String message, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (DEBUG) {
            debug(processMessage(message, p1, p2, p3, p4, p5), null);
        }
    }

    static public void trace(String message, Throwable ex) {
        if (TRACE) logger.log(LEVEL_TRACE, message, ex);
    }

    static public void trace(String message) {
        if (TRACE) logger.log(LEVEL_TRACE, message, null);
    }

    static public void trace(String message, Throwable ex, Object p1) {
        if (TRACE) {
            trace(processMessage(message, p1, null, null, null, null), ex);
        }
    }

    static public void trace(String message, Throwable ex, Object p1, Object p2) {
        if (TRACE) {
            trace(processMessage(message, p1, p2, null, null, null), ex);
        }
    }

    static public void trace(String message, Throwable ex, Object p1, Object p2, Object p3) {
        if (TRACE) {
            trace(processMessage(message, p1, p2, p3, null, null), ex);
        }
    }

    static public void trace(String message, Throwable ex, Object p1, Object p2, Object p3, Object p4) {
        if (TRACE) {
            trace(processMessage(message, p1, p2, p3, p4, null), ex);
        }
    }

    static public void trace(String message, Throwable ex, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (TRACE) {
            trace(processMessage(message, p1, p2, p3, p4, p5), ex);
        }
    }

    static public void trace(String message, Object p1) {
        if (TRACE) {
            trace(processMessage(message, p1, null, null, null, null), null);
        }
    }

    static public void trace(String message, Object p1, Object p2) {
        if (TRACE) {
            trace(processMessage(message, p1, p2, null, null, null), null);
        }
    }

    static public void trace(String message, Object p1, Object p2, Object p3) {
        if (TRACE) {
            trace(processMessage(message, p1, p2, p3, null, null), null);
        }
    }

    static public void trace(String message, Object p1, Object p2, Object p3, Object p4) {
        if (TRACE) {
            trace(processMessage(message, p1, p2, p3, p4, null), null);
        }
    }

    static public void trace(String message, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (TRACE) {
            trace(processMessage(message, p1, p2, p3, p4, p5), null);
        }
    }


    /**
     * Performs the actual logging. Default implementation logs to System.out. Extended and use {@link Log#logger} set to handle
     * logging differently.
     */
    static public class Logger {
        private long firstLogTime = new Date().getTime();
        private static final String error_msg = " ERROR: ";
        private static final String warn_msg = " WARN: ";
        private static final String info_msg = " INFO: ";
        private static final String debug_msg = " DEBUG: ";
        private static final String trace_msg = " TRACE: ";

        private String category = null;

        public void setCategory(String category) {
            this.category = category;
        }

        public void log(int level, String message, Throwable ex) {
            StringBuilder builder = new StringBuilder(256);
            long time = new Date().getTime() - firstLogTime;
            long minutes = time / (1000 * 60);
            long seconds = time / (1000) % 60;
            if (minutes <= 9) builder.append('0');
            builder.append(minutes);
            builder.append(':');
            if (seconds <= 9) builder.append('0');
            builder.append(seconds);
            switch (level) {
                case LEVEL_ERROR:
                    builder.append(error_msg);
                    break;
                case LEVEL_WARN:
                    builder.append(warn_msg);
                    break;
                case LEVEL_INFO:
                    builder.append(info_msg);
                    break;
                case LEVEL_DEBUG:
                    builder.append(debug_msg);
                    break;
                case LEVEL_TRACE:
                    builder.append(trace_msg);
                    break;
            }

            if (printCaller && GET_CALLER_CLASS_SUN_AVAILABLE) {
                String callerName = sun.reflect.Reflection.getCallerClass(3).getName();
                builder.append(callerName);
                builder.append(':');
                builder.append(' ');
            }

            if (category != null) {
                builder.append('[');
                builder.append(category);
                builder.append("] ");
            }
            builder.append(message);
            if (ex != null) {
                StringWriter writer = new StringWriter(256);
                ex.printStackTrace(new PrintWriter(writer));
                builder.append('\n');
                builder.append(writer.toString().trim());
            }
            print(builder.toString());
        }

        /**
         * Prints the message to System.out. Called by the default implementation of {@link #log(int, String, Throwable)}.
         */
        protected void print(String message) {
            System.out.println(message);
        }
    }
}
