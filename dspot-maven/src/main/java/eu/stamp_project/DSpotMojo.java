package eu.stamp_project;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.List;

@Mojo(name = "amplify-unit-tests", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class DSpotMojo extends AbstractMojo {

	/**
	 *	[mandatory] specify the path to the configuration file (format Java properties) of the target project (e.g. ./foo.properties).
	 */
	@Parameter(property = "path-to-properties")
	private String path_to_properties;

	/**
	 *	[optional] specify the list of amplifiers to use. Default with all available amplifiers.
	 *			 - StringLiteralAmplifier
	 *			 - NumberLiteralAmplifier
	 *			 - CharLiteralAmplifier
	 *			 - BooleanLiteralAmplifier
	 *			 - AllLiteralAmplifiers
	 *			 - MethodAdd
	 *			 - MethodRemove
	 *			 - TestDataMutator (deprecated)
	 *			 - StatementAdd
	 *			 - ReplacementAmplifier
	 *			 - None
	 */
	@Parameter(defaultValue = "None", property = "amplifiers")
	private List<String> amplifiers;

	/**
	 *	[optional] specify the number of amplification iterations. A larger number may help to improve the test criterion (e.g. a larger number of iterations may help to kill more mutants). This has an impact on the execution time: the more iterations, the longer DSpot runs.
	 */
	@Parameter(defaultValue = "3", property = "iteration")
	private Integer iteration;

	/**
	 *	[optional] specify the test adequacy criterion to be maximized with amplification
	 */
	@Parameter(defaultValue = "PitMutantScoreSelector", property = "test-criterion")
	private String test_criterion;

	/**
	 *	[optional] specify the maximum number of amplified tests that dspot keeps (before generating assertion)
	 */
	@Parameter(defaultValue = "200", property = "max-test-amplified")
	private Integer max_test_amplified;

	/**
	 *	[optional] fully qualified names of test classes to be amplified. If the value is all, DSpot will amplify the whole test suite. You can also use regex to describe a set of test classes. By default, DSpot selects all the tests (value all). You can use the value diff, to select tests according to a diff between two versions of the same program. Be careful, using --test diff, you must specify both properties folderPath and baseSha.
	 */
	@Parameter(defaultValue = "all", property = "test")
	private List<String> test;

	/**
	 *	specify the test cases to amplify
	 */
	@Parameter(property = "cases")
	private List<String> cases;

	/**
	 *	[optional] specify the output folder (default: dspot-report)
	 */
	@Parameter(property = "output-path")
	private String output_path;

	/**
	 *	[optional] if enabled, DSpot will remove the out directory if exists, else it will append the results to the exist files. (default: off)
	 */
	@Parameter(defaultValue = "false", property = "clean")
	private Boolean clean;

	/**
	 *	[optional, expert mode] specify the path to the .csv of the original result of Pit Test. If you use this option the selector will be forced to PitMutantScoreSelector
	 */
	@Parameter(property = "path-pit-result")
	private String path_pit_result;

	/**
	 *	Enable the descartes engine for Pit Mutant Score Selector.
	 */
	@Parameter(defaultValue = "false", property = "descartes")
	private Boolean descartes;

	/**
	 *	[optional] specify the automatic builder to build the project
	 */
	@Parameter(defaultValue = "MavenBuilder", property = "automatic-builder")
	private String automatic_builder;

	/**
	 *	specify the path to the maven home
	 */
	@Parameter(property = "maven-home")
	private String maven_home;

	/**
	 *	specify a seed for the random object (used for all randomized operation)
	 */
	@Parameter(defaultValue = "23", property = "randomSeed")
	private Long randomSeed;

	/**
	 *	specify the timeout value of the degenerated tests in millisecond
	 */
	@Parameter(defaultValue = "10000", property = "timeOut")
	private Integer timeOut;

	/**
	 *	Enable verbose mode of DSpot.
	 */
	@Parameter(defaultValue = "false", property = "verbose")
	private Boolean verbose;

	/**
	 *	Enable comment on amplified test: details steps of the Amplification.
	 */
	@Parameter(defaultValue = "false", property = "with-comment")
	private Boolean with_comment;

	/**
	 *	Disable the minimization of amplified tests.
	 */
	@Parameter(defaultValue = "false", property = "no-minimize")
	private Boolean no_minimize;

	/**
	 *	Enable this option to change working directory with the root of the project.
	 */
	@Parameter(defaultValue = "false", property = "working-directory")
	private Boolean working_directory;

	/**
	 *	run the example of DSpot and leave
	 */
	@Parameter(defaultValue = "false", property = "example")
	private Boolean example;

	/**
	 *	show this help
	 */
	@Parameter(defaultValue = "false", property = "help")
	private Boolean help;

	private final String automaticBuilderName = "MavenBuilder";

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Main.run(
					new Configuration(
							this.path_to_properties,
							JSAPOptions.buildAmplifiersFromString(this.amplifiers.toArray(new String[this.amplifiers.size()])),
							this.iteration,
							this.test,
							this.output_path,
							JSAPOptions.SelectorEnum.valueOf(this.test_criterion).buildSelector(),
							this.cases,
							this.randomSeed,
							this.timeOut,
							automaticBuilderName,
							this.maven_home,
							this.max_test_amplified,
							this.clean,
							!this.no_minimize
					)
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}