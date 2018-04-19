package eu.stamp_project;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.Configuration;
import fr.inria.stamp.JSAPOptions;
import fr.inria.stamp.JSAPOptions.SelectorEnum;
import fr.inria.stamp.Main;

@Mojo(name = "amplify-unit-tests", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class DSpotMojo extends AbstractMojo {

	// Command Line parameters -> fr.inria.stamp.Configuration

	private static final String BUILDER = "MavenBuilder";

	@Parameter(defaultValue = "MethodAdd", property = "amplifiers")
	private List<String> amplifiers;

	@Parameter(defaultValue = "3", property = "iteration")
	private Integer iteration;

//	@Parameter(defaultValue = "PitMutantScoreSelector", property = "test-criterion")
//	private String testCriterion;

	@Parameter(defaultValue = "200", property = "max-test-amplified")
	private Integer maxTestAmplified;
	
//	@Parameter(defaultValue = "all", property = "test")
//	private List<String> namesOfTestCases;
	
	@Parameter( property = "cases")
	private List<String> namesOfTestMethods;

	@Parameter(defaultValue = "${project.build.directory}/dspot-report", property = "output-path")
	private String outputPath;

	@Parameter(defaultValue = "false", property = "clean")
	private Boolean clean;

	@Parameter(property = "descartes")
	private Boolean descartes;

	@Parameter(defaultValue = "23", property = "randomSeed")
	private Long randomSeed;

	@Parameter(defaultValue = "10000", property = "timeOut")
	private Integer timeOutInMs;

	@Parameter(defaultValue = "PitMutantScoreSelector", property = "selector")
	private String selector;

	// Properties file parameters -> fr.inria.diversify.runner.InputConfiguration

	@Parameter(defaultValue = "${project.basedir}", property = "project")
	private File project;

	@Parameter(defaultValue = "${project.build.sourceDirectory}", property = "src")
	private File srcDir;

	@Parameter(defaultValue = "${project.build.testSourceDirectory}", property = "test-src")
	private File testDir;

	@Parameter(defaultValue = "${project.build.outputDirectory}", property = "classes")
	private File classesDir;

	@Parameter(defaultValue = "${project.build.testOutputDirectory}", property = "test-classes")
	private File testClassesDir;

	@Parameter(defaultValue = "${project.build.directory}/tempDir", property = "temp-dir")
	private File tempDir;

	@Parameter(defaultValue = "example.*", property = "filter")
	private String filter;

	@Parameter(defaultValue = "${env.M2_HOME}", property = "mavenHome")
	private File mavenHome;

	public void execute() throws MojoExecutionException, MojoFailureException {

		Configuration configuration = new Configuration(
				// path to file
				null,
				// Amplifiers
				JSAPOptions.buildAmplifiersFromString(getAmplifiers().toArray(new String[0])),
				// Iteration
				getIteration(),
				// testClases
				getNamesOfTestCases(), getOutputPath(), 
				SelectorEnum.valueOf(getSelector()).buildSelector(),
				new ArrayList<String>(), 
				getRandomSeed().longValue(),
				getTimeOutInMs().intValue(),
				BUILDER,
				getMavenHome().getAbsolutePath(), 200, false, true);

		InputConfiguration inputConfiguration;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();
        for(URL url: urls){
        	System.out.println(url.getFile());
        }
		

		try {
			inputConfiguration = new InputConfiguration(getProject(), getSrcDir(), getTestDir(), getClassesDir(),
					getTestClassesDir(), getTempDir(), getFilter(), getMavenHome());
			Main.run(configuration, inputConfiguration);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<String> getAmplifiers() {
		return amplifiers;
	}

	public Integer getIteration() {
		return iteration;
	}

//	public String getTestCriterion() {
//		return testCriterion;
//	}

	public List<String> getNamesOfTestCases() {
		List<String> toReturn = new ArrayList<String>();
		toReturn.add("example.TestSuiteExample");
		return toReturn;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public Long getRandomSeed() {
		return randomSeed;
	}

	public Integer getTimeOutInMs() {
		return timeOutInMs;
	}

	public File getProject() {
		return project;
	}

	public File getSrcDir() {
		return srcDir;
	}

	public File getTestDir() {
		return testDir;
	}

	public String getFilter() {
		return filter;
	}

	public File getMavenHome() {
		return mavenHome;
	}

	public File getClassesDir() {
		return classesDir;
	}

	public File getTestClassesDir() {
		return testClassesDir;
	}

	public File getTempDir() {
		return tempDir;
	}

	public String getSelector() {
		return selector;
	}

}
