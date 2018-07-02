package eu.stamp_project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stamp_project.utils.sosiefier.InputConfiguration;
import eu.stamp_project.JSAPOptions.SelectorEnum;

@Mojo(name = "amplify-unit-tests", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class DSpotMojo extends AbstractMojo {

	private static final Logger LOGGER = LoggerFactory.getLogger(DSpotMojo.class);

	// Command Line parameters -> eu.stamp_project.Configuration

	private static final String BUILDER = "MavenBuilder";

	@Parameter(defaultValue = "MethodAdd", property = "amplifiers")
	private List<String> amplifiers;

	@Parameter(defaultValue = "3", property = "iteration")
	private Integer iteration;

	@Parameter(defaultValue = "PitMutantScoreSelector", property = "testcriterion")
	private String testCriterion;

	@Parameter(defaultValue = "200", property = "maxtestamplified")
	private Integer maxTestAmplified;

	@Parameter(defaultValue = "all", property = "tests")
	private List<String> namesOfTestCases;

	@Parameter(property = "cases")
	private List<String> namesOfTestMethods;

	@Parameter(defaultValue = "${project.build.directory}/dspot-report", property = "outputpath")
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

	// Properties file parameters -> eu.stamp_project.diversify.runner.InputConfiguration

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

	@Parameter(property = "filter")
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
				getNamesOfTestCases(), getOutputPath(), SelectorEnum.valueOf(getSelector()).buildSelector(),
				new ArrayList<String>(), getRandomSeed().longValue(), getTimeOutInMs().intValue(), BUILDER,
				getMavenHome().getAbsolutePath(), 200, false, true);
		
		InputConfiguration inputConfiguration;

		try {
			inputConfiguration = new InputConfiguration(getProject(), getSrcDir(), getTestDir(), getClassesDir(),
					getTestClassesDir(), getTempDir(), getFilter(), getMavenHome());
			Main.run(configuration, inputConfiguration);
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
		}
	}

	public List<String> getAmplifiers() {
		return amplifiers;
	}

	public Integer getIteration() {
		return iteration;
	}

	public String getTestCriterion() {
		return testCriterion;
	}

	public List<String> getNamesOfTestCases() {
		return namesOfTestCases;
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