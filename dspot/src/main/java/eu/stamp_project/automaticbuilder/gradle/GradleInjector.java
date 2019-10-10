package eu.stamp_project.automaticbuilder.gradle;

import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.stream.Collectors;

import static eu.stamp_project.automaticbuilder.gradle.GradlePitTaskAndOptions.*;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 18/07/17.
 * <p>
 * refactored by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/10/18
 */
public class GradleInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleAutomaticBuilder.class);

    private static final String GRADLE_BUILD_FILE_BACK_SUFFIX = ".orig";

    public static final String GRADLE_BUILD_FILE = "build.gradle";

    private File originalGradleBuildFile;

    private boolean isDescartesMode;

    private String filter;

    private String pitVersion;

    private int timeoutMs;

    private String JVMArgs;

    private String excludedClasses;

    private String additionalClasspathElements;

    public GradleInjector(String absolutePathToOriginalGradleBuildFile,
                          boolean isDescartesMode,
                          String filter,
                          String pitVersion,
                          int timeoutMs,
                          String JVMArgs,
                          String excludedClasses,
                          String additionalClasspathElements) {
        this.isDescartesMode = isDescartesMode;
        this.filter = filter;
        this.pitVersion = pitVersion;
        this.timeoutMs = timeoutMs;
        this.JVMArgs = JVMArgs;
        this.excludedClasses = excludedClasses;
        this.additionalClasspathElements = additionalClasspathElements;
        this.originalGradleBuildFile = new File(absolutePathToOriginalGradleBuildFile);
        if (!this.originalGradleBuildFile.exists()) {
            throw new RuntimeException(absolutePathToOriginalGradleBuildFile + " does not exists!");
        }
    }

    void injectPrintClasspathTask(String pathToRootOfProject) throws IOException {
        String originalGradleBuildFilename = pathToRootOfProject + File.separator + GRADLE_BUILD_FILE;
        File gradleBuildFile = new File(originalGradleBuildFilename);
        makeBackup(gradleBuildFile);
        String printClasspathTask = getPrintClasspathTask();
        Files.write(Paths.get(originalGradleBuildFilename), printClasspathTask.getBytes(), StandardOpenOption.APPEND);
        LOGGER.info("Injected following Gradle task in the Gradle build file {}:", originalGradleBuildFilename);
        LOGGER.info("{}", printClasspathTask);
    }

    @Deprecated
    void injectPitTask(String pathToRootOfProject) throws IOException {
        injectPitTask(pathToRootOfProject, null);
    }

    void injectPitTask(String pathToRootOfProject, CtType<?>... testClasses) throws IOException {
        String originalGradleBuildFilename = pathToRootOfProject + File.separator + GRADLE_BUILD_FILE;
        File gradleBuildFile = new File(originalGradleBuildFilename);
        makeBackup(gradleBuildFile);
        String pitTask = getPitTask(testClasses);
        Files.write(Paths.get(originalGradleBuildFilename), pitTask.getBytes(), StandardOpenOption.WRITE);
    }

    /*
        ORIGINAL GRADLE BUILD MANAGEMENT
     */

    void makeBackup(File gradleBuildFile) throws IOException {
        LOGGER.info("Making backup copy of original Gradle file...");

        final String originalGradleBuildFilename = gradleBuildFile.getAbsolutePath();
        final String backedUpGradleBuildFilename = originalGradleBuildFilename + GRADLE_BUILD_FILE_BACK_SUFFIX;
        final Path from = Paths.get(originalGradleBuildFilename);
        final Path to = Paths.get(backedUpGradleBuildFilename);

        //overwrite existing file, if exists
        final CopyOption[] options = new CopyOption[]{
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        };
        Files.copy(from, to, options);
        LOGGER.info("Original Gradle build file backed-up as " + backedUpGradleBuildFilename + ".");
    }

    void resetOriginalGradleBuildFile(String pathToRootOfProject) {
        LOGGER.info("Restoring original Gradle build file...");
        String modifiedGradleBuildFilename = pathToRootOfProject + File.separator + GRADLE_BUILD_FILE;
        String originalGradleBuildFilename = modifiedGradleBuildFilename + GRADLE_BUILD_FILE_BACK_SUFFIX;
        File originalGradleBuildFile = new File(originalGradleBuildFilename);
        if (originalGradleBuildFile.exists()) {
            File modifiedGradleBuildFile = new File(modifiedGradleBuildFilename);
            LOGGER.info("Deleting modified (with injected task) Gradle build file...");
            modifiedGradleBuildFile.delete();
            LOGGER.info("Renaming original Gradle build file from " + originalGradleBuildFilename + " to " + modifiedGradleBuildFilename + "...");
            originalGradleBuildFile.renameTo(new File(pathToRootOfProject + File.separator + GRADLE_BUILD_FILE));
        }
    }

    /*
        GET TASK METHODS
     */

    public static final String WRITE_CLASSPATH_TASK = "WRITE_CLASSPATH_TASK";

    private String getPrintClasspathTask() {

        return AmplificationHelper.LINE_SEPARATOR + AmplificationHelper.LINE_SEPARATOR +
                "task " + WRITE_CLASSPATH_TASK + " << { " + AmplificationHelper.LINE_SEPARATOR +
                "    buildDir.mkdirs() " + AmplificationHelper.LINE_SEPARATOR +
                "    new File(buildDir, \"classpath.txt\").text = configurations.testCompile.asPath " + AmplificationHelper.LINE_SEPARATOR +
                "}" + AmplificationHelper.LINE_SEPARATOR;
    }

    private String readContentOfOrigianlGradleFile() {
        try (final BufferedReader reader = new BufferedReader(new FileReader(this.originalGradleBuildFile))) {
            return reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String getPitTask(CtType<?>... testClasses) {
        String pitTaskConfiguration = "";
        final String contentOfOriginalGradle = readContentOfOrigianlGradleFile();
        if (!contentOfOriginalGradle.contains("buildscript")) { // this means that there is no buildscript section in the original gradle build
            pitTaskConfiguration = contentOfOriginalGradle + getPitTaskConfiguration();
        } else {
            final String originalBuildscriptContent = getContentOfGivenSectionFromGivenString("buildscript", contentOfOriginalGradle);
            String buildscriptContentWithInjection =
                    AmplificationHelper.LINE_SEPARATOR + this.getMaybeCreateConfiguration() +
                            AmplificationHelper.LINE_SEPARATOR + getDependenciesForBuildscript() +
                            originalBuildscriptContent;
            //repositories management
            if (!originalBuildscriptContent.contains("repositories")) {
                buildscriptContentWithInjection += AmplificationHelper.LINE_SEPARATOR + this.getRepositoriesConfiguration();
            } else {
                final String originalRepositoriesContent = getContentOfGivenSectionFromGivenString("repositories", contentOfOriginalGradle);
                if (!originalRepositoriesContent.contains("url \"https://plugins.gradle.org/m2/\"")) {
                    String repositoriesContentWithInjection = originalRepositoriesContent;
                    repositoriesContentWithInjection += AmplificationHelper.LINE_SEPARATOR + this.getRepositoriesContent();
                    buildscriptContentWithInjection = buildscriptContentWithInjection.replace(originalRepositoriesContent, repositoriesContentWithInjection);
                } // else nothing, the needed repository is already there.
            }
            // adding the configuration.maybeCreate in anycase
            pitTaskConfiguration = contentOfOriginalGradle.replace(originalBuildscriptContent, buildscriptContentWithInjection);
        }
        return pitTaskConfiguration +
                AmplificationHelper.LINE_SEPARATOR + AmplificationHelper.LINE_SEPARATOR +
                getApplyPluginPit() +
                getPitTaskOptions(testClasses);
    }

    public static String getContentOfGivenSectionFromGivenString(String sectionName, String content) {
        final int indexOfSection = content.indexOf(sectionName);
        final int indexOfSectionWithoutSectionName = indexOfSection + sectionName.length();
        final int indexOfMatchCurlyBracket = getIndexOfMatchCurlyBracket(content.substring(indexOfSectionWithoutSectionName));
        final String contentWithoutSectionName = content.substring(indexOfSectionWithoutSectionName);
        final int indexOfFirstCurlyBracket = contentWithoutSectionName.indexOf("{");
        final String contentOfSection = contentWithoutSectionName.substring(indexOfFirstCurlyBracket + 1, indexOfMatchCurlyBracket);
        return contentOfSection;
    }

    public static int getIndexOfMatchCurlyBracket(String string) {
        final int indexOfFirstCurlyBracket = string.indexOf("{");
        int currentNumberOfCurlyBracket = 1;
        int currentIndex = indexOfFirstCurlyBracket;
        while (currentNumberOfCurlyBracket != 0) {
            currentIndex++;
            currentNumberOfCurlyBracket += string.charAt(currentIndex) == '{' ? 1 : 0;
            currentNumberOfCurlyBracket += string.charAt(currentIndex) == '}' ? -1 : 0;
        }
        return currentIndex;
    }

    private String getPitTaskConfiguration() {
        return AmplificationHelper.LINE_SEPARATOR +
                "buildscript {" + AmplificationHelper.LINE_SEPARATOR +
                getBuildScriptContent() +
                "}" + AmplificationHelper.LINE_SEPARATOR;
    }

    private String getApplyPluginPit() {
        return "apply plugin: 'info.solidsoft.pitest'" + AmplificationHelper.LINE_SEPARATOR;
    }

    private String getBuildScriptContent() {
        return getRepositoriesConfiguration() + AmplificationHelper.LINE_SEPARATOR +
                AmplificationHelper.LINE_SEPARATOR +
                getMaybeCreateConfiguration() +
                AmplificationHelper.LINE_SEPARATOR +
                getDependenciesForBuildscript();
    }

    private String getDependenciesForBuildscript() {
        return "    dependencies {" + AmplificationHelper.LINE_SEPARATOR +
                getDependenciesToPITAndOrDescartes() +
                "    }" + AmplificationHelper.LINE_SEPARATOR;
    }

    private String getDependenciesToPITAndOrDescartes() {
        return "       classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.3.0'" + AmplificationHelper.LINE_SEPARATOR +
                (this.isDescartesMode ? "       pitest 'eu.stamp-project:descartes:1.2.4'" : "") + AmplificationHelper.LINE_SEPARATOR;
    }

    private String getMaybeCreateConfiguration() {
        return "    configurations.maybeCreate('pitest')" + AmplificationHelper.LINE_SEPARATOR;
    }

    private String getRepositoriesConfiguration() {
        return "    repositories {" + AmplificationHelper.LINE_SEPARATOR +
                getRepositoriesContent() + AmplificationHelper.LINE_SEPARATOR +
                "    }";
    }

    private String getRepositoriesContent() {
        return "        maven {" + AmplificationHelper.LINE_SEPARATOR + " " +
                "            url \"https://plugins.gradle.org/m2/\"" + AmplificationHelper.LINE_SEPARATOR +
                "        }";
    }

    private String wrapWithSingleQuote(String option) {
        return "\'" + option + "\'";
    }

    private String getPitTaskOptions(CtType<?>... testClasses) {
        return AmplificationHelper.LINE_SEPARATOR + "pitest {" + AmplificationHelper.LINE_SEPARATOR +
                (this.filter != null && !this.filter.isEmpty() ?
                "    " + OPT_TARGET_CLASSES + "['" + this.filter + "']" + AmplificationHelper.LINE_SEPARATOR : "" ) +
                "    " + OPT_WITH_HISTORY + "true" + AmplificationHelper.LINE_SEPARATOR +
                "    " + OPT_VALUE_REPORT_DIR + AmplificationHelper.LINE_SEPARATOR +
                "    " + OPT_VALUE_FORMAT + AmplificationHelper.LINE_SEPARATOR +
                "    " + OPT_PIT_VERSION + this.wrapWithSingleQuote(this.pitVersion) + AmplificationHelper.LINE_SEPARATOR +
                "    " + PROPERTY_VALUE_TIMEOUT + " = " + this.timeoutMs + AmplificationHelper.LINE_SEPARATOR +
                (!this.JVMArgs.isEmpty() ?
                        "    " + PROPERTY_VALUE_JVM_ARGS + " = [" +
                                Arrays.stream(this.JVMArgs.split(",")).map(this::wrapWithSingleQuote).collect(Collectors.joining(",")) + "]"
                                + AmplificationHelper.LINE_SEPARATOR : "") +
                (testClasses != null ? "    " + OPT_TARGET_TESTS + "['" + Arrays.stream(testClasses).map(DSpotUtils::ctTypeToFullQualifiedName).collect(Collectors.joining(",")) + "']" + AmplificationHelper.LINE_SEPARATOR : "") +
                (!this.additionalClasspathElements.isEmpty() ?
                        "    " + OPT_ADDITIONAL_CP_ELEMENTS + "['" + this.additionalClasspathElements + "']" + AmplificationHelper.LINE_SEPARATOR : "") +
                "    " + (isDescartesMode ? OPT_MUTATION_ENGINE : OPT_MUTATORS + VALUE_MUTATORS_ALL) + AmplificationHelper.LINE_SEPARATOR +
                (!this.excludedClasses.isEmpty() ?
                        "    " + OPT_EXCLUDED_CLASSES + "['" + this.excludedClasses + "']" + AmplificationHelper.LINE_SEPARATOR : "") +
                "}" + AmplificationHelper.LINE_SEPARATOR;
    }

}
