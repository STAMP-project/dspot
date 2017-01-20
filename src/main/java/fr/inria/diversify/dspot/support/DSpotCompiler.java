package fr.inria.diversify.dspot.support;

import fr.inria.diversify.runner.InputProgram;
import spoon.Launcher;
import spoon.SpoonModelBuilder;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/19/17
 */
public class DSpotCompiler {

    public DSpotCompiler(InputProgram program, String pathToDependencies) {
        String pathToSources = program.getAbsoluteSourceCodeDir() + ":" + program.getAbsoluteTestSourceCodeDir();
        this.dependencies = pathToDependencies;
        this.launcher = getSpoonModelOf(pathToSources, pathToDependencies);

        this.binaryOutputDirectory = new File(program.getProgramDir() + "/" + program.getTestClassesDir());

        this.sourceOutputDirectory = new File("tmpDir/tmpSrc_test");
        if (!this.sourceOutputDirectory.exists()) {
            this.sourceOutputDirectory.mkdir();
        }
    }

    public boolean compile(String pathToAdditionalDependencies) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        String[] sourcesArray = this.sourceOutputDirectory.getAbsolutePath().split(":");
        Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
        String[] dependenciesArray = (this.dependencies + ":" + pathToAdditionalDependencies).split(":");
        launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
        launcher.buildModel();

        SpoonModelBuilder modelBuilder = launcher.getModelBuilder();
        modelBuilder.setBinaryOutputDirectory(this.getBinaryOutputDirectory());
        return modelBuilder.compile(SpoonModelBuilder.InputType.CTTYPES);
    }

    public static Launcher getSpoonModelOf(String pathToSources, String pathToDependencies) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        String[] sourcesArray = pathToSources.split(":");
        Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
        String[] dependenciesArray = pathToDependencies.split(":");
        launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
        launcher.buildModel();

        return launcher;
    }

    public static boolean compile(String pathToSources, String dependencies, File binaryOutputDirectory) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(false);
        launcher.getEnvironment().setCommentEnabled(true);
        String[] sourcesArray = pathToSources.split(":");
        Arrays.stream(sourcesArray).forEach(launcher::addInputResource);
        String[] dependenciesArray = dependencies.split(":");
        launcher.getModelBuilder().setSourceClasspath(dependenciesArray);
        launcher.buildModel();

        SpoonModelBuilder modelBuilder = launcher.getModelBuilder();
        modelBuilder.setBinaryOutputDirectory(binaryOutputDirectory);
        return modelBuilder.compile(SpoonModelBuilder.InputType.CTTYPES);
    }

    private Launcher launcher;

    private File binaryOutputDirectory;

    private String dependencies;

    private File sourceOutputDirectory;

    public File getBinaryOutputDirectory() {
        return binaryOutputDirectory;
    }

    public File getSourceOutputDirectory() {
        return sourceOutputDirectory;
    }

    public String getDependencies() {
        return dependencies;
    }

    public Launcher getLauncher() {
        return launcher;
    }

}
