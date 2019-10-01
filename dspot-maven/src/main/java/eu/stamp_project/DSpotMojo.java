package eu.stamp_project;

import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.options.*;
import eu.stamp_project.utils.program.ConstantsProperties;
import eu.stamp_project.utils.program.InputConfiguration;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mojo(name = "amplify-unit-tests", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.TEST)
public class DSpotMojo extends AbstractMojo {

    // >>>>>> BEGIN GENERATED >>>>>>
    // The following codes, i.e. maven parameters, are generated using the main function in eu.stamp_project.utils.options.JSAPOptions

    /**
     *	[mandatory] specify the path to the configuration file (format Java properties) of the target project (e.g. ./foo.properties).
     */
    @Parameter(property = "path-to-properties")
    private String pathToProperties;

    /**
     *	[optional] specify the list of amplifiers to use. By default, DSpot does not use any amplifiers (None) and applies only assertion amplification.
     *	Possible values are:
     *			 - MethodAdd
     *			 - MethodDuplicationAmplifier
     *			 - MethodRemove
     *			 - FastLiteralAmplifier
     *			 - TestDataMutator
     *			 - MethodGeneratorAmplifier
     *			 - MethodAdderOnExistingObjectsAmplifier
     *			 - ReturnValueAmplifier
     *			 - StringLiteralAmplifier
     *			 - NumberLiteralAmplifier
     *			 - BooleanLiteralAmplifier
     *			 - CharLiteralAmplifier
     *			 - AllLiteralAmplifiers
     *			 - NullifierAmplifier
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
     *	[optional] specify the test adequacy criterion to be maximized with amplification.
     *	Possible values are:
     *			 - PitMutantScoreSelector
     *			 - JacocoCoverageSelector
     *			 - TakeAllSelector
     *			 - ChangeDetectorSelector
     */
    @Parameter(defaultValue = "PitMutantScoreSelector", property = "test-criterion")
    private String testCriterion;

    /**
     *	[optional] specify the Pit output format.
     *	Possible values are:
     *			 - XML
     *			 - CSV
     */
    @Parameter(defaultValue = "XML", property = "pit-output-format")
    private String pitOutputFormat;

    /**
     *	[optional] specify a Input Amplification Distributor.
     *	Possible values are:
     *			 - RandomInputAmplDistributor
     *			 - TextualDistanceInputAmplDistributor
     *			 - SimpleInputAmplDistributor
     */
    @Parameter(defaultValue = "RandomInputAmplDistributor", property = "input-ampl-distributor")
    private String inputAmplDistributor;

    /**
     *	[optional] specify the maximum number of amplified tests that dspot keeps (before generating assertion)
     */
    @Parameter(defaultValue = "200", property = "max-test-amplified")
    private Integer maxTestAmplified;

    /**
     *	[optional] fully qualified names of test classes to be amplified. If the value is all, DSpot will amplify the whole test suite. You can also use regex to describe a set of test classes. By default, DSpot selects all the tests (value all).
     */
    @Parameter(defaultValue = "all", property = "test")
    private List<String> test;

    /**
     *	specify the test cases to amplify
     */
    @Parameter(property = "test-cases")
    private List<String> testCases;

    /**
     *	[optional] specify the output folder
     */
    @Parameter(defaultValue = "target/dspot/output", property = "output-path")
    private String outputPath;

    /**
     *	[optional] if enabled, DSpot will remove the out directory if exists, else it will append the results to the exist files.
     */
    @Parameter(defaultValue = "false", property = "clean")
    private Boolean clean;

    /**
     *	[optional, expert mode] specify the path to the .xml or .csv of the original result of Pit Test. If you use this option the selector will be forced to PitMutantScoreSelector
     */
    @Parameter(property = "path-pit-result")
    private String pathPitResult;

    /**
     *	[optional, expert] enable this option will make DSpot computing the mutation score of only one test class (the first pass through --test command line option)
     */
    @Parameter(defaultValue = "false", property = "target-one-test-class")
    private Boolean targetOneTestClass;

    /**
     *	Enable the descartes engine for Pit Mutant Score Selector.
     */
    @Parameter(defaultValue = "true", property = "descartes")
    private Boolean descartes;

    /**
     *	Enable the gregor engine for Pit Mutant Score Selector.
     */
    @Parameter(defaultValue = "false", property = "gregor")
    private Boolean gregor;

    /**
     *	[optional] specify the automatic builder to build the project
     */
    @Parameter(defaultValue = "", property = "automatic-builder")
    private String automaticBuilder;

    /**
     *	specify the path to the maven home
     */
    @Parameter(property = "maven-home")
    private String mavenHome;

