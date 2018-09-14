package eu.stamp_project.clover;

import com.atlassian.clover.CloverInstr;
import com.atlassian.clover.reporters.html.HtmlReporter;
import com.atlassian.clover.reporters.json.JSONException;
import com.atlassian.clover.reporters.json.JSONObject;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.program.InputConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/12/17
 */
public class CloverExecutor {

    private static final String ROOT_DIRECTORY = "target/dspot/clover/";

    private static final String DATABASE_FILE = "/clover.db";

    private static final String INSTR_SOURCE_DIRECTORY = "/instr/";

    private static final String INSTR_BIN_DIRECTORY = "/instr-classes/";

    private static final String REPORT_DIRECTORY = "/report/";

    public static Map<String, Map<String, List<Integer>>> executeAll(InputConfiguration configuration,
                                                                     String pathToSources) {

        return CloverExecutor.execute(configuration, pathToSources,
                DSpotUtils.getAllTestClasses(configuration)
        );
    }

    public static Map<String, Map<String, List<Integer>>> execute(InputConfiguration configuration,
                                                                  String pathToSources,
                                                                  String... testClassesNames) {
        final File rootDirectoryOfCloverFiles = new File(configuration.getAbsolutePathToProjectRoot(), ROOT_DIRECTORY);
        try {
            FileUtils.deleteDirectory(rootDirectoryOfCloverFiles);
        } catch (IOException ignored) {
            //ignored
        }

        CloverInstr.mainImpl(new String[]{
                "-i", rootDirectoryOfCloverFiles.getAbsolutePath() + DATABASE_FILE,
                "-s", pathToSources,
                "-d", rootDirectoryOfCloverFiles.getAbsolutePath() + INSTR_SOURCE_DIRECTORY
        });

        final String finalClasspath = configuration.getDependencies() +
                AmplificationHelper.PATH_SEPARATOR + rootDirectoryOfCloverFiles.getAbsolutePath() + INSTR_BIN_DIRECTORY +
                AmplificationHelper.PATH_SEPARATOR + CLOVER_DEPENDENCIES;

        final File binaryOutputDirectory = new File(rootDirectoryOfCloverFiles.getAbsolutePath() + INSTR_BIN_DIRECTORY);
        if (!binaryOutputDirectory.mkdir()) {
            throw new RuntimeException("Could not create the directory" + rootDirectoryOfCloverFiles.getAbsolutePath() + INSTR_BIN_DIRECTORY);
        }
        DSpotCompiler.compile(configuration, rootDirectoryOfCloverFiles.getAbsolutePath() + INSTR_SOURCE_DIRECTORY,
                finalClasspath,
                binaryOutputDirectory
        );

        try {
            EntryPoint.runTestClasses(finalClasspath, testClassesNames);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        HtmlReporter.runReport(new String[]{
                "-i", rootDirectoryOfCloverFiles.getAbsolutePath() + DATABASE_FILE,
                "-o", rootDirectoryOfCloverFiles.getAbsolutePath() + REPORT_DIRECTORY,
                "--lineinfo",
                "--showinner",
                "--showlambda",
        });

        // removing the test classes ran
        Arrays.stream(testClassesNames).forEach(jsonTestTargets::remove);
        return convert();
    }

    private static Map<String, Map<String, List<Integer>>> convert() {
        final Map<String, Map<String, List<Integer>>> coverage = new HashMap<>();
        jsonTestTargets.keySet().forEach(sourceClass -> {
            final JSONObject jsonObject = jsonTestTargets.get(sourceClass);
            jsonObject.keys().forEachRemaining(record -> {
                try {
                    JSONObject currentValues = jsonObject.getJSONObject((String) record);
                    final String testMethodName = currentValues.getString("name");
                    if (!coverage.containsKey(testMethodName)) {
                        coverage.put(testMethodName, new HashMap<>());
                    }
                    coverage.get(testMethodName).put(sourceClass, new ArrayList<>());
                    ((List) currentValues.get("statements")).stream()
                            .map(list -> ((Map) list).get("sl"))
                            .forEach(line ->
                                    coverage.get(testMethodName).get(sourceClass).add((Integer) line)
                            );
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        jsonTestTargets.clear();
        return coverage;
    }

    private static final String CLOVER_DEPENDENCIES =
            FileUtils.class.getResource("/" + FileUtils.class.getName().replaceAll("\\.", "/") + ".class").getPath().substring(5).split("!")[0]
                    + AmplificationHelper.PATH_SEPARATOR +
                    CloverInstr.class.getResource("/" + CloverInstr.class.getName().replaceAll("\\.", "/") + ".class").getPath().substring(5).split("!")[0];

    public volatile static Map<String, JSONObject> jsonTestTargets = new HashMap<>();

}
