package eu.stamp_project;


import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;


@Mojo(name = "amplify-unit-tests", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class DSpotMojo extends AbstractMojo {
    @Override
    public void execute() {
        if (!targetModule.isEmpty()) {
            if (this.mavenProject.getBasedir().getAbsolutePath().endsWith(targetModule)) {
                this.absolutePathToProjectRoot =
                        this.mavenProject.getBasedir().getAbsolutePath().substring(0,
                                this.mavenProject.getBasedir().getAbsolutePath().length() - this.targetModule.length()
                        );
            } else {
                return;
            }
        } else {
            this.absolutePathToProjectRoot = this.mavenProject.getBasedir().getAbsolutePath();
        }
        final String[] args = DSpotMojo.removeBlank(new String[]{});
        Main.main(args);
    }

    private String absolutePathToProjectRoot;

    private static String[] removeBlank(String[] args) {
        return Arrays.stream(args).filter(( arg) -> !(arg.isEmpty())).toArray(String[]::new);
    }

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;
}

