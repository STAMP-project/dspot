package eu.stamp_project.diff_test_selection.configuration;

import eu.stamp_project.diff_test_selection.diff.DiffComputer;
import eu.stamp_project.diff_test_selection.report.CSVReport;
import eu.stamp_project.diff_test_selection.report.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.sax.SAXSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/02/19
 */
public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    public static final String DEFAULT_OUTPUT_PATH_NAME = "testsThatExecuteTheChange.csv";

    public final String pathToFirstVersion;

    public final String pathToSecondVersion;

    public final String outputPath;

    public final ReportEnum reportFormat;

    public final String module;

    public final String diff;

    public Configuration(String pathToFirstVersion, String pathToSecondVersion, String outputPath, String reportFormat, String module, String pathToDiff) {
        this.pathToFirstVersion = pathToFirstVersion;
        this.pathToSecondVersion = pathToSecondVersion;
        this.reportFormat = ReportEnum.valueOf(reportFormat);
        this.module = module == null ? "" : module;
        if (pathToDiff == null || pathToDiff.isEmpty()) {
            if (!new File(pathToFirstVersion + "/" + DiffComputer.DIFF_FILE_NAME).exists()) {
                LOGGER.warn("No path to diff file has been specified.");
                LOGGER.warn("I'll compute a diff file using the UNIX diff command");
                LOGGER.warn("You may encounter troubles.");
                LOGGER.warn("If so, please specify a path to a correct diff file");
                LOGGER.warn("or implement a new way to compute a diff file.");
                this.diff = new DiffComputer().computeDiffWithDiffCommand(
                        new File(pathToFirstVersion), new File(pathToSecondVersion)
                );
            } else {
                this.diff = this.readFile(pathToFirstVersion + "/" + DiffComputer.DIFF_FILE_NAME);
            }
        } else {
            this.diff = this.readFile(pathToDiff);
        }
        System.out.println(this.diff);
        if (outputPath == null || outputPath.isEmpty()) {
            this.outputPath = this.pathToFirstVersion +
                    (this.pathToFirstVersion.endsWith("/") ? "" : "/") +
                    DEFAULT_OUTPUT_PATH_NAME;
        } else {
            this.outputPath = outputPath;
        }
    }

    private String readFile(String pathToFileToRead) {
        String fileContent = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(pathToFileToRead)))) {
            fileContent += reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileContent;
    }

    public enum ReportEnum {
        CSV(new CSVReport());
        public final Report instance;

        ReportEnum(Report instance) {
            this.instance = instance;
        }
    }

}
