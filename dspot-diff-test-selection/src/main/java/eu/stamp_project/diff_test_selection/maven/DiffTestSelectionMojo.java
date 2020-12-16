package eu.stamp_project.diff_test_selection.maven;

import eu.stamp_project.diff_test_selection.Main;
import eu.stamp_project.diff_test_selection.configuration.Configuration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;


@Mojo(name = "list")
public class DiffTestSelectionMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     *	[Mandatory] Specify the path to root directory of the project in the first version.
     */
    @Parameter(property = "path-dir-first-version")
    private String pathDirFirstVersion;

    /**
     *	[Mandatory] Specify the path to root directory of the project in the second version.
     */
    @Parameter(property = "path-dir-second-version")
    private String pathDirSecondVersion;

    /**
     *	[Optional] Specify the path of the output.
     */
    @Parameter(defaultValue = "", property = "output-path")
    private String outputPath;

    /**
     *	[Optional] Specify the format of the output. (For now, only the CSV format is available)
     */
    @Parameter(defaultValue = "CSV", property = "output-format")
    private String outputFormat;

    /**
     *	[Optional] In case of multi-module project, specify which module (a path from the project's root).
     */
    @Parameter(defaultValue = "", property = "module")
    private String module;

    /**
     *	[Optional] Specify the path of a diff file. If it is not specified, it will be computed using diff command line.
     */
    @Parameter(defaultValue = "", property = "path-to-diff")
    private String pathToDiff;

    /**
     *	[Optional]
     */
    @Parameter(defaultValue = "false", property = "use-second-version")
    private boolean useSecondVersionAsBasis;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Main.run(new Configuration(
                this.project.getBasedir().getAbsolutePath(),
                this.pathDirSecondVersion,
                this.outputPath,
                this.outputFormat,
                this.module,
                this.pathToDiff,
                this.useSecondVersionAsBasis
        ));
    }
}