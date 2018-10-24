package eu.stamp_project.automaticbuilder.gradle;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.Amplification;
import eu.stamp_project.mutant.pit.GradlePitTaskAndOptions;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.program.InputConfiguration;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.CMD_PIT_MUTATION_COVERAGE;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.OPT_ADDITIONAL_CP_ELEMENTS;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.OPT_EXCLUDED_CLASSES;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.OPT_MUTATION_ENGINE;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.OPT_MUTATORS;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.OPT_TARGET_CLASSES;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.OPT_TARGET_TESTS;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.OPT_VALUE_FORMAT;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.OPT_VALUE_REPORT_DIR;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.OPT_WITH_HISTORY;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.PROPERTY_VALUE_JVM_ARGS;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.PROPERTY_VALUE_TIMEOUT;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.VALUE_MUTATORS_ALL;
import static eu.stamp_project.mutant.pit.GradlePitTaskAndOptions.descartesMode;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 18/07/17.
 */
public class GradleAutomaticBuilder implements AutomaticBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleAutomaticBuilder.class);

    private static final String JAVA_PROJECT_CLASSPATH = "gjp_cp"; // Gradle Java Project classpath file

    private GradleInjector gradleInjector;

    public GradleAutomaticBuilder() {
        this.gradleInjector = new GradleInjector(
                InputConfiguration.get().getAbsolutePathToProjectRoot()
                        + File.separator + GradleInjector.GRADLE_BUILD_FILE
        );
    }

    @Override
    public String compileAndBuildClasspath() {
        this.compile();
        return this.buildClasspath();
    }

    @Override
    public void compile() {
        runTasks(InputConfiguration.get().getAbsolutePathToProjectRoot(),
                "clean", "compileJava", "test"
        );
    }

    @Override
    public String buildClasspath() {
        try {
            final File classpathFile = new File(InputConfiguration.get().getAbsolutePathToProjectRoot() + File.separator + JAVA_PROJECT_CLASSPATH);
            if (!classpathFile.exists()) {
                LOGGER.info("Classpath file for Gradle project doesn't exist, starting to build it...");
                LOGGER.info("Injecting  Gradle task to print project classpath on stdout...");
                this.gradleInjector.injectPrintClasspathTask(InputConfiguration.get().getAbsolutePathToProjectRoot());
                LOGGER.info("Retrieving project classpath...");
                byte[] taskOutput = cleanClasspath(runTasks(InputConfiguration.get().getAbsolutePathToProjectRoot(), "printClasspath4DSpot"));
                LOGGER.info("Writing project classpath on file " + JAVA_PROJECT_CLASSPATH + "...");
                FileOutputStream fos = new FileOutputStream(InputConfiguration.get().getAbsolutePathToProjectRoot() + File.separator + JAVA_PROJECT_CLASSPATH);
                fos.write(taskOutput);
                fos.close();
                this.gradleInjector.resetOriginalGradleBuildFile(InputConfiguration.get().getAbsolutePathToProjectRoot());
            }
            try (BufferedReader buffer = new BufferedReader(new FileReader(classpathFile))) {
                final String collect = buffer
                        .lines()
                        .collect(Collectors.joining());
                return Arrays.stream(collect.split(":"))
                        .filter(path -> new File(path).exists() && new File(path).isAbsolute())
                        .collect(Collectors.joining(":"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset() {
        //TODO Maybe we should change one time the the gradle and reset it at the end of the process
    }

    @Override
    public void runPit(String pathToRootOfProject) {
        runPit(pathToRootOfProject, null);
    }

    @Override
    public void runPit(String pathToRootOfProject, CtType<?>... testClasses) {
        try {
            LOGGER.info("Injecting  Gradle task to run Pit...");
            this.gradleInjector.injectPitTask(pathToRootOfProject, testClasses);
            LOGGER.info("Running Pit...");
            runTasks(pathToRootOfProject, CMD_PIT_MUTATION_COVERAGE);
            this.gradleInjector.resetOriginalGradleBuildFile(pathToRootOfProject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected byte[] runTasks(String pathToRootOfProject, String... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(new File(pathToRootOfProject)).connect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LOGGER.info("Run gradle tasks: {}", String.join(" ", tasks));
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
        LOGGER.info("Retrieved task output:");
        LOGGER.info(new String(taskOutput));
        LOGGER.info("Extracting project classpath from task output...");
        StringBuilder sb = new StringBuilder();
        String cleanCP = new String(taskOutput);
        String classPathPattern = "([\\/a-z0-9\\.\\-]*\\.jar|[\\/a-z0-9\\.\\-]*\\.zip)";
        Pattern p = Pattern.compile(classPathPattern);
        Matcher m = p.matcher(cleanCP);
        while (m.find()) {
            sb.append(m.group());
            sb.append(AmplificationHelper.PATH_SEPARATOR);
        }
        LOGGER.info("Project classpath from task output:");
        final String string = sb.toString();
        LOGGER.info("{}", string);
        return string.getBytes();
    }

    @Override
    public String getOutputDirectoryPit() {
        return GradlePitTaskAndOptions.OUTPUT_DIRECTORY_PIT;
    }
}
