package eu.stamp_project.prettifier.code2vec;

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
public class Code2VecExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Code2VecExecutor.class);

    private static final String COMMAND_LINE = "python3 code2vec.py --load ";

    private static final String PREDICT_ARGUMENT = " --predict";

    private final Process code2vecProcess;

    private final BufferedWriter writer;

    private final Future future;

    private final ExecutorService service;

    private final Code2VecRunnableProcess task;

    private PrintStream output;

    private ByteArrayOutputStream outStream;

    /**
     * Construct the Code2VecExecutor.
     * This class will initialize the model of Code2Vec, then provide an API to predict a name for a test method.
     */
    public Code2VecExecutor() {
        System.out.println(InputConfiguration.get());
        final String root = InputConfiguration.get().getPathToRootOfCode2Vec();
        final String pathToModel = InputConfiguration.get().getRelativePathToModelForCode2Vec();

        this.service = Executors.newSingleThreadExecutor();
        try {
            final String command = COMMAND_LINE + pathToModel + PREDICT_ARGUMENT;
            this.code2vecProcess = Runtime.getRuntime().exec(command, (String[]) null, new File(root));
            LOGGER.info("Executing: {} in {}", command, root);
        } catch (IOException var12) {
            throw new RuntimeException(var12);
        }

        this.outStream = new ByteArrayOutputStream();
        this.output = new PrintStream(outStream);
        this.task = new Code2VecRunnableProcess(this.code2vecProcess, this.output);
        this.future = this.service.submit(this.task);
        this.writer = new BufferedWriter(new OutputStreamWriter(this.code2vecProcess.getOutputStream()));
        try {
            LOGGER.info("Waiting {} seconds that code2vec is well initialized...", InputConfiguration.get().getTimeToWaitForCode2vecInMillis() / 1000);
            Thread.sleep(InputConfiguration.get().getTimeToWaitForCode2vecInMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            LOGGER.info("Writing go to the stdin of the code2vec's process");
            this.outStream.reset();
            this.writer.write("go" + AmplificationHelper.LINE_SEPARATOR);
            this.writer.flush();
            try {
                LOGGER.info("Waiting 5 seconds that code2vec is doing its job...");
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
            if (this.code2vecProcess != null) {
                this.code2vecProcess.destroyForcibly();
            }
            this.future.cancel(true);
            this.service.shutdownNow();
        }
    }

}
