package eu.stamp_project.mutant.pit;

import java.io.File;

public class AbstractParser {

    protected static File getPathOfMutationsCsvFile(String pathToDirectoryResults, String PATH_TO_MUTATIONS_RESULT) {
        if (!new File(pathToDirectoryResults).exists()) {
            return null;
        }
        if (new File(pathToDirectoryResults + PATH_TO_MUTATIONS_RESULT).exists()) {
            return new File(pathToDirectoryResults + PATH_TO_MUTATIONS_RESULT);
        }
        final File[] files = new File(pathToDirectoryResults).listFiles();
        if (files == null) {
            return null;
        }
        File directoryReportPit = files[0];
        if (!directoryReportPit.exists()) {
            return null;
        }
        return new File(directoryReportPit.getPath() + PATH_TO_MUTATIONS_RESULT);
    }
}
