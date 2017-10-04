package fr.inria.diversify.dspot.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "mutationCoverage", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class DSpotMojo extends AbstractMojo {

	// Command Line parameters -> fr.inria.stamp.Configuration
//	/**
//	 * @deprecated path to dspot properties file. Use Maven Properties
//	 */
//	@Parameter(property = "path-to-properties")
//	private String pathToConfigurationFile;

	@Parameter(defaultValue = "all", property = "amplifiers")
	private List<String> amplifiers;

	@Parameter(defaultValue = "3", property = "iteration")
	private Integer iteration;

	@Parameter(defaultValue = "PitMutantScoreSelector", property = "test-criterion")
	private String testCriterion;

	@Parameter(defaultValue = "all", property = "test")
	private String namesOfTestCases;

	@Parameter(defaultValue = "${project.build.directory}/dspot-report", property = "output-path")
	private String outputPath;

	@Parameter(defaultValue = "23", property = "randomSeed")
	private Long randomSeed;

	@Parameter(defaultValue = "10000", property = "timeOut")
	private Integer timeOutInMs;

	// Properties file parameters -> fr.inria.diversify.runner.InputConfiguration
	/*
	 *
	 */

	@Parameter(defaultValue = "${project.basedir}", property = "project")
	private File project;

	@Parameter(defaultValue = "${project.build.sourceDirectory}", property = "src")
	private File srcDir;

	@Parameter(defaultValue = "${project.build.testSourceDirectory}", property = "test")
	private File testDir;

	@Parameter(property = "filter")
	private String filter;

	@Parameter(defaultValue = "${env.M2_HOME}", property = "mavenHome")
	private File mavenHome;

	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("getAmplifiers(): " + getAmplifiers());
		System.out.println("getIteration(): " + getIteration());
		System.out.println("getTestCriterion(): " + getTestCriterion());
		System.out.println("getNamesOfTestCases(): " + getNamesOfTestCases());
		System.out.println("getOutputPath(): " + getOutputPath());
		System.out.println("getRandomSeed() : " + getRandomSeed() );
		System.out.println("getTimeOutInMs(): " + getTimeOutInMs());
		System.out.println("getProject(): " + getProject());
		System.out.println("getSrcDir(): " + getSrcDir());
		System.out.println("getTestDir(): " + getTestDir());
		System.out.println("getFilter(): " + getFilter());
		System.out.println("getMavenHome(): " + getMavenHome().toString());
		
		
//		try {
//			MyInputConfiguration configuration = new MyInputConfiguration(getPathToConfigurationFile(), getProject(),
//					getTmpDir());
//			getLog().info("before new DSpot");
//			DSpot dSpot = new DSpot(configuration, 1);
//			getLog().info("before amplifyTest");
//			//
//			AmplificationHelper.setSeedRandom(72);
//			AmplificationHelper.setTimeOutInMs(100000);
//
//			Main.createOutputDirectories(configuration);
//			if ("all".equals(test)) {
//				Main.amplifyAll(dSpot, configuration);
//			} else {
////				configuration.testClasses.forEach(testCase -> {
////					if (!configuration.namesOfTestCases.isEmpty()) {
////						amplifyOne(dspot, testCase, configuration.namesOfTestCases);
////					} else {
////						amplifyOne(dspot, testCase, Collections.EMPTY_LIST);
////					}
////				});
//			}
//			dSpot.cleanResources();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidSdkException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
//
//	public String getPathToConfigurationFile() {
//		return pathToConfigurationFile;
//	}

	public List<String> getAmplifiers() {
		return amplifiers;
	}

	public Integer getIteration() {
		return iteration;
	}

	public String getTestCriterion() {
		return testCriterion;
	}

	public String getNamesOfTestCases() {
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

}
