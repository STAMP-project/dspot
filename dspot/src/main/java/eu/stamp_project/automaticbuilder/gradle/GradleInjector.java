package eu.stamp_project.automaticbuilder.gradle;

import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.stream.Collectors;

import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.*;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.OPT_EXCLUDED_CLASSES;

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

    private String pathToOriginalGradleBuildFile;

    private File originalGradleBuildFile;

    public GradleInjector(String absolutePathToOriginalGradleBuildFile) {
        this.pathToOriginalGradleBuildFile = absolutePathToOriginalGradleBuildFile;
        this.originalGradleBuildFile = new File(absolutePathToOriginalGradleBuildFile);
        if (!this.originalGradleBuildFile.exists()) {
            throw new RuntimeException(absolutePathToOriginalGradleBuildFile + " does not exists!");
        }
    }


    public void injectPrintClasspathTask(String pathToRootOfProject) throws IOException {

        String originalGradleBuildFilename = pathToRootOfProject + File.separator + GRADLE_BUILD_FILE;
        File gradleBuildFile = new File(originalGradleBuildFilename);
        makeBackup(gradleBuildFile);

        String printClasspathTask = getPrintClasspathTask();

        Files.write(Paths.get(originalGradleBuildFilename), printClasspathTask.getBytes(), StandardOpenOption.APPEND);

        LOGGER.info("Injected following Gradle task in the Gradle build file {}:", originalGradleBuildFilename);
        LOGGER.info("{}", printClasspathTask);
    }

    @Deprecated
    public void injectPitTask(String pathToRootOfProject) throws IOException {
        injectPitTask(pathToRootOfProject, null);
    }

    public void injectPitTask(String pathToRootOfProject, CtType<?>... testClasses) throws IOException {

        String originalGradleBuildFilename = pathToRootOfProject + File.separator + GRADLE_BUILD_FILE;
        File gradleBuildFile = new File(originalGradleBuildFilename);
        makeBackup(gradleBuildFile);

        String pitTask = getPitTask(testClasses);

        Files.write(Paths.get(originalGradleBuildFilename), pitTask.getBytes(), StandardOpenOption.APPEND);

        LOGGER.info("Injected following Gradle task in the Gradle build file " + originalGradleBuildFilename + ":" + AmplificationHelper.LINE_SEPARATOR + " ");
        LOGGER.info(pitTask);
    }

    /*
        ORIGINAL GRADLE BUILD MANAGEMENT
     */

    void makeBackup(File gradleBuildFile) throws IOException {
        LOGGER.info("Making backup copy of original Gradle file...");

        final String originalGradleBuildFilename = gradleBuildFile.getPath();
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

    private String getPrintClasspathTask() {
        return AmplificationHelper.LINE_SEPARATOR + "task printClasspath4DSpot {" + AmplificationHelper.LINE_SEPARATOR +
                "    doLast {" + AmplificationHelper.LINE_SEPARATOR +
                "        configurations.testRuntime.each { println it }" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "}";
    }

    private String getPitTask() {
        return getPitTask(null);
    }


    private String getPitTask(CtType<?>... testClasses) {
        return getPitTaskConfiguration() + getPitTaskOptions(testClasses);
    }

    private String getPitTaskConfiguration() {
        return AmplificationHelper.LINE_SEPARATOR + "buildscript {" + AmplificationHelper.LINE_SEPARATOR +
                "    repositories {" + AmplificationHelper.LINE_SEPARATOR +
                (descartesMode ? "        mavenLocal()" : "") + AmplificationHelper.LINE_SEPARATOR +
                "        maven {" + AmplificationHelper.LINE_SEPARATOR + " " +
                "            url \"https://plugins.gradle.org/m2/\"" + AmplificationHelper.LINE_SEPARATOR +
                "        }" + AmplificationHelper.LINE_SEPARATOR +
                AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                AmplificationHelper.LINE_SEPARATOR +
                "    configurations.maybeCreate(\"pitest\")" + AmplificationHelper.LINE_SEPARATOR +
                AmplificationHelper.LINE_SEPARATOR +
                "    dependencies {" + AmplificationHelper.LINE_SEPARATOR +
                "       classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.1.11'" + AmplificationHelper.LINE_SEPARATOR +
                (descartesMode ? "       pitest 'eu.stamp_project.stamp:descartes:0.1-SNAPSHOT'" : "") + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "}" + AmplificationHelper.LINE_SEPARATOR +
                AmplificationHelper.LINE_SEPARATOR +
                "apply plugin: 'info.solidsoft.pitest'" + AmplificationHelper.LINE_SEPARATOR;
    }

    private String wrapWithSingleQuote(String option) {
        return "\'" + option + "\'";
    }

    private String getPitTaskOptions(CtType<?>... testClasses) {
        return AmplificationHelper.LINE_SEPARATOR + AmplificationHelper.LINE_SEPARATOR + "pitest {" + AmplificationHelper.LINE_SEPARATOR +
                "    " + OPT_TARGET_CLASSES + "['" + InputConfiguration.get().getFilter() + "']" + AmplificationHelper.LINE_SEPARATOR +
                "    " + OPT_WITH_HISTORY + "true" + AmplificationHelper.LINE_SEPARATOR +
                "    " + OPT_VALUE_REPORT_DIR + AmplificationHelper.LINE_SEPARATOR +
                "    " + OPT_VALUE_FORMAT + AmplificationHelper.LINE_SEPARATOR +
                (!InputConfiguration.get().getTimeoutPit().isEmpty() ?
                        "    " + PROPERTY_VALUE_TIMEOUT + " = " + InputConfiguration.get().getTimeoutPit().isEmpty() : "") + AmplificationHelper.LINE_SEPARATOR +
                (!InputConfiguration.get().getJVMArgs().isEmpty() ?
                        "    " + PROPERTY_VALUE_JVM_ARGS + " = [" +
                                Arrays.stream(InputConfiguration.get().getJVMArgs().split(" ")).map(this::wrapWithSingleQuote).collect(Collectors.joining(",")) + "]"
                        : "") + AmplificationHelper.LINE_SEPARATOR +
                (testClasses != null ? "    " + OPT_TARGET_TESTS + "['" + Arrays.stream(testClasses).map(DSpotUtils::ctTypeToFullQualifiedName).collect(Collectors.joining(",")) + "']" : "") + AmplificationHelper.LINE_SEPARATOR +
                (!InputConfiguration.get().getAdditionalClasspathElements().isEmpty() ?
                        "    " + OPT_ADDITIONAL_CP_ELEMENTS + "['" + InputConfiguration.get().getAdditionalClasspathElements() + "']" : "") + AmplificationHelper.LINE_SEPARATOR +
                (descartesMode ? "    " + OPT_MUTATION_ENGINE + AmplificationHelper.LINE_SEPARATOR + "    " + getDescartesMutators() :
                        "    " + OPT_MUTATORS + VALUE_MUTATORS_ALL) + AmplificationHelper.LINE_SEPARATOR +
                (!InputConfiguration.get().getExcludedClasses().isEmpty() ?
                        "    " + OPT_EXCLUDED_CLASSES + "['" + InputConfiguration.get().getExcludedClasses() + "']" : "") + AmplificationHelper.LINE_SEPARATOR +
                "}" + AmplificationHelper.LINE_SEPARATOR;
    }

    private String getDescartesMutators() {
        return "mutators = " + InputConfiguration.get().getDescartesMutators();
    }

}
