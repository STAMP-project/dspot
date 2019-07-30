package eu.stamp_project.prettifier.context2name;

import eu.stamp_project.prettifier.options.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.*;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Context2NameExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Context2NameExecutor.class);

    private static final String COMMAND_LINE = "python3 context2name.py --load ";

    private static final String PREDICT_ARGUMENT = " --predict";

    private final Process context2nameProcess;

    private final BufferedWriter writer;

    private final Future future;

    private final ExecutorService service;

    private final Context2NameRunnableProcess task;

    private PrintStream output;

    private ByteArrayOutputStream outStream;

    /**
     * Construct the Context2NameExecutor.
     * This class will initialize the model of Context2Name, then provide an API to predict a name for a test method.
     */
    public Context2NameExecutor() {
        final String root = InputConfiguration.get().getPathToRootOfContext2Name();
        final String pathToModel = InputConfiguration.get().getRelativePathToModelForContext2Name();

        this.service = Executors.newSingleThreadExecutor();
        try {
            final String command = COMMAND_LINE + pathToModel + PREDICT_ARGUMENT;
            this.context2nameProcess = Runtime.getRuntime().exec(command, (String[]) null, new File(root));
            LOGGER.info("Executing: {} in {}", command, root);
        } catch (IOException var12) {
            throw new RuntimeException(var12);
        }

        this.outStream = new ByteArrayOutputStream();
        this.output = new PrintStream(outStream);
        this.task = new Context2NameRunnableProcess(this.context2nameProcess, this.output);
        this.future = this.service.submit(this.task);
        this.writer = new BufferedWriter(new OutputStreamWriter(this.context2nameProcess.getOutputStream()));
        try {
            LOGGER.info("Waiting {} seconds that context2name is well initialized...", InputConfiguration.get().getTimeToWaitForContext2nameInMillis() / 1000);
            Thread.sleep(InputConfiguration.get().getTimeToWaitForContext2nameInMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            LOGGER.info("Writing go to the stdin of the context2name's process");
            this.outStream.reset();
            this.writer.write("go" + AmplificationHelper.LINE_SEPARATOR);
            this.writer.flush();
            try {
                LOGGER.info("Waiting 5 seconds that context2name is doing its job...");
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getOutput() {
        return this.outStream.toString();
    }

    public void stop() {
        try {
            this.future.cancel(true);
        } catch (Exception var11) {
            throw new RuntimeException(var11);
        } finally {
            if (this.context2nameProcess != null) {
                this.context2nameProcess.destroyForcibly();
            }
            this.future.cancel(true);
            this.service.shutdownNow();
        }
    }

    static class Context2NameRunnableProcess implements Runnable {

        private Process process;

        private PrintStream output;

        public Context2NameRunnableProcess(Process process, PrintStream output) {
            this.process = process;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                (new Context2NameThread(this.output, this.process.getInputStream())).start();
                (new Context2NameThread(System.err, this.process.getErrorStream())).start();
                this.process.waitFor();
            } catch (Exception var2) {
                throw new RuntimeException(var2);
            }
        }
    }

    static class Context2NameThread extends Thread {

        private final PrintStream output;
        private final InputStream input;

        Context2NameThread(PrintStream output, InputStream input) {
            this.output = output;
            this.input = input;
        }

        public synchronized void start() {
            while (true) {
                try {
                    int read;
                    if ((read = this.input.read()) != -1) {
                        this.output.print((char) read);
                        continue;
                    }
                } catch (Exception var6) {

                } finally {
                    this.interrupt();
                }

                return;
            }
        }
    }
}
