package eu.stamp_project.diff_test_selection.diff;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/02/19
 */
public class DiffComputer {

    public static final String DIFF_FILE_NAME = "patch.diff";

    private final Log log;

    public DiffComputer(Log log) {
        this.log = log;
    }

    /**
     * This method allows to call a shell command
     *
     * @param command                the command to launch
     * @param pathToWorkingDirectory the directory from where the command must be launch
     */
    private void executeCommand(String command, File pathToWorkingDirectory) {
        this.log.info(String.format("Executing: %s from %s", command,
                pathToWorkingDirectory != null ?
                        pathToWorkingDirectory.getAbsolutePath() :
                        System.getProperty("user.dir")
                )
        );
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Process process;
        try {
            process = Runtime.getRuntime().exec(command, null, pathToWorkingDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Future<?> submit = executor.submit(() -> {
            try {
                process.waitFor();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ;
        });
        try {
            submit.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
            submit.cancel(true);
            executor.shutdownNow();
        }
    }

    public void computeDiffWithDiffCommand(File directoryVersionOne, File directoryVersionTwo) {
        this.log.info("Computing the diff with diff commnd line");
        this.log.info("The diff will be computed between:");
        this.log.info(directoryVersionOne.getAbsolutePath() + " and ");
        this.log.info(directoryVersionTwo.getAbsolutePath());
        final String command = String.join(" ", new String[]{
                "diff",
                "-ru",
                directoryVersionOne.getAbsolutePath(),
                directoryVersionTwo.getAbsolutePath(),
                ">",
                DIFF_FILE_NAME
        });
        this.executeCommand(command, directoryVersionOne);
    }

}
