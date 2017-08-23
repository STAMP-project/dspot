package fr.inria.diversify.dspot.maven;

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
import fr.inria.diversify.runner.InputConfiguration;

@Mojo(name = "mutationCoverage", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class DSpotMojo extends AbstractMojo {

	/**
	 * path to dspot properties file.
	 */
	@Parameter(property = "path-to-properties")
	private String pathToProperties;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Hello, world.");
		getLog().info(getPathToProperties().toString());
		try {
			InputConfiguration configuration = new InputConfiguration(getPathToProperties());
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

	public String getPathToProperties() {
		return pathToProperties;
	}

}
