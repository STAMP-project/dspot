package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.mutant.pit.PitResult;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 18/07/17.
 */
public class GradleAutomaticBuilder implements AutomaticBuilder {

    private static final String JAVA_PROJECT_CLASSPATH = "gjp_cp"; // Gradle Java Project classpath file

    private static final String GRADLE_BUILD_FILE = "gradle.build";

    @Override
    public void compile(String pathToRootOfProject) {

    }

    @Override
    public String buildClasspath(String pathToRootOfProject) {
        try {
            final File classpathFile = new File(pathToRootOfProject + File.separator + JAVA_PROJECT_CLASSPATH);
            if (!classpathFile.exists()) {
                byte[] taskOutput = cleanClasspath(runTasks(pathToRootOfProject,"printClasspath4DSpot"));
                FileOutputStream fos = new FileOutputStream(pathToRootOfProject + File.separator + JAVA_PROJECT_CLASSPATH);
                fos.write(taskOutput);
                fos.close();
            }
            try (BufferedReader buffer = new BufferedReader(new FileReader(classpathFile))) {
                return buffer.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PitResult> runPit(String pathToRootOfProject, CtType<?> testClass) {
        return null;
    }

    @Override
    public List<PitResult> runPit(String pathToRootOfProject) {
        return null;
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
            System.out.println(outputStream.toString());
        } catch (Exception e) {
            new RuntimeException(e);
        } finally {
            connection.close();
        }
        return outputStream.toByteArray();
    }

    private byte[] cleanClasspath(byte[] taskOutput) {
        String cleanCP = new String(taskOutput);
        String classPathPattern = ".*(\\/[a-zA-Z0-9\\/\\.\\-]+\\.jar).*";

        Pattern p = Pattern.compile(classPathPattern);

        Matcher m = p.matcher(cleanCP);
        return m.group().getBytes();
    }

}