    /**
     *	specify a seed for the random object (used for all randomized operation)
     */
    @Parameter(defaultValue = "23", property = "random-seed")
    private Long randomSeed;

    /**
     *	specify the timeout value of the degenerated tests in millisecond
     */
    @Parameter(defaultValue = "10000", property = "time-out")
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
    private Boolean withComment;

    /**
     *	Disable the minimization of amplified tests.
     */
    @Parameter(defaultValue = "false", property = "no-minimize")
    private Boolean noMinimize;

    /**
     *	Enable this option to change working directory with the root of the project.
     */
    @Parameter(defaultValue = "false", property = "working-directory")
    private Boolean workingDirectory;

    /**
     *	Enable the creation of a new test class.
     */
    @Parameter(defaultValue = "false", property = "generate-new-test-class")
    private Boolean generateNewTestClass;

    /**
     *	If enabled, DSpot keeps original test methods of the amplified test class.
     */
    @Parameter(defaultValue = "false", property = "keep-original-test-methods")
    private Boolean keepOriginalTestMethods;

    /**
     *	If enabled, DSpot will use maven to execute the tests.
     */
    @Parameter(defaultValue = "false", property = "use-maven-to-exe-test")
    private Boolean useMavenToExeTest;

    /**
     *	If enabled, DSpot will generate assertions for values that seems like to be paths.
     */
    @Parameter(defaultValue = "false", property = "allow-path-in-assertions")
    private Boolean allowPathInAssertions;

    /**
     *	[optional] If enabled, DSpot will execute the tests in parallel. For JUnit5 tests it will use the number of given processors (specify 0 to take the number of available core processors). For JUnit4 tests, it will use the number of available CPU processors (given number of processors is ignored).
     */
    @Parameter(defaultValue = "0", property = "execute-test-parallel-with-number-processors")
    private Integer executeTestParallelWithNumberProcessors;

    /**
     *	[optional] specify the classpath of the project. If this option is used, DSpot won't use an AutomaticBuilder (e.g. Maven) to clean, compile and get the classpath of the project. Please ensure that your project is in a good shape, i.e. clean and correctly compiled, sources and test sources.
     */
    @Parameter(property = "full-classpath")
    private String fullClasspath;

    /**
     *	[optional] set a collector: MongodbCollector to send info to Mongodb at end process, NullCollector which does nothing.
     */
    @Parameter(defaultValue = "NullCollector", property = "collector")
    private String collector;

    /**
     *	[optional] If valid url, DSpot will submit to Mongodb database. For default use mongodb://localhost:27017
     */
    @Parameter(defaultValue = "mongodb://localhost:27017", property = "mongo-url")
    private String mongoUrl;

    /**
     *	[optional] If valid mongo-url provided, DSpot will submit to the provided database name.
     */
    @Parameter(defaultValue = "Dspot", property = "mongo-dbname")
    private String mongoDbname;

    /**
     *	[optional] If valid mongo-url and mongo-dbname provided, DSpot will submit to the provided collection name.
     */
    @Parameter(defaultValue = "AmpRecords", property = "mongo-colname")
    private String mongoColname;

    /**
     *	[optional] slug of the repo for instance Stamp/Dspot,this is used by mongodb as a identifier for analyzed repo's submitted data
     */
    @Parameter(defaultValue = "UnknownSlug", property = "repo-slug")
    private String repoSlug;

    /**
     *	[optional] branch name of the submitted repo,this is used by mongodb as a identifier for analyzed repo's submitted data
     */
    @Parameter(defaultValue = "UnknownBranch", property = "repo-branch")
    private String repoBranch;

    /**
     *	If 1 or true will enable restful mode for web Interface. It will look for a pending document in Mongodb with the corresponding slug and branch provided instead of creating a completely new one.
     */
    @Parameter(defaultValue = "false", property = "restful")
    private Boolean restful;

    /**
     *	Username for Gmail, used for submit email at end-process
     */
    @Parameter(defaultValue = "Unknown@gmail.com", property = "smtp-username")
    private String smtpUsername;

    /**
     *	password for Gmail, used for submit email at end-process
     */
    @Parameter(defaultValue = "Unknown", property = "smtp-password")
    private String smtpPassword;

    /**
     *	host server name , default: smtp.gmail.com
     */
    @Parameter(defaultValue = "smtp.gmail.com", property = "smtp-host")
    private String smtpHost;

    /**
     *	host server port , default : 587
     */
    @Parameter(defaultValue = "587", property = "smtp-port")
    private String smtpPort;

    /**
     *	true , if the smtp host server require auth, which is usually the case
     */
    @Parameter(defaultValue = "false", property = "smtp-auth")
    private Boolean smtpAuth;

