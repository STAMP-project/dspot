package eu.stamp_project;

import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.options.*;
import eu.stamp_project.utils.program.ConstantsProperties;
import eu.stamp_project.utils.program.InputConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Mojo(name = "amplify-unit-tests", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class DSpotMojo extends AbstractMojo {

    /**
     * [mandatory] specify the path to the configuration file (format Java properties) of the target project (e.g. ./foo.properties).
     */
    @Parameter(property = "path-to-properties")
    private String pathToProperties;

    /**
     * [optional] specify the list of amplifiers to use. By default, DSpot does not use any amplifiers (None) and applies only assertion amplification.
     * Possible values are:
     * - MethodAdd
     * - MethodRemove
     * - TestDataMutator
     * - MethodGeneratorAmplifier
     * - ReturnValueAmplifier
     * - StringLiteralAmplifier
     * - NumberLiteralAmplifier
     * - BooleanLiteralAmplifier
     * - CharLiteralAmplifier
     * - AllLiteralAmplifiers
     * - NullifierAmplifier
     * - None
     */
    @Parameter(defaultValue = "None", property = "amplifiers")
    private List<String> amplifiers;

    /**
     * [optional] specify the number of amplification iterations. A larger number may help to improve the test criterion (e.g. a larger number of iterations may help to kill more mutants). This has an impact on the execution time: the more iterations, the longer DSpot runs.
     */
    @Parameter(defaultValue = "3", property = "iteration")
    private Integer iteration;

    /**
     * [optional] specify the test adequacy criterion to be maximized with amplification.
     * Possible values are:
     * - PitMutantScoreSelector
     * - JacocoCoverageSelector
     * - TakeAllSelector
     * - ChangeDetectorSelector
     */
    @Parameter(defaultValue = "PitMutantScoreSelector", property = "test-criterion")
    private String testCriterion;

    /**
     * [optional] specify the Pit output format.
     * Possible values are:
     * - XML
     * - CSV
     */
    @Parameter(defaultValue = "XML", property = "pit-output-format")
    private String pitOutputFormat;

    /**
     * [optional] specify a Bugdetizer.
     * Possible values are:
     * - RandomBudgetizer
     * - TextualDistanceBudgetizer
     * - SimpleBudgetizer
     */
    @Parameter(defaultValue = "RandomBudgetizer", property = "budgetizer")
    private String budgetizer;

    /**
     * [optional] specify the maximum number of amplified tests that dspot keeps (before generating assertion)
     */
    @Parameter(defaultValue = "200", property = "max-test-amplified")
    private Integer maxTestAmplified;

    /**
     * [optional] fully qualified names of test classes to be amplified. If the value is all, DSpot will amplify the whole test suite. You can also use regex to describe a set of test classes. By default, DSpot selects all the tests (value all).
     */
    @Parameter(defaultValue = "all", property = "test")
    private List<String> test;

    /**
     * specify the test cases to amplify
     */
    @Parameter(property = "test-cases")
    private List<String> testCases;

    /**
     * [optional] specify the output folder
     */
    @Parameter(defaultValue = "target/dspot/output", property = "output-path")
    private String outputPath;

    /**
     * [optional] if enabled, DSpot will remove the out directory if exists, else it will append the results to the exist files.
     */
    @Parameter(defaultValue = "false", property = "clean")
    private Boolean clean;

    /**
     * [optional, expert mode] specify the path to the .xml or .csv of the original result of Pit Test. If you use this option the selector will be forced to PitMutantScoreSelector
     */
    @Parameter(property = "path-pit-result")
    private String pathPitResult;

    /**
     * [optional, expert] enable this option will make DSpot computing the mutation score of only one test class (the first pass through --test command line option)
     */
    @Parameter(defaultValue = "false", property = "target-one-test-class")
    private Boolean targetOneTestClass;

    /**
     * Enable the descartes engine for Pit Mutant Score Selector.
     */
    @Parameter(defaultValue = "true", property = "descartes")
    private Boolean descartes;

    /**
     * Enable the gregor engine for Pit Mutant Score Selector.
     */
    @Parameter(defaultValue = "false", property = "gregor")
    private Boolean gregor;

    /**
     * [optional] specify the automatic builder to build the project
     */
    @Parameter(defaultValue = "", property = "automatic-builder")
    private String automaticBuilder;

    /**
     * specify the path to the maven home
     */
    @Parameter(property = "maven-home")
    private String mavenHome;

    /**
     * specify a seed for the random object (used for all randomized operation)
     */
    @Parameter(defaultValue = "23", property = "random-seed")
    private Long randomSeed;

    /**
     * specify the timeout value of the degenerated tests in millisecond
     */
    @Parameter(defaultValue = "10000", property = "time-out")
    private Integer timeOut;

    /**
     * Enable verbose mode of DSpot.
     */
    @Parameter(defaultValue = "false", property = "verbose")
    private Boolean verbose;

    /**
     * Enable comment on amplified test: details steps of the Amplification.
     */
    @Parameter(defaultValue = "false", property = "with-comment")
    private Boolean withComment;

    /**
     * Disable the minimization of amplified tests.
     */
    @Parameter(defaultValue = "false", property = "no-minimize")
    private Boolean noMinimize;

    /**
     * Enable this option to change working directory with the root of the project.
     */
    @Parameter(defaultValue = "false", property = "working-directory")
    private Boolean workingDirectory;

    /**
     * Enable the creation of a new test class.
     */
    @Parameter(defaultValue = "false", property = "generate-new-test-class")
    private Boolean generateNewTestClass;

    /**
     * If enabled, DSpot keeps original test methods of the amplified test class.
     */
    @Parameter(defaultValue = "false", property = "keep-original-test-methods")
    private Boolean keepOriginalTestMethods;

    /**
     * If enabled, DSpot will use maven to execute the tests.
     */
    @Parameter(defaultValue = "false", property = "use-maven-to-exe-test")
    private Boolean useMavenToExeTest;

    /**
     * If enabled, DSpot will generate assertions for values that seems like to be paths.
     */
    @Parameter(defaultValue = "false", property = "allow-path-in-assertions")
    private Boolean allowPathInAssertions;

    /**
     * run the example of DSpot and leave
     */
    @Parameter(defaultValue = "false", property = "example")
    private Boolean example;

    /**
     * show this help
     */
    @Parameter(defaultValue = "false", property = "help")
    private Boolean help;

    private final String AUTOMATIC_BUILDER_NAME = "MAVEN";

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * Enable the selection of the test to be amplified from a csv file.
     * This parameter is a path that must point to a csv file that list test classes and their test methods in the following format:
     * test-class-name;test-method-1;test-method-2;test-method-3;...
     * If this parameter is used, DSpot will ignore the value used in the parameter test and cases
     * It is recommended to use an absolute path
     */
    @Parameter(defaultValue = "", property = "path-to-test-list-csv")
    private String pathToTestListCsv = "";

    /**
     * Allows to specify the path to the second version through command line, rather than using properties file.
     * This parameter is the same than {@link eu.stamp_project.utils.program.ConstantsProperties#PATH_TO_SECOND_VERSION}
     * If this parameter is used, DSpot will ignore the value used in the properties file.
     * It is recommended to use an absolute path
     */
    @Parameter(defaultValue = "", property = "path-to-second-version")
    private String pathToSecondVersion = "";

    @Override
    public void execute() {
        if (this.help) {
            JSAPOptions.showUsage();
        }
        Properties properties = initializeProperties();
        if (properties.getProperty(ConstantsProperties.MODULE.getName()) != null) {
            if (!this.project.getBasedir().getAbsolutePath().endsWith(ConstantsProperties.MODULE.get(properties))) {
                return;
            } else {
                getLog().error(properties.toString());
                properties.put(ConstantsProperties.PROJECT_ROOT_PATH.getName(),
                        ConstantsProperties.PROJECT_ROOT_PATH.get(properties).substring(0,
                                ConstantsProperties.PROJECT_ROOT_PATH.get(properties).length() - ConstantsProperties.MODULE.get(properties).length()
                        )
                );
                getLog().error(properties.toString());
            }
        }

        if (this.amplifiers.size() == 1 &&
                this.amplifiers.get(0).contains(AmplificationHelper.PATH_SEPARATOR)) {
            this.amplifiers = Arrays.stream(this.amplifiers.get(0).split(AmplificationHelper.PATH_SEPARATOR))
                    .collect(Collectors.toList());
        }
        try {

            Configuration.configure(
                    properties,
                    this.amplifiers,
                    this.testCriterion,
                    this.budgetizer,
                    this.pitOutputFormat,
                    this.pathPitResult,
                    this.AUTOMATIC_BUILDER_NAME,
                    this.outputPath,
                    this.iteration,
                    this.randomSeed,
                    this.timeOut,
                    this.maxTestAmplified,
                    this.clean,
                    this.verbose,
                    this.workingDirectory,
                    this.withComment,
                    this.generateNewTestClass,
                    this.keepOriginalTestMethods,
                    this.gregor,
                    this.descartes,
                    this.useMavenToExeTest,
                    this.targetOneTestClass,
                    this.allowPathInAssertions,
                    this.test,
                    this.testCases
            );

            if (!this.pathToTestListCsv.isEmpty()) {
                // clear both list of test classes and test cases
                InputConfiguration.get().getTestCases().clear();
                InputConfiguration.get().getTestClasses().clear();
                // add all test classes and test cases from the csv file
                try (BufferedReader buffer = new BufferedReader(new FileReader(this.pathToTestListCsv))) {
                    buffer.lines().forEach(line -> {
                                final String[] splittedLine = line.split(";");
                                InputConfiguration.get().addTestClasses(splittedLine[0]);
                                for (int i = 1; i < splittedLine.length; i++) {
                                    InputConfiguration.get().addTestCase(splittedLine[i]);
                                }
                            }
                    );
                }
            }
            Main.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // visible for testing...
    @NotNull
    Properties initializeProperties() {
        Properties properties = new Properties();
        final String absolutePathProjectRoot = project.getBasedir().getAbsolutePath();
        properties.setProperty(ConstantsProperties.PROJECT_ROOT_PATH.getName(), absolutePathProjectRoot);
        final Build build = project.getBuild();
        properties.setProperty(ConstantsProperties.SRC_CODE.getName(),
                DSpotUtils.removeProjectRootIfAbsoluteAndAddSeparator(absolutePathProjectRoot, build.getSourceDirectory())
        );
        properties.setProperty(ConstantsProperties.TEST_SRC_CODE.getName(),
                DSpotUtils.removeProjectRootIfAbsoluteAndAddSeparator(absolutePathProjectRoot, build.getTestSourceDirectory())
        );
        properties.setProperty(ConstantsProperties.SRC_CLASSES.getName(),
                DSpotUtils.removeProjectRootIfAbsoluteAndAddSeparator(absolutePathProjectRoot, build.getOutputDirectory())
        );
        properties.setProperty(ConstantsProperties.TEST_CLASSES.getName(),
                DSpotUtils.removeProjectRootIfAbsoluteAndAddSeparator(absolutePathProjectRoot, build.getTestOutputDirectory())
        );
        // TODO checks that we can use an empty module for multi module project
        // TODO the guess here is that the user will launch the plugin from the root of the targeted module
        // TODO and thus, we do not need to compute the relative path from its parents
        // TODO however, it may lacks some dependencies and the user should run a resolve dependency goal
        // TODO before using the dspot plugin
        // TODO we must maybe need to use a correct lifecycle
        properties.setProperty(ConstantsProperties.MODULE.getName(), "");
        if (this.pathToProperties != null && !this.pathToProperties.isEmpty()) {
            try {
                properties.load(new FileInputStream(this.pathToProperties));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    /*
        Setters are used for testing
     */
    void setPathToProperties(String pathToProperties) {
        this.pathToProperties = pathToProperties;
    }

    void setAmplifiers(List<String> amplifiers) {
        this.amplifiers = amplifiers;
    }

    void setIteration(Integer iteration) {
        this.iteration = iteration;
    }

    void setTestCriterion(String testCriterion) {
        this.testCriterion = testCriterion;
    }

    void setTestClassesNames(List<String> test) {
        this.test = test;
    }

    void setTestMethods(List<String> cases) {
        this.testCases = cases;
    }

    void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    void setPathPitResult(String pathPitResult) {
        this.pathPitResult = pathPitResult;
    }

    void setPathToTestListCsv(String pathToTestListCsv) {
        this.pathToTestListCsv = pathToTestListCsv;
    }
}
