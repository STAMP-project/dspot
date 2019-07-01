package eu.stamp_project.prettifier.context2code;

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
public class Context2CodeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Context2CodeExecutor.class);

    private static final String COMMAND_LINE = "python3 context2code.py --load ";

    private static final String PREDICT_ARGUMENT = " --predict";

    private final Process context2codeProcess;

    private final BufferedWriter writer;

    private final Future future;

    private final ExecutorService service;

    private final Context2CodeRunnableProcess task;

    private PrintStream output;

    private ByteArrayOutputStream outStream;

    /**
     * Construct the Context2CodeExecutor.
     * This class will initialize the model of Context2Code, then provide an API to predict a name for a test method.
     */
    public Context2CodeExecutor() {
        final String root = InputConfiguration.get().getPathToRootOfContext2Code();
        final String pathToModel = InputConfiguration.get().getRelativePathToModelForContext2Code();

        this.service = Executors.newSingleThreadExecutor();
        try {
            final String command = COMMAND_LINE + pathToModel + PREDICT_ARGUMENT;
            this.context2codeProcess = Runtime.getRuntime().exec(command, (String[]) null, new File(root));
            LOGGER.info("Executing: {} in {}", command, root);
        } catch (IOException var12) {
            throw new RuntimeException(var12);
        }

        this.outStream = new ByteArrayOutputStream();
        this.output = new PrintStream(outStream);
        this.task = new Context2CodeRunnableProcess(this.context2codeProcess, this.output);
        this.future = this.service.submit(this.task);
        this.writer = new BufferedWriter(new OutputStreamWriter(this.context2codeProcess.getOutputStream()));
        try {
            LOGGER.info("Waiting {} seconds that context2code is well initialized...", InputConfiguration.get().getTimeToWaitForContext2codeInMillis() / 1000);
            Thread.sleep(InputConfiguration.get().getTimeToWaitForContext2codeInMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            LOGGER.info("Writing go to the stdin of the context2code's process");
            this.outStream.reset();
            this.writer.write("go" + AmplificationHelper.LINE_SEPARATOR);
            this.writer.flush();
            try {
                LOGGER.info("Waiting 5 seconds that context2code is doing its job...");
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
            if (this.context2codeProcess != null) {
                this.context2codeProcess.destroyForcibly();
            }
            this.future.cancel(true);
            this.service.shutdownNow();
        }
    }

}
