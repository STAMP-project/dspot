package fr.inria.diversify.automaticbuilder;

import java.io.*;

import java.nio.file.*;

import java.util.Arrays;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.stream.Collectors;

import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.runner.InputConfiguration;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.util.Log;

import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import static fr.inria.diversify.mutant.pit.GradlePitTaskAndOptions.*;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 18/07/17.
 */
public class GradleAutomaticBuilder implements AutomaticBuilder {

    private static final String JAVA_PROJECT_CLASSPATH = "gjp_cp"; // Gradle Java Project classpath file

    private static final String CLASSPATH_SEPARATOR = System.getProperty("path.separator");

    private static final String GRADLE_BUILD_FILE_BAK_SUFFIX = ".orig";

    private static final String GRADLE_BUILD_FILE = "build.gradle";

    private static final String NEW_LINE = System.getProperty("line.separator");

    private InputConfiguration configuration;

    GradleAutomaticBuilder(InputConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void compile(String pathToRootOfProject) {
        runTasks(pathToRootOfProject,"clean", "compileJava", "test");
    }

    @Override
    public String buildClasspath(String pathToRootOfProject) {
        try {
            final File classpathFile = new File(pathToRootOfProject + File.separator + JAVA_PROJECT_CLASSPATH);
            if (!classpathFile.exists()) {
                Log.debug("Classpath file for Gradle project doesn't exist, starting to build it...");

                Log.debug("Injecting  Gradle task to print project classpath on stdout...");
                injectPrintClasspathTask(pathToRootOfProject);
                Log.debug("Retrieving project classpath...");
                byte[] taskOutput = cleanClasspath(runTasks(pathToRootOfProject,"printClasspath4DSpot"));
                Log.debug("Writing project classpath on file " + JAVA_PROJECT_CLASSPATH +"...");
                FileOutputStream fos = new FileOutputStream(pathToRootOfProject + File.separator + JAVA_PROJECT_CLASSPATH);
                fos.write(taskOutput);
                fos.close();
                resetOriginalGradleBuildFile(pathToRootOfProject);
            }
            try (BufferedReader buffer = new BufferedReader(new FileReader(classpathFile))) {
                return buffer.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PitResult> runPit(String pathToRootOfProject) {
        return runPit(pathToRootOfProject, null);
    }

    @Override
    public List<PitResult> runPit(String pathToRootOfProject, CtType<?> testClass) {
        try {
            Log.debug("Injecting  Gradle task to run Pit...");
            injectPitTask(pathToRootOfProject, testClass);

            Log.debug("Running Pit...");

            runTasks(pathToRootOfProject,CMD_PIT_MUTATION_COVERAGE);

            resetOriginalGradleBuildFile(pathToRootOfProject);

            File directoryReportPit = new File(pathToRootOfProject + "/build/pit-reports").listFiles()[0];
            return PitResultParser.parse(new File(directoryReportPit.getPath() + "/mutations.csv"));

        } catch (Exception e) {
            return null;
        }
    }

    protected byte[] runTasks(String pathToRootOfProject, String... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(new File(pathToRootOfProject)).connect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            BuildLauncher build = connection.newBuild();
            build.forTasks(tasks);
            build.setStandardOutput(outputStream);
            build.setStandardError(outputStream);
            build.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            connection.close();
        }
        return outputStream.toByteArray();
    }

    private byte[] cleanClasspath(byte[] taskOutput) {

        Log.debug("Retrieved task output:\n");
        Log.debug(new String(taskOutput));
        Log.debug("\nExtracting project classpath from task output...");
        StringBuilder sb = new StringBuilder();
        String cleanCP = new String(taskOutput);
        String classPathPattern = "([\\/a-z0-9\\.\\-]*\\.jar|[\\/a-z0-9\\.\\-]*\\.zip)";

        Pattern p = Pattern.compile(classPathPattern);

        Matcher m = p.matcher(cleanCP);
        while (m.find()) {
            sb.append(m.group());
            sb.append(CLASSPATH_SEPARATOR);
        }
        Log.debug("Project classpath from task output:\n");
        Log.debug(sb.toString() + "\n");
        return sb.toString().getBytes();
    }

    private void injectPrintClasspathTask(String pathToRootOfProject) throws IOException {

        String originalGradleBuildFilename = pathToRootOfProject + File.separator + GRADLE_BUILD_FILE;
        File gradleBuildFile = new File(originalGradleBuildFilename);
        makeBackup(gradleBuildFile);

        String printClasspathTask = getPrintClasspathTask();

        Files.write(Paths.get(originalGradleBuildFilename), printClasspathTask.getBytes(), StandardOpenOption.APPEND);

        Log.debug("Injected following Gradle task in the Gradle build file " + originalGradleBuildFilename + ":\n");
        Log.debug(printClasspathTask);
    }

    private void injectPitTask(String pathToRootOfProject) throws IOException {
        injectPitTask(pathToRootOfProject, null);
    }

    private void injectPitTask(String pathToRootOfProject, CtType<?> testClass) throws IOException {

        String originalGradleBuildFilename = pathToRootOfProject + File.separator + GRADLE_BUILD_FILE;
        File gradleBuildFile = new File(originalGradleBuildFilename);
        makeBackup(gradleBuildFile);

        String pitTask = getPitTask(testClass);

        Files.write(Paths.get(originalGradleBuildFilename), pitTask.getBytes(), StandardOpenOption.APPEND);

        Log.debug("Injected following Gradle task in the Gradle build file " + originalGradleBuildFilename + ":\n");
        Log.debug(pitTask);
    }

    private void makeBackup(File gradleBuildFile) throws IOException {
        if (gradleBuildFile.exists()) {
            Log.debug("Found original Gradle build file, making backup copy...");
            String originalGradleBuildFilename = gradleBuildFile.getPath();
            String backedUpGradleBuildFilename = originalGradleBuildFilename + GRADLE_BUILD_FILE_BAK_SUFFIX;
            Path from = Paths.get(originalGradleBuildFilename);
            Path to = Paths.get(backedUpGradleBuildFilename);
            //overwrite existing file, if exists
            CopyOption[] options = new CopyOption[]{
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
            };
            Files.copy(from, to, options);
            Log.debug("Original Gradle build file backed-up as " + backedUpGradleBuildFilename + ".");
        }
    }

    private void resetOriginalGradleBuildFile(String pathToRootOfProject) {

        Log.debug("Restoring original Gradle build file...");

        String modifiedGradleBuildFilename = pathToRootOfProject + File.separator + GRADLE_BUILD_FILE;
        String originalGradleBuildFilename = modifiedGradleBuildFilename + GRADLE_BUILD_FILE_BAK_SUFFIX;

        File originalGradleBuildFile = new File(originalGradleBuildFilename);
        if (originalGradleBuildFile.exists()) {
            File modifiedGradleBuildFile = new File(modifiedGradleBuildFilename);
            Log.debug("Deleting modified (with injected task) Gradle build file...");
            modifiedGradleBuildFile.delete();
            Log.debug("Renaming original Gradle build file from " + originalGradleBuildFilename + " to " + modifiedGradleBuildFilename + "...");
            originalGradleBuildFile.renameTo(new File(pathToRootOfProject + File.separator + GRADLE_BUILD_FILE));
        }
    }

    private String ctTypeToFullQualifiedName(CtType<?> testClass) {
        if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
            CtTypeReference<?> referenceOfSuperClass = testClass.getReference();
            return testClass.getFactory().Class().getAll()
                    .stream()
                    .filter(ctType -> referenceOfSuperClass.equals(ctType.getSuperclass()))
                    .map(CtType::getQualifiedName)
                    .collect(Collectors.joining(","));
        } else {
            return testClass.getQualifiedName();
        }
    }



    private String getPrintClasspathTask() {
        return NEW_LINE + NEW_LINE + "task printClasspath4DSpot {" + NEW_LINE +
                "    doLast {" + NEW_LINE +
                "        configurations.testRuntime.each { println it }" + NEW_LINE +
                "    }" + NEW_LINE +
                "}";
    }

    private String getPitTask() {
        return getPitTask(null);
    }


    private String getPitTask(CtType<?> testClass) {
        return getPitTaskConfiguration() + getPitTaskOptions(testClass);
    }

    private String getPitTaskConfiguration() {
        return NEW_LINE + NEW_LINE + "buildscript {" + NEW_LINE +
                "    repositories {" + NEW_LINE +
                "        //mavenLocal()" + NEW_LINE +
                "        maven {\n" +
                "            url \"https://plugins.gradle.org/m2/\"" + NEW_LINE +
                "        }" + NEW_LINE +
                NEW_LINE +
                "    }" + NEW_LINE +
                NEW_LINE +
                "    configurations.maybeCreate(\"pitest\")" + NEW_LINE +
                NEW_LINE +
                "    dependencies {" + NEW_LINE +
                "       classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.1.11'" + NEW_LINE +
                "       //pitest 'fr.inria.stamp:descartes:0.2-SNAPSHOT'" + NEW_LINE +
                "    }" + NEW_LINE +
                "}" + NEW_LINE +
                NEW_LINE +
                "apply plugin: 'info.solidsoft.pitest'" + NEW_LINE;
    }

    private String getPitTaskOptions() {
        return getPitTaskOptions(null);
    }

    private String getPitTaskOptions(CtType<?> testClass) {
        return  NEW_LINE + NEW_LINE + "pitest {" + NEW_LINE +
                "    " + OPT_TARGET_CLASSES + "['" + configuration.getProperty("filter") + "']" + NEW_LINE +
                "    " + OPT_WITH_HISTORY + "true" + NEW_LINE +
                "    " + OPT_VALUE_REPORT_DIR + NEW_LINE +
                "    " + OPT_VALUE_FORMAT + NEW_LINE +
//                "    " + OPT_VALUE_TIMEOUT + NEW_LINE +
//                "    " + OPT_VALUE_MEMORY + NEW_LINE +
                (testClass != null ? "    " + OPT_TARGET_TESTS + "['" + ctTypeToFullQualifiedName(testClass) + "']": "") + NEW_LINE +
                (configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) != null ?
                                    "    " + OPT_ADDITIONAL_CP_ELEMENTS + "['" + configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) + "']":"") + NEW_LINE +
                (descartesMode ? "    " + OPT_MUTATION_ENGINE :
                                    "    " + OPT_MUTATORS + (evosuiteMode ?
                                                    VALUE_MUTATORS_EVOSUITE : VALUE_MUTATORS_ALL)) + NEW_LINE +
                (configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) != null ?
                        "    " + OPT_EXCLUDED_CLASSES +  "['" + configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) + "']":"") + NEW_LINE +
                "}" + NEW_LINE;
    }

}
