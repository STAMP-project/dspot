package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.mutant.pit.PitResult;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.Task;
import spoon.reflect.declaration.CtType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 18/07/17.
 */
public class GradleAutomaticBuilder implements AutomaticBuilder {

    @Override
    public void compile(String pathToRootOfProject) {

    }

    @Override
    public String buildClasspath(String pathToRootOfProject) {
        runTasks(pathToRootOfProject,"printClasspath4DSpot");

        return null;
    }

    @Override
    public List<PitResult> runPit(String pathToRootOfProject, CtType<?> testClass) {
        return null;
    }

    @Override
    public List<PitResult> runPit(String pathToRootOfProject) {
        return null;
    }

    protected void runTasks(String pathToRootOfProject, String... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(new File(pathToRootOfProject)).connect();

        try {
            BuildLauncher build = connection.newBuild();
            build.forTasks(tasks);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            build.setStandardOutput(outputStream);
            build.setStandardError(outputStream);
            build.run();
            FileOutputStream fos = new FileOutputStream("src/test/resources/test-projects/cp");
            fos.write(outputStream.toByteArray());
            fos.close();
        } catch (Exception e) {
            new RuntimeException(e);
        } finally {
            connection.close();
        }
    }

    protected void runClasspathTask(String pathToRootOfProject, String... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(new File(pathToRootOfProject)).connect();

        try {

            GradleProject project = connection.getModel(GradleProject.class);

            for (Task task : project.getTasks()) {
                System.out.println("    " + task.getName());
            }

            BuildLauncher build = connection.newBuild();

            build.forTasks(tasks);


            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            build.setStandardOutput(outputStream);
            build.setStandardError(outputStream);
            build.run();
            FileOutputStream fos = new FileOutputStream("src/test/resources/test-projects/cp");
            fos.write(outputStream.toByteArray());
            fos.close();
        } catch (Exception e) {
            new RuntimeException(e);
        } finally {
            connection.close();
        }
    }
}
