package eu.stamp_project.automaticbuilder.gradle;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.utils.options.InputConfiguration;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.stream.Collectors;

import static eu.stamp_project.automaticbuilder.gradle.GradlePitTaskAndOptions.ARGUMENTS_SKIP_COMPILE_TEST;
import static eu.stamp_project.automaticbuilder.gradle.GradlePitTaskAndOptions.CMD_PIT_MUTATION_COVERAGE;

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
        runTasks(false, "clean", "compileTest");
    }

    @Override
    public String buildClasspath() {
        try {
            final File classpathFile = new File(InputConfiguration.get().getAbsolutePathToProjectRoot() + File.separator + "build/classpath.txt");
            if (!classpathFile.exists()) {
                LOGGER.info("Classpath file for Gradle project doesn't exist, starting to build it...");
                LOGGER.info("Injecting  Gradle task to print project classpath on stdout...");
                this.gradleInjector.injectPrintClasspathTask(InputConfiguration.get().getAbsolutePathToProjectRoot());
                LOGGER.info("Retrieving project classpath...");
                this.runTasks(false, GradleInjector.WRITE_CLASSPATH_TASK);
                LOGGER.info("Writing project classpath on file " + JAVA_PROJECT_CLASSPATH + "...");
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
    public void runPit() {
        runPit(null);
    }

    @Override
    public void runPit(CtType<?>... testClasses) {
        try {
            LOGGER.info("Injecting  Gradle task to run Pit...");
            this.gradleInjector.injectPitTask(InputConfiguration.get().getAbsolutePathToProjectRoot(), testClasses);
            LOGGER.info("Running Pit...");
            runTasks(true, CMD_PIT_MUTATION_COVERAGE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.gradleInjector.resetOriginalGradleBuildFile(InputConfiguration.get().getAbsolutePathToProjectRoot());
        }
    }

    protected void runTasks(boolean skipTest, String... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(new File(InputConfiguration.get().getAbsolutePathToProjectRoot())).connect();
        LOGGER.info("Run gradle tasks: {}", String.join(" ", tasks));
        try {
            final BuildLauncher buildLauncher = connection.newBuild()
                    .forTasks(tasks);
            if (skipTest) {
                buildLauncher.withArguments(ARGUMENTS_SKIP_COMPILE_TEST);
            }
            if (InputConfiguration.get().isVerbose()) {
                buildLauncher.setStandardError(System.err);
                buildLauncher.setStandardOutput(System.out);
            }
            buildLauncher.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            connection.close();
        }
    }

    @Override
    public String getOutputDirectoryPit() {
        return GradlePitTaskAndOptions.OUTPUT_DIRECTORY_PIT;
    }
}
