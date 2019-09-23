package eu.stamp_project;

import eu.stamp_project.utils.DSpotUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/01/19
 * <p>
 * This maven plugin will generate a default properties with minimal values to use dspot
 */
@Mojo(name = "generate-properties")
public class GeneratePropertiesMojo extends AbstractMojo {

    private final String COMMENT = "This properties file has been automatically generated using generate-properties of dspot-maven";

    /**
     * Configure the output path of the generated properties file.
     */
    @Parameter(defaultValue = "dspot.properties", property = "output")
    private String outputPath;

    /**
     * Maven project for which we want to produce a properties file.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    private Properties properties;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.properties = new Properties();
        final String rootPath = this.project.getBasedir().getAbsolutePath();
        if (this.project.getParent() != null) {
            final MavenProject topParent = getTopParent();
            final String pathOfTopParent = topParent.getBasedir().getAbsolutePath();
            this.properties.put(ConstantsProperties.PROJECT_ROOT_PATH.getName(), pathOfTopParent);
            this.properties.put(ConstantsProperties.MODULE.getName(), rootPath);
        } else {
            this.properties.put(ConstantsProperties.PROJECT_ROOT_PATH.getName(), rootPath);
        }
        this.properties.put(ConstantsProperties.SRC_CODE.getName(), this.project.getCompileSourceRoots().get(0));
        this.properties.put(ConstantsProperties.TEST_SRC_CODE.getName(), this.project.getTestCompileSourceRoots().get(0));
        final File testSrcDirectory = new File(this.project.getCompileSourceRoots().get(0));
        this.properties.put(ConstantsProperties.PIT_FILTER_CLASSES_TO_KEEP.getName(), buildFilter(testSrcDirectory));
        this.output();
    }

    private String buildFilter(File testSrcDirectory) {
        try {
            final Path pathToTopPackage = Files.walk(Paths.get(testSrcDirectory.getAbsolutePath()))
                    .filter(path -> path.toFile().isDirectory())
                    .filter(path -> path.toFile().listFiles(pathname -> pathname.getAbsolutePath().endsWith(".java")).length > 0)
                    .findFirst()
                    .get();
            return pathToTopPackage.toFile()
                    .getAbsolutePath()
                    .substring(DSpotUtils.shouldAddSeparator.apply(testSrcDirectory.getAbsolutePath()).length())
                    .replaceAll("/", ".") + "*";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MavenProject getTopParent() {
        MavenProject parent = this.project.getParent();
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    private void output() {
        try {
            this.properties.store(new FileWriter(this.project.getBasedir().getAbsolutePath() + outputPath), COMMENT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // VISIBLE FOR TESTING
    String getOutputPath() {
        return this.outputPath;
    }

    MavenProject getProject() {
        return this.project;
    }
}
