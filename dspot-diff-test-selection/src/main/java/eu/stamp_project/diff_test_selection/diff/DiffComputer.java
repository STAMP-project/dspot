package eu.stamp_project.diff_test_selection.diff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/02/19
 */
public class DiffComputer {

    public static final String DIFF_FILE_NAME = "patch.diff";

    private static final Logger LOGGER = LoggerFactory.getLogger(DiffComputer.class);

    /**
     * This method allows to call a shell command
     *
     * @param command                the command to launch
     * @param pathToWorkingDirectory the directory from where the command must be launch
     */
    private String executeCommand(String command, File pathToWorkingDirectory) {
        LOGGER.info(String.format("Executing: %s from %s", command,
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
        });
        try {
            submit.get(5, TimeUnit.SECONDS);
            if (process != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                int current;
                StringBuilder output = new StringBuilder();
                while (true) {
                    try {
                        if ((current = inputStreamReader.read()) == -1) {
                            break;
                        }
                        output.append((char) current);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                try (FileWriter writer = new FileWriter(pathToWorkingDirectory + DIFF_FILE_NAME, false)) {
                    writer.write(output.toString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return output.toString();
            } else {
                return "";
            }
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

    public String computeDiffWithDiffCommand(File directoryVersionOne,
                                           File directoryVersionTwo) {
        LOGGER.info("Computing the diff with diff commnd line");
        LOGGER.info("The diff will be computed between:");
        LOGGER.info(directoryVersionOne.getAbsolutePath() + " and ");
        LOGGER.info(directoryVersionTwo.getAbsolutePath());
        final String command = String.join(" ", new String[]{
                "diff",
                "-ru",
                directoryVersionOne.getAbsolutePath(),
                directoryVersionTwo.getAbsolutePath()
        });
        return this.executeCommand(command, directoryVersionOne);
    }

}
