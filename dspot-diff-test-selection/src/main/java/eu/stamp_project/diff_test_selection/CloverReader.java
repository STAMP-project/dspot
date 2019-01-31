package eu.stamp_project.diff_test_selection;

import com.atlassian.clover.reporters.html.HtmlReporter;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/12/17
 */
public class CloverReader {

    private static final String ROOT_DIRECTORY = "/target/clover";

    private static final String DATABASE_FILE = "/clover.db";

    private static final String REPORT_DIRECTORY = "/report/";

    public volatile static Map<String, Map<String, Map<String, List<Integer>>>> coveragePerTestMethods = new LinkedHashMap<>();

    /**
     * read the database initialize by {@link CloverExecutor}.
     *
     * @param directory
     * @return a map, that associate test method names and the map of executed line in each classes
     */
    public Map<String, Map<String, Map<String, List<Integer>>>> read(String directory) {
        final File rootDirectoryOfCloverFiles = new File(directory + ROOT_DIRECTORY);
        HtmlReporter.runReport(new String[]{
                "-i", rootDirectoryOfCloverFiles.getAbsolutePath() + DATABASE_FILE,
                "-o", rootDirectoryOfCloverFiles.getAbsolutePath() + REPORT_DIRECTORY,
                "--lineinfo",
                "--showinner",
                "--showlambda",
        });
        return coveragePerTestMethods;
    }

}
