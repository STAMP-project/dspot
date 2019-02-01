package eu.stamp_project.diff_test_selection;

import eu.stamp_project.diff_test_selection.coverage.Coverage;
import eu.stamp_project.diff_test_selection.diff.DiffComputer;
import eu.stamp_project.diff_test_selection.report.CSVReport;
import eu.stamp_project.diff_test_selection.report.Report;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

@Mojo(name = "list")
public class DiffTestSelectionMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Parameter(property = "pathToDiff")
    private String pathToDiff;

    @Parameter(property = "pathToOtherVersion", required = true)
    private String pathToOtherVersion;

    @Parameter(property = "outputPath", defaultValue = "")
    private String outputPath;

    private static final String DEFAULT_OUTPUT_PATH_NAME = "testsThatExecuteTheChange.csv";

    @Parameter(property = "report", defaultValue = "CSV")
    private String report;

    @Parameter(property = "module", defaultValue = "")
    private String module;

    @Parameter(property = "skipCoverage")
    private boolean skipCoverage = false;

    private Coverage coverage = new Coverage(getLog());

    public DiffTestSelectionMojo() {

    }

    private enum ReportEnum {
        CSV(new CSVReport());
        public final Report instance;

        ReportEnum(Report instance) {
            this.instance = instance;
        }
    }

    private void checksArguments() {
        this.pathToOtherVersion = checksIfExistAndUseAbsolutePath(this.pathToOtherVersion) + "/";
        if (this.pathToDiff == null || !new File(this.pathToDiff).exists()) {
            // computing the diff...
            new DiffComputer(getLog()).computeDiffWithDiffCommand(this.project.getBasedir(), new File(this.pathToOtherVersion));
            this.pathToDiff = this.project.getBasedir() + "/" + DiffComputer.DIFF_FILE_NAME;
        }
        this.pathToDiff = checksIfExistAndUseAbsolutePath(this.pathToDiff) + "/";
        if (this.module == null) {
            this.module = "";
        }
    }

    private String checksIfExistAndUseAbsolutePath(String pathFileToCheck) {
        final File file = new File(pathFileToCheck);
        if (!file.exists()) {
            getLog().error(pathFileToCheck + " does not exist, please check it out!", new IllegalArgumentException(pathFileToCheck));
            System.exit(1);
        }
        return file.getAbsolutePath();
    }

    @Override
    public void execute() {
        checksArguments();
        final Map<String, Map<String, Map<String, List<Integer>>>> coverage = getCoverage();
        final Map<String, Set<String>> testThatExecuteChanges = this.getTestThatExecuteChanges(coverage);
        if (this.outputPath == null || this.outputPath.isEmpty()) {
            this.outputPath = this.project.getBasedir().getAbsolutePath() + "/" + DEFAULT_OUTPUT_PATH_NAME;
        }
        getLog().info("Saving result in " + this.outputPath + " ...");
        ReportEnum.valueOf(this.report).instance.report(
                getLog(),
                this.outputPath,
                testThatExecuteChanges,
                this.coverage
        );
    }

    private Map<String, Map<String, Map<String, List<Integer>>>> getCoverage() {
        if (!skipCoverage) {
            getLog().info("Computing coverage for " + this.project.getBasedir().getAbsolutePath());
            new CloverExecutor().instrumentAndRunTest(this.project.getBasedir().getAbsolutePath());
        }
        return new CloverReader().read(this.project.getBasedir().getAbsolutePath());
    }

    private Map<String, Set<String>> getTestThatExecuteChanges(Map<String, Map<String, Map<String, List<Integer>>>> coverage) {
        final Map<String, Set<String>> testMethodPerTestClasses = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(this.pathToDiff)))) {
            String currentLine = null;
            while ((currentLine = reader.readLine()) != null) {
                if ((currentLine.startsWith("+++") || currentLine.startsWith("---")) && currentLine.endsWith(".java")) {
                    Map<String, List<Integer>> modifiedLinesPerQualifiedName =
                            getModifiedLinesPerQualifiedName(currentLine, reader.readLine());
                    if (modifiedLinesPerQualifiedName == null) {
                        continue;
                    }
                    //this.coverage.addModifiedLines(modifiedLinesPerQualifiedName);
                    Map<String, Set<String>> matchedChangedWithCoverage = matchChangedWithCoverage(coverage, modifiedLinesPerQualifiedName);
                    matchedChangedWithCoverage.keySet().forEach(key -> {
                        if (!testMethodPerTestClasses.containsKey(key)) {
                            testMethodPerTestClasses.put(key, matchedChangedWithCoverage.get(key));
                        } else {
                            testMethodPerTestClasses.get(key).addAll(matchedChangedWithCoverage.get(key));
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testMethodPerTestClasses;
    }

    @Nullable
    private Map<String, List<Integer>> getModifiedLinesPerQualifiedName(String currentLine,
                                                                        String secondLine) throws Exception {
        final File baseDir = project.getBasedir();
        final String file1 = getCorrectPathFile(currentLine);
        final String file2 = getCorrectPathFile(secondLine);
        if (!file2.endsWith(file1)) {
            getLog().warn("Could not match " + file1 + " and " + file2);
            return null;
        }
        final File f1 = getCorrectFile(baseDir.getAbsolutePath(), file1);
        final File f2 = getCorrectFile(this.pathToOtherVersion, file2);
        try {
            getLog().info(f1.getAbsolutePath());
            getLog().info(f2.getAbsolutePath());
            return buildMap(new AstComparator().compare(f1, f2));
        } catch (Exception e) {
            e.printStackTrace();
            getLog().error("Error when trying to compare " + f1 + " and " + f2);
            return null;
        }
    }

    private File getCorrectFile(String baseDir, String fileName) {
        if (fileName.substring(1).startsWith(this.module)) {
            fileName = fileName.substring(this.module.length() + 1);
        }
        final File file = new File(baseDir + "/" + fileName);
        return file.exists() ? file : new File(baseDir + "/../" + fileName);
    }

    @NotNull
    private Map<String, List<Integer>> buildMap(Diff compare) {
        Map<String, List<Integer>> modifiedLinesPerQualifiedName = new LinkedHashMap<>();// keeps the order
        final List<Operation> allOperations = compare.getAllOperations();
        final List<CtStatement> statements = new  ArrayList<>();
        for (Operation operation : allOperations) {
            final CtElement node = filterOperation(operation);
            if ( node != null && !statements.contains(node.getParent(CtStatement.class))) {
                final int line = node.getPosition().getLine();
                final String qualifiedName = node
                        .getPosition()
                        .getCompilationUnit()
                        .getMainType()
                        .getQualifiedName();
                if (!modifiedLinesPerQualifiedName.containsKey(qualifiedName)) {
                    modifiedLinesPerQualifiedName.put(qualifiedName, new ArrayList<>());
                }
                modifiedLinesPerQualifiedName.get(qualifiedName).add(line);
                if (!(node.getParent(CtStatement.class)instanceof CtBlock<?>)) {
                    this.coverage.addModifiedLine(qualifiedName, line);
                }
                statements.add(node.getParent(CtStatement.class));
            }
        }
        return modifiedLinesPerQualifiedName;
    }

    private Map<String, Set<String>> matchChangedWithCoverage(Map<String, Map<String, Map<String, List<Integer>>>> coverage,
                                                              Map<String, List<Integer>> modifiedLinesPerQualifiedName) {
        Map<String, Set<String>> testClassNamePerTestMethodNamesThatCoverChanges = new LinkedHashMap<>();
        for (String testClassKey : coverage.keySet()) {
            for (String testMethodKey : coverage.get(testClassKey).keySet()) {
                for (String targetClassName : coverage.get(testClassKey).get(testMethodKey).keySet()) {
                    if (modifiedLinesPerQualifiedName.containsKey(targetClassName)) {
                        for (Integer line : modifiedLinesPerQualifiedName.get(targetClassName)) {
                            if (coverage.get(testClassKey).get(testMethodKey).get(targetClassName).contains(line)) {
                                // testClassKey#testMethodKey hits targetClassName#line
                                this.coverage.covered(targetClassName, line);
                                if (!testClassNamePerTestMethodNamesThatCoverChanges.containsKey(testClassKey)) {
                                    testClassNamePerTestMethodNamesThatCoverChanges.put(testClassKey, new HashSet<>());
                                }
                                testClassNamePerTestMethodNamesThatCoverChanges.get(testClassKey).add(testMethodKey);
                            }
                        }
                    }
                }
            }
        }
        return testClassNamePerTestMethodNamesThatCoverChanges;
    }

    private CtElement filterOperation(Operation operation) {
        if (operation.getSrcNode() != null &&
                filterOperationFromNode(operation.getSrcNode())) {
            return operation.getSrcNode();
        } else if (operation.getDstNode() != null &&
                filterOperationFromNode(operation.getDstNode())) {
            return operation.getDstNode();
        }
        return null;
    }

    private boolean filterOperationFromNode(CtElement element) {
        return element.getPosition() != null &&
                element.getPosition().getCompilationUnit() != null &&
                element.getPosition().getCompilationUnit().getMainType() != null;
    }

    private String getCorrectPathFile(String path) {
        final String s = path.split(" ")[1];
        if (s.contains("\t")) {
            return removeDiffPrefix(s.split("\t")[0]);
        }
        return removeDiffPrefix(s);
    }

    /**
     * removing the first letter, i.e. a and b;
     *
     * @param s
     * @return the string s without a or b at the begin, if there are present
     */
    private String removeDiffPrefix(String s) {
        return s.startsWith("a") || s.startsWith("b") ? s.substring(1) : s;
    }

    /*
     *  Test purposes
     */

    void setProject(MavenProject project) {
        this.project = project;
    }

    void setPathToDiff(String pathToDiff) {
        this.pathToDiff = pathToDiff;
    }

    void setPathToOtherVersion(String pathToOtherVersion) {
        this.pathToOtherVersion = pathToOtherVersion;
    }

    void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    void setReport(String report) {
        this.report = report;
    }

    public static void main(String[] args) {
        DiffTestSelectionMojo diffTestSelectionMojo = new DiffTestSelectionMojo();
        diffTestSelectionMojo.setPathToDiff("/home/bdanglot/workspace/bugs-dot-jar/commons-math/.bugs-dot-jar/developer-patch.diff");
        diffTestSelectionMojo.setPathToOtherVersion("/home/bdanglot/workspace/bugs-dot-jar/commons-math_fixed");
        MavenProject mavenProject = new MavenProject();
        mavenProject.setPomFile(new File("/home/bdanglot/workspace/bugs-dot-jar/commons-math/pom.xml"));
        mavenProject.setFile(new File("/home/bdanglot/workspace/bugs-dot-jar/commons-math/pom.xml"));
        diffTestSelectionMojo.setProject(mavenProject);
        diffTestSelectionMojo.setLog(new DefaultLog(new ConsoleLogger(0, "logger")));
        diffTestSelectionMojo.setReport("CSV");
        diffTestSelectionMojo.setOutputPath("testsThatExecuteTheChange.csv");
        diffTestSelectionMojo.execute();
    }

}