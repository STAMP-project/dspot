package eu.stamp_project;

import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.options.AmplifierEnum;
import eu.stamp_project.utils.options.BudgetizerEnum;
import eu.stamp_project.utils.options.JSAPOptions;
import eu.stamp_project.utils.options.SelectorEnum;
import eu.stamp_project.utils.program.ConstantsProperties;
import eu.stamp_project.utils.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Mojo(name = "amplify-unit-tests", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class DSpotMojo extends AbstractMojo {

    /**
     * [optional] specify the path to the configuration file (format Java properties) of the target project (e.g. ./foo.properties).
     */
    @Parameter(property = "path-to-properties", defaultValue = "")
    private String pathToProperties;

    /**
     * [optional] specify the list of amplifiers to use. Default with all available amplifiers.
     * - StringLiteralAmplifier
     * - NumberLiteralAmplifier
     * - CharLiteralAmplifier
     * - BooleanLiteralAmplifier
     * - AllLiteralAmplifiers
     * - MethodAdd
     * - MethodRemove
     * - TestDataMutator (deprecated)
     * - MethodGeneratorAmplifier
     * - ReturnValueAmplifier
     * - ReplacementAmplifier
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
     * [optional] specify the test adequacy criterion to be maximized with amplification
     */
    @Parameter(defaultValue = "PitMutantScoreSelector", property = "test-criterion")
    private String testCriterion;

    /**
     * [optional] specify a Bugdetizer.
     */
    @Parameter(defaultValue = "NoBudgetizer", property = "budgetizer")
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
    @Parameter(property = "cases")
    private List<String> cases;

    /**
     * [optional] specify the output folder
     */
    @Parameter(defaultValue = "target/dspot/output", property = "output-path")
    private String outputPath;

    /**
     * [optional] if enabled, DSpot will remove the out directory if exists, else it will append the results to the exist files. (default: off)
     */
    @Parameter(defaultValue = "false", property = "clean")
    private Boolean clean;

    /**
     * [optional, expert mode] specify the path to the .csv of the original result of Pit Test. If you use this option the selector will be forced to PitMutantScoreSelector
     */
    @Parameter(property = "path-pit-result")
    private String pathPitResult;

    /**
     * [optional, expert] enable this option will make DSpot computing the mutation score of only one test class (the first pass through --test command line option)
     */
    @Parameter(defaultValue = "false", property = "targetOneTestClass")
    private Boolean targetOneTestClass;

    /**
     * Enable the descartes engine for Pit Mutant Score Selector.
     */
    @Parameter(defaultValue = "true", property = "descartes")
    private Boolean descartes;

    /**
     * [optional] specify the automatic builder to build the project
     */
    @Parameter(defaultValue = "MavenBuilder", property = "automatic-builder")
    private String automaticBuilder;

    /**
     * specify the path to the maven home
     */
    @Parameter(property = "maven-home")
    private String mavenHome;

    /**
     * specify a seed for the random object (used for all randomized operation)
     */
    @Parameter(defaultValue = "23", property = "randomSeed")
    private Long randomSeed;

    /**
     * specify the timeout value of the degenerated tests in millisecond
     */
    @Parameter(defaultValue = "10000", property = "timeOut")
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
    private Boolean useMavenToExeTest = false;


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
            final String absolutePath = new File(DSpotUtils.shouldAddSeparator.apply(ConstantsProperties.PROJECT_ROOT_PATH.get(properties))
                    + ConstantsProperties.MODULE.get(properties)).getAbsolutePath();
            if (!FilenameUtils.normalize(absolutePath).equals(
                    this.project.getBasedir().getAbsolutePath())) {
                return;
            }
        }
        if (this.amplifiers.size() == 1 &&
                this.amplifiers.get(0).contains(AmplificationHelper.PATH_SEPARATOR)) {
            this.amplifiers = Arrays.stream(this.amplifiers.get(0).split(AmplificationHelper.PATH_SEPARATOR))
                    .collect(Collectors.toList());
        }
        try {
            InputConfiguration.initialize(properties)
                    .setAmplifiers(AmplifierEnum.buildAmplifiersFromString(new ArrayList<>(this.amplifiers)))
                    .setNbIteration(this.iteration)
                    .setTestClasses(this.test)
                    .setBudgetizer(BudgetizerEnum.valueOf(this.budgetizer))
                    .setTestCases(this.cases)
                    .setSeed(this.randomSeed)
                    .setTimeOutInMs(this.timeOut)
                    .setBuilderName(this.automaticBuilder)
                    .setMaxTestAmplified(this.maxTestAmplified)
                    .setClean(this.clean)
                    .setMinimize(this.noMinimize)
                    .setVerbose(this.verbose)
                    .setUseWorkingDirectory(this.workingDirectory)
                    .setWithComment(this.withComment)
                    .setDescartesMode(this.descartes)
                    .setGenerateAmplifiedTestClass(this.generateNewTestClass)
                    .setKeepOriginalTestMethods(this.keepOriginalTestMethods)
                    .setUseMavenToExecuteTest(this.useMavenToExeTest)
                    .setTargetOneTestClass(this.targetOneTestClass);

            InputConfiguration.get().setOutputDirectory(
                    ConstantsProperties.OUTPUT_DIRECTORY.get(properties).isEmpty() ?
                            this.outputPath : ConstantsProperties.OUTPUT_DIRECTORY.get(properties));

            if (this.pathPitResult != null && !this.pathPitResult.isEmpty()) {
                InputConfiguration.get().setSelector(new PitMutantScoreSelector(this.pathPitResult,
                        this.pathPitResult.endsWith(".xml") ?
                                PitMutantScoreSelector.OutputFormat.XML : PitMutantScoreSelector.OutputFormat.CSV,
                        PitMutantScoreSelector.OutputFormat.XML)
                );
            } else {
                InputConfiguration.get().setSelector(SelectorEnum.valueOf(this.testCriterion).buildSelector());
            }

            if (!this.pathToSecondVersion.isEmpty()) {
                InputConfiguration.get().setAbsolutePathToSecondVersionProjectRoot(this.pathToSecondVersion);
            }

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
        // global report handling
        Main.GLOBAL_REPORT.output();
        Main.GLOBAL_REPORT.reset();
    }

    // visible for testing...
    @NotNull
    Properties initializeProperties() {
        Properties properties = new Properties();
        if (this.pathToProperties != null && !this.pathToProperties.isEmpty()) {
            try {
                properties.load(new FileInputStream(this.pathToProperties));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            properties.setProperty(ConstantsProperties.PROJECT_ROOT_PATH.getName(), project.getBasedir().getAbsolutePath());
            final Build build = project.getBuild();
            properties.setProperty(ConstantsProperties.SRC_CODE.getName(), build.getSourceDirectory());
            properties.setProperty(ConstantsProperties.TEST_SRC_CODE.getName(), build.getTestSourceDirectory());
            properties.setProperty(ConstantsProperties.SRC_CLASSES.getName(), build.getOutputDirectory());
            properties.setProperty(ConstantsProperties.TEST_CLASSES.getName(), build.getTestOutputDirectory());
            // TODO checks that we can use an empty module for multi module project
            // TODO the guess here is that the user will launch the plugin from the root of the targeted module
            // TODO and thus, we do not need to compute the relative path from its parents
            // TODO however, it may lacks some dependencies and the user should run a resolve dependency goal
            // TODO before using the dspot plugin
            // TODO we must maybe need to use a correct lifecycle
            properties.setProperty(ConstantsProperties.MODULE.getName(), "");
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
        this.cases = cases;
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
