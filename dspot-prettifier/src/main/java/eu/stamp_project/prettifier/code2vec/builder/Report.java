package eu.stamp_project.prettifier.code2vec.builder;

import eu.stamp_project.utils.AmplificationHelper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/12/18
 */
public class Report {

    private static final String PATH_NAME_CSV_NUMBERS = Main.ROOT_PATH_DATA + "numbers.csv";

    private Map<String, String> errorDuringMethodExtraction;

    private static final String headerMethodsExtraction = "Errors during methods extraction:";

    private Map<String, String> errorDuringCloning;

    private static final String headerCloning = "Errors during cloning repository";

    private Map<String, NumbersOfMethodsPerSet> numberOfMethodsPerSetPerProject;

    private final static String CSV_SEPARATOR = ";";

    public Map<String, String> getErrorDuringMethodExtraction() {
        return errorDuringMethodExtraction;
    }

    public Map<String, String> getErrorDuringCloning() {
        return errorDuringCloning;
    }

    /**
     * this construct the report. It will initialize all the fields.
     * If the file designed by the path {@link Report#PATH_NAME_CSV_NUMBERS} exists,
     * it will initialize the map of number of test per project with its content.
     * If so, it will update the shaPerProject map of the given Cloner in order to make it match.
     * @param cloner
     */
    public Report(Cloner cloner) {
        this.errorDuringMethodExtraction = new HashMap<>();
        this.errorDuringCloning = new HashMap<>();
        if (new File(PATH_NAME_CSV_NUMBERS).exists()) {
            try (final BufferedReader reader = new BufferedReader(new FileReader(PATH_NAME_CSV_NUMBERS))) {
                this.numberOfMethodsPerSetPerProject = reader.lines()
                        .map(line -> line.split(";"))
                        .filter(lineCsv -> !"total".equals(lineCsv[1]))
                        .collect(
                                Collectors.toMap(
                                        lineCsv -> lineCsv[0],
                                        lineCsv -> new NumbersOfMethodsPerSet(
                                                Integer.parseInt(lineCsv[1]),
                                                Integer.parseInt(lineCsv[2]),
                                                Integer.parseInt(lineCsv[3])
                                        )
                                )
                        );
                cloner.update(this.numberOfMethodsPerSetPerProject.keySet());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            this.numberOfMethodsPerSetPerProject = new HashMap<>();
        }
    }

    public void addNumberOfMethodsPerSetPerProject(String project, NumbersOfMethodsPerSet numbersOfMethodsPerSet) {
        this.numberOfMethodsPerSetPerProject.put(project, numbersOfMethodsPerSet);
    }

    public void addToGivenMap(Map<String, String> map, String key, Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        map.put(key, sw.toString());
    }

    public boolean containsProject(String userAndProject) {
        return this.numberOfMethodsPerSetPerProject.containsKey(userAndProject);
    }

    public NumbersOfMethodsPerSet getNumbersOfMethodsPerSet(String userAndProject) {
        return this.numberOfMethodsPerSetPerProject.get(userAndProject);
    }

    public void report() {
        numbers();
        errors();
    }

    private void numbers() {
        StringBuilder report = new StringBuilder();
        final NumbersOfMethodsPerSet total = this.numberOfMethodsPerSetPerProject.keySet()
                .stream()
                .map(project -> {
                    final NumbersOfMethodsPerSet numbersOfMethodsPerSet = this.numberOfMethodsPerSetPerProject.get(project);
                    this.appendNumbersOfMethodsPerSetToStringBuilder(report, project, numbersOfMethodsPerSet);
                    return numbersOfMethodsPerSet;
                }).reduce(NumbersOfMethodsPerSet::sum).get();
        this.appendNumbersOfMethodsPerSetToStringBuilder(report, "total", total);
        write(PATH_NAME_CSV_NUMBERS, report.toString());
    }

    private void appendNumbersOfMethodsPerSetToStringBuilder(StringBuilder report,
                                                             String project,
                                                             NumbersOfMethodsPerSet numbersOfMethodsPerSet) {
        report.append(project)
                .append(CSV_SEPARATOR)
                .append(numbersOfMethodsPerSet.numberOfMethodForTraining)
                .append(CSV_SEPARATOR)
                .append(numbersOfMethodsPerSet.numberOfMethodForValidation)
                .append(CSV_SEPARATOR)
                .append(numbersOfMethodsPerSet.numberOfMethodForTest)
                .append(CSV_SEPARATOR)
                .append(numbersOfMethodsPerSet.total())
                .append(AmplificationHelper.LINE_SEPARATOR);
    }

    private void errors() {
        StringBuilder report = new StringBuilder();
        addContentOfGivenMap(errorDuringMethodExtraction, headerMethodsExtraction, report);
        addContentOfGivenMap(errorDuringCloning, headerCloning, report);
        write(Main.ROOT_PATH_DATA + "errors.txt", report.toString());
    }

    private void addContentOfGivenMap(Map<String, String> map, String header, StringBuilder report) {
        if (!map.isEmpty()) {
            report.append(header).append(AmplificationHelper.LINE_SEPARATOR);
            map.keySet().forEach(key ->
                    report.append(key)
                            .append(AmplificationHelper.LINE_SEPARATOR)
                            .append(errorDuringMethodExtraction.get(key))
                            .append(AmplificationHelper.LINE_SEPARATOR)
            );
        }
    }

    private void write(String pathname, String content) {
        try (FileWriter writer = new FileWriter(pathname, false)) {
            writer.write(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
