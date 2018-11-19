package eu.stamp_project.mutant.pit;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

abstract public class AbstractParser {
    private final String PATH_TO_MUTATIONS_RESULT;

    AbstractParser(String PATH_TO_MUTATIONS_RESULT){
        this.PATH_TO_MUTATIONS_RESULT = PATH_TO_MUTATIONS_RESULT;
    }

    protected File getPathOfMutationsFile(String pathToDirectoryResults) {
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

    public List<AbstractPitResult> parseAndDelete(String pathToDirectoryResults) {
        final File fileResults = getPathOfMutationsFile(pathToDirectoryResults);
        final List<AbstractPitResult> results = parse(fileResults);
        try {
            FileUtils.deleteDirectory(new File(pathToDirectoryResults));
        } catch (IOException e) {
            // ignored
        }
        return results;
    }

    abstract public List<AbstractPitResult> parse(File fileResults);


}
