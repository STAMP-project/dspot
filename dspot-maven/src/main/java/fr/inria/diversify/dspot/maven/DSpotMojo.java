package fr.inria.diversify.dspot.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpot;

@Mojo(name = "mutationCoverage", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class DSpotMojo extends AbstractMojo {

	/**
	 * @deprecated path to dspot properties file. Use Maven Properties
	 */
	@Parameter(property = "path-to-properties")
	private String pathToProperties;

	@Parameter(defaultValue = "${project.basedir}", property = "project")
	private File project;
	
	//@Parameter(defaultValue = "${project.build.directory}/tempDir", property = "tmpDir")
	@Parameter(defaultValue = "target/tmpDir", property = "tmpDir")
	private String tmpDir;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Hello, world.");
		getLog().info("PathToProperties: " + getPathToProperties());
		getLog().info("Project: " + getProject());
		try {
			MyInputConfiguration configuration = new MyInputConfiguration(getPathToProperties(), getProject(),getTmpDir());
			getLog().info("before new DSpot");
			DSpot dSpot = new DSpot(configuration, 1);
			getLog().info("before amplifyTest");
			dSpot.amplifyTest("example.TestSuiteExample");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidSdkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getPathToProperties() {
		return pathToProperties;
	}

	private String getProject() {
		return project.toString();
	}
	
	private String getTmpDir() {
		return tmpDir;
	}

}
