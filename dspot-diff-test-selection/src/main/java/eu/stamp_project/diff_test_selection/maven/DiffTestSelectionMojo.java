package eu.stamp_project.diff_test_selection.maven;

import eu.stamp_project.diff_test_selection.Main;
import eu.stamp_project.diff_test_selection.configuration.Configuration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;


@Mojo(name = "list")
public class DiffTestSelectionMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    private String pathDirFirstVersion;

    /**
     * [Mandatory] Specify the path to root directory of the project in the second version.
     */
    @Parameter(property = "path-dir-second-version")
    private String pathDirSecondVersion;

    /**
     * [Optional] Specify the path of the output.
     */
    @Parameter(defaultValue = "", property = "output-path")
    private String outputPath;

    /**
     * [Optional] Specify the format of the output. (For now, only the CSV format is available)
     */
    @Parameter(defaultValue = "CSV", property = "output-format")
    private String outputFormat;

    /**
     * [Optional] Specify the path of a diff file. If it is not specified, it will be computed using diff command line.
     */
    @Parameter(defaultValue = "", property = "path-to-diff")
    private String pathToDiff;

    /**
     * Use the enhanced diff-test-selection. Select the test of the first version that hit the deletions, and the
     * tests of the second version that hit the additions.
     */
    @Parameter(defaultValue = "true", property = "enhanced")
    private boolean enhanced;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (new File(this.project.getBasedir().getAbsolutePath() + "/src").exists()) {
            final String module = this.project.getBasedir().getAbsolutePath().substring(this.pathDirSecondVersion.length());
            getLog().info("Running on:");
            getLog().info(this.project.getBasedir().getAbsolutePath());
            getLog().info(this.pathDirSecondVersion + "/" + module);
            Main.run(
                    new Configuration(
                            this.project.getBasedir().getAbsolutePath(),
                            this.pathDirSecondVersion + "/" + module,
                            this.outputPath,
                            this.outputFormat,
                            this.pathToDiff,
                            this.enhanced
                    )
            );
        }
    }
}