    /**
     *	true , if need secure tls transport.
     */
    @Parameter(defaultValue = "false", property = "smtp-tls")
    private Boolean smtpTls;

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

    // <<<<<< END GENERATED <<<<<<

    private final String builder = "MAVEN";

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
    
    /**
     * [optional] If enabled, DSpot will execute the tests in parallel. 
     * For JUnit5 tests it will use the number of given processors (specify 0 to take the number of available core processors). 
     * For JUnit4 tests, it will use the number of available CPU processors (given number of processors is ignored)
     */
    @Parameter(defaultValue = "-1", property = "execute-test-parallel-with-number-processors")
    private Integer numberParallelExecutionProcessors;

    /**
     * Enable the parallel execution of test during amplification process
     */
    @Parameter(defaultValue = "false", property = "path-to-second-version")
    private boolean executeTestsInParallel = false;

    /**
     * Enable to execute DSpot on all modules. This parameter do not take into account the value of targetModule.
     */
    @Parameter(defaultValue = "false", property = "run-on-all-modules")
    private boolean runOnAllModules = false;

    @Parameter(defaultValue = "", property = "target-module")
    private String targetModule = "";

    // must be initialized at the first call of execute()
    private static Properties properties;

    void reset() {
        properties = null;
    }

    @Override
    public void execute() {
        if (this.help) {
            JSAPOptions.showUsage();
        }

        Properties properties = initializeProperties();

        final String currentPath = DSpotUtils.shouldAddSeparator.apply(
                this.project.getBasedir().getAbsolutePath());
        if (this.runOnAllModules &&
                !properties.getProperty(ConstantsProperties.MODULE.getName()).isEmpty()) {
            properties.put(ConstantsProperties.MODULE.getName(),
                    DSpotUtils.shouldAddSeparator.apply(
                        currentPath
                            .substring(ConstantsProperties.PROJECT_ROOT_PATH.get(properties).length())
                    )
            );
        } else if ((properties.getProperty(ConstantsProperties.MODULE.getName()) != null ||
                !properties.getProperty(ConstantsProperties.MODULE.getName()).isEmpty())) {
            final String target = DSpotUtils.shouldAddSeparator.apply(ConstantsProperties.PROJECT_ROOT_PATH.get(properties))
                    + ConstantsProperties.MODULE.get(properties);
            if (!currentPath.equals(target)) {
                return;
            }
        }

        if (this.amplifiers.size() == 1 &&
                this.amplifiers.get(0).contains(AmplificationHelper.PATH_SEPARATOR)) {
            this.amplifiers = Arrays.stream(this.amplifiers.get(0).split(AmplificationHelper.PATH_SEPARATOR))
                    .collect(Collectors.toList());
        }

        boolean executeTestsInParallel = executeTestParallelWithNumberProcessors > 0;
        if (executeTestParallelWithNumberProcessors == 0) {
            this.executeTestParallelWithNumberProcessors = Runtime.getRuntime().availableProcessors();
        }

        try {
            Configuration.configure(
                    properties,
                    amplifiers,
                    testCriterion,
                    inputAmplDistributor,
                    pitOutputFormat,
                    pathPitResult,
                    builder,
                    outputPath,
                    iteration,
                    randomSeed,
                    timeOut,
                    maxTestAmplified,
                    clean,
                    verbose,
                    workingDirectory,
                    withComment,
                    generateNewTestClass,
                    keepOriginalTestMethods,
                    gregor,
                    descartes,
                    useMavenToExeTest,
                    targetOneTestClass,
                    allowPathInAssertions,
                    executeTestsInParallel,
                    executeTestParallelWithNumberProcessors,
                    test,
                    testCases,
                    null
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
            e.printStackTrace();
        }
    }

    // visible for testing...
    @NotNull
    Properties initializeProperties() {
        if (properties != null) {
            return properties;
        }
        properties = new Properties();
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
        properties.setProperty(ConstantsProperties.MODULE.getName(),
                DSpotUtils.shouldAddSeparator.apply(this.targetModule)
        );
        if (this.pathToProperties != null && !this.pathToProperties.isEmpty()) {
            try {
                properties.load(new FileInputStream(this.pathToProperties));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        properties.put(ConstantsProperties.PROJECT_ROOT_PATH.getName(),
                formatPath.apply(ConstantsProperties.PROJECT_ROOT_PATH.get(properties))
        );

        return properties;
    }

    private static final Function<String, String> formatPath = path ->
        DSpotUtils.shouldAddSeparator.apply(
                Paths.get(new File(path).getAbsolutePath())
                        .normalize()
                        .toString()
        );


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
