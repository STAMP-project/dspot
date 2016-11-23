package fr.inria.diversify.dspot.dynamic.logger;


import java.io.*;
import java.util.*;

/**
 * User: Simon
 * Date: 17/03/16
 * Time: 15:01
 */
public class  LogWriter {
    protected List<MethodCall> currentMethods;
    protected List<Env> environments;
    protected Set<Integer> methodCallsLog;
    protected int deep;
    protected Map<String, Integer> methodCallCount;


    //Directory where the log is being stored
    protected File dir = null;
    protected PrintWriter fileWriter;
    protected Thread thread;

    public LogWriter(Thread thread, File logDir) {
        this.thread = thread;
        dir = logDir;
        if (dir == null) {
            initDir();
        }
        methodCallsLog = new HashSet<Integer>();
        currentMethods = new LinkedList<MethodCall>();
        environments = new LinkedList<Env>();
        methodCallCount = new HashMap<String, Integer>();

        ShutdownHookLog shutdownHook = new ShutdownHookLog();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public void startLog(String methodId, Object... params) {
        MethodCall mc = new MethodCall(methodId, deep, params);

        Map<Object, Set<Object>> env = new IdentityHashMap<Object, Set<Object>>();
        for(Object object : mc.getObjectParameters()) {
            Set<Object> set = new HashSet<Object>();
            set.add(object);
            env.put(object, set);
        }
        currentMethods.add(0, mc);
        environments.add(0, new Env());

        deep++;
    }

    public void stopLog(String methodId, Object target) {
        deep--;
        try {
            if (!currentMethods.isEmpty()) {
                MethodCall methodCall = currentMethods.get(0);
                if (methodCall.sameMethod(methodId, deep)) {
                    methodCall.setTarget(target);
                    writeCandidate(methodCall);
                }
                environments.remove(0);
                currentMethods.remove(0);
            }
        } catch (Throwable e) {}
    }

    public void writeField(String methodId, Object receiver, String fieldId) {
        if(!currentMethods.isEmpty()) {
            Env env = environments.get(0);
            env.add(receiver, fieldId);
        }
    }

    public void readField(String methodId, Object receiver, String fieldId) {
        try {
            //cas ou le receiver est un parametre que l'on peut serializer, on ne cherche que l'objet dans dans environement

            if (//receiver != null &&
                    !currentMethods.isEmpty()) {
                int count = 0;
                for (Env env : environments) {
                    if (env.contains(receiver, fieldId)) {
                        break;
                    }
                    count++;
                }
                if (count == currentMethods.size()) {
                    currentMethods.clear();
                    environments.clear();
                } else {
                    Env env = environments.get(environments.size() - 1);
                    for (int i = 0; i < count; i++) {
                        currentMethods.remove(0);
                        env.addSubEnv(environments.remove(0));
                    }
                }
            }
        } catch (Throwable e) {
            currentMethods.clear();
            environments.clear();
        }
    }

    protected PrimitiveCache primitiveCache;

    public PrimitiveCache getPrimitiveCache() {
        if(primitiveCache == null) {
            primitiveCache = new PrimitiveCache();
        }
        return primitiveCache;
    }

    public void logPrimitive(int methodId, int constructorId, int argIndex, Object value) {
        try {
            if(!getPrimitiveCache().alreadyLog(constructorId, argIndex, value)) {
                PrintWriter fileWriter = getFileWriter();
                StringBuilder s = new StringBuilder();
                s.append(KeyWord.primitiveKeyWord)
                        .append(KeyWord.simpleSeparator)
                        .append(methodId)
                        .append(KeyWord.simpleSeparator)
                        .append(constructorId)
                        .append(KeyWord.simpleSeparator)
                        .append(argIndex)
                        .append(KeyWord.simpleSeparator)
                        .append(value)
                        .append(KeyWord.endLine);
                fileWriter.append(s.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    protected void writeCandidate(MethodCall methodCall) {
        if(methodCallsLog.add(methodCall.hashCode())) {
            if(methodCallCount.containsKey(methodCall.getMethod())) {
                methodCallCount.put(methodCall.getMethod(), methodCallCount.get(methodCall.getMethod()) + 1);
            } else {
                methodCallCount.put(methodCall.getMethod(), 1);
            }

            if(methodCallCount.get(methodCall.getMethod()) < 50) {
                try {
                    PrintWriter fileWriter = getFileWriter();
                    fileWriter.write(methodCall.toString());
                } catch (Exception e) {}
            }
        }
    }
    public void close() {
        if(fileWriter != null) {
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
        return "log" + thread.getName().replace("/", ".");
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

    protected synchronized PrintWriter getFileWriter() throws IOException, InterruptedException {
        if (fileWriter == null) {
            String fileName = getThreadLogFilePath(thread) + "_" + System.currentTimeMillis();
            fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
        }
        return fileWriter;
    }
}
