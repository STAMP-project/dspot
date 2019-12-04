package eu.stamp_project.dspot.common.configuration;

import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.configuration.options.*;
import picocli.CommandLine;
import spoon.reflect.factory.Factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper.PATH_SEPARATOR;

/**
 * The input configuration class encapsulates all the data and associated behavior we obtain from the input properties
 * given by the user.
 * Created by marcel on 8/06/14.
 * This version of the UserInput has been largely modified, and customized to be use in DSpot.
 */
@CommandLine.Command(name = "eu.stamp_project.Main", mixinStandardHelpOptions = true)
public class UserInput {

    /*
        Project descriptions paths
     */

    @CommandLine.Option(
            names = "--absolute-path-to-project-root",
            description = "Specify the path to the root of the project. " +
                    "This path must be absolute." +
                    "We consider as root of the project folder that contain the top-most parent in a multi-module project."
    )
    private String absolutePathToProjectRoot;

    @CommandLine.Option(
            names = "--target-module",
            defaultValue = "",
            description = "Specify the module to be amplified. " +
                    "This value must be a relative path from value specified by --absolute-path-to-project-root command-line option. " +
                    "If your project is multi-module, you must use this property because DSpot works at module level."
    )
    private String targetModule = "";

    @CommandLine.Option(
            names = "--relative-path-to-source-code",
            defaultValue = "src/main/java/",
            description = "Specify the relative path from --absolute-path-to-project-root/--target-module command-line options " +
                    "that points to the folder that contains sources (.java)." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String pathToSourceCode = "src/main/java/";

    @CommandLine.Option(
            names = "--relative-path-to-test-code",
            defaultValue = "src/test/java/",
            description = "Specify the relative path from --absolute-path-to-project-root/--target-module command-line options " +
                    "that points to the folder that contains test sources (.java)." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String pathToTestSourceCode = "src/test/java/";

    @CommandLine.Option(
            names = "--relative-path-to-classes",
            defaultValue = "target/classes/",
            description = "Specify the relative path from --absolute-path-to-project-root/--target-module command-line options " +
                    "that points to the folder that contains binaries of the source (.class)." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String pathToClasses = "target/classes/";

    @CommandLine.Option(
            names = "--relative-path-to-test-classes",
            defaultValue = "target/test-classes/",
            description = "Specify the relative path from --absolute-path-to-project-root/--target-module command-line options " +
                    "that points to the folder that contains binaries of the test source (.class)." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String pathToTestClasses = "target/test-classes/";

    /*
        Amplification process configuration
     */

    @CommandLine.Option(
            names = {"-i", "--iteration"},
            defaultValue = "1",
            description = "Specify the number of amplification iterations. " +
                    "A larger number may help to improve the test criterion " +
                    "(e.g. a larger number of iterations may help to kill more mutants). " +
                    "This has an impact on the execution time: the more iterations, the longer DSpot runs." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private int nbIteration;

    @CommandLine.Option(
            names = {"-a", "--amplifiers"},
            defaultValue = "None",
            split = ",",
            description = "Specify the list of amplifiers to use. " +
                    "By default, DSpot does not use any amplifiers (None) and applies only assertion amplification. " +
                    "Valid values: ${COMPLETION-CANDIDATES}" +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private List<AmplifierEnum> amplifiers;

    @CommandLine.Option(
            names = {"-t", "--test"},
            split = ",",
            defaultValue = "all",
            description = "Fully qualified names of test classes to be amplified. " +
                    "If the value is all, DSpot will amplify the whole test suite. " +
                    "You can also use regex to describe a set of test classes. " +
                    "By default, DSpot selects all the tests classes."
    )
    private List<String> testClasses;

    @CommandLine.Option(
            names = {"-s", "--test-criterion", "--test-selector"},
            defaultValue = "PitMutantScoreSelector",
            description = "Specify the test adequacy criterion to be maximized with amplification. " +
                    "Valid values: ${COMPLETION-CANDIDATES}" +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private SelectorEnum selector;

    @CommandLine.Option(
            names = {"-c", "--cases", "--test-methods", "--test-cases"},
            split = ",",
            defaultValue = "",
            description = "Specify the test cases to amplify." +
                    "By default, DSpot selects all the tests methods."
    )
    private List<String> testCases = new ArrayList<>();

    @CommandLine.Option(
            names = {"--output-path", "--output-directory"},
            defaultValue = "target/dspot/output/",
            description = "specify a path folder for the output." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String outputDirectory = "target/dspot/output/";

    /*
        advanced amplification process configuration
     */

    @CommandLine.Option(
            names = {"--path-to-test-list-csv"},
            defaultValue = "",
            description = "Enable the selection of the test to be amplified from a csv file. " +
                    "This parameter is a path that must point to a csv file that list test classes and their test methods in the following format: " +
                    "test-class-name;test-method-1;test-method-2;test-method-3;... " +
                    "If this parameter is used, DSpot will ignore the value used in the parameter test and cases " +
                    "It is recommended to use an absolute path."
    )
    private String pathToTestListCSV = "";

    @CommandLine.Option(
            names = "--path-to-additional-classpath-elements",
            defaultValue = "",
            description = "Specify additional classpath elements (e.g. a jar files). " +
                    "Elements of this list must be separated by a comma \',\'."
    )
    private String additionalClasspathElements = "";

    @CommandLine.Option(
            names = "--automatic-builder",
            defaultValue = "Maven",
            description = "Specify the automatic builder to be used. " +
                    "Valid values: ${COMPLETION-CANDIDATES}" +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private AutomaticBuilderEnum automaticBuilder = AutomaticBuilderEnum.Maven;

    @CommandLine.Option(
            names = {"--system-properties"},
            defaultValue = "",
            description = "Specify system properties. " +
                    "This value should be a list of couple property=value, separated by a comma \',\'. " +
                    "For example, systemProperties=admin=toto,passwd=tata. " +
                    "This defines two system properties."
    )
    private String systemProperties = "";

    @CommandLine.Option(
            names = "--absolute-path-to-second-version",
            defaultValue = "",
            description = "When using the ChangeDetectorSelector, you must specify this option. " +
                    "It should have for value the path to the root of the second version of the project. " +
                    "It is recommended to give an absolute path"
    )
    private String absolutePathToSecondVersionProjectRoot;


    @CommandLine.Option(
            names = "--maven-home",
            defaultValue = "",
            description = "Specify the maven home directory. " +
                    "If it is not specified DSpot will first look in both MAVEN_HOME and M2_HOME environment variables. " +
                    "If these variables are not set, DSpot will look for a maven home at default locations " +
                    "/usr/share/maven/, /usr/local/maven-3.3.9/ and /usr/share/maven3/."
    )
    @Deprecated
    private String mavenHome;

    @CommandLine.Option(
            names = "--maven-pre-goals-test-execution",
            defaultValue = "",
            description = "Specify pre goals to run before executing test with maven." +
                    "It will be used as follow: the elements, separated by a comma," +
                    "must be valid maven goals and they will be placed just before the \"test\" goal, e.g." +
                    "--maven-pre-goals-test-execution preGoal1,preGoal2 will give \"mvn preGoal1 preGoal2 test\""
    )
    private String preGoalsTestExecution;

    @CommandLine.Option(
            names = "--delta",
            defaultValue = "0.1",
            description = "Specify the delta value for the assertions of floating-point numbers. " +
                    "If DSpot generates assertions for float, it uses Assert.assertEquals(expected, actual, delta). " +
                    "It specifies the delta value." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private double delta;

    @CommandLine.Option(
            names = "--pit-filter-classes-to-keep",
            defaultValue = "",
            description = "Specify the filter of classes to keep used by PIT. " +
                    "This allow you restrict the scope of the mutation done by PIT. " +
                    "If this is not specified, DSpot will try to build on the " +
                    "fly a filter that takes into account the largest number of classes, e.g. the topest package. "
    )
    private String filter;

    @CommandLine.Option(
            names = "--pit-version",
            defaultValue = "1.4.0",
            description = "Specify the version of PIT to use." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String pitVersion = "1.4.0";

    @CommandLine.Option(
            names = "--descartes-version",
            defaultValue = "1.2.4",
            description = "Specify the version of pit-descartes to use." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String descartesVersion = "1.2.4";

    @CommandLine.Option(
            names = "--excluded-classes",
            defaultValue = "",
            description = "Specify the full qualified name of excluded test classes. " +
                    "Each qualified name must be separated by a comma \',\'. " +
                    "These classes won't be amplified, nor executed during the mutation analysis, " +
                    "if the PitMutantScoreSelector is used." +
                    "This option can be valued by a regex."
    )
    private String excludedClasses = "";

    @CommandLine.Option(
            names = "--excluded-test-cases",
            defaultValue = "",
            description = "Specify the list of test cases to be excluded. " +
                    "Each is the name of a test case, separated by a comma \',\'."
    )
    private String excludedTestCases;

    @CommandLine.Option(
            names = "--jvm-args",
            defaultValue = "",
            description = "Specify JVM args to use when executing the test, PIT or other java process. " +
                    "This arguments should be a list, separated by a comma \',\', " +
                    "e.g. jvmArgs=Xmx2048m,-Xms1024m',-Dis.admin.user=admin,-Dis.admin.passwd=$2pRSid#"
    )
    private String JVMArgs = "";

    @CommandLine.Option(
            names = "--descartes-mutators",
            defaultValue = "",
            description = "Specify the list of descartes mutators to be used separated by comma. " +
                    "Please refer to the descartes documentation for more details: " +
                    "https://github.com/STAMP-project/pitest-descartes"
    )
    private String descartesMutators = "";


    @CommandLine.Option(
            names = "--gregor-mode",
            defaultValue = "false",
            description = "Enable the gregor engine for Pit Mutant Score Selector." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean gregorMode = false;

    @CommandLine.Option(
            names = "--cache-size",
            defaultValue = "10000",
            description = "Specify the size of the memory cache in terms of the number of store entries" +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private Long cacheSize = 10000L;

    @CommandLine.Option(
            names = "--use-working-directory",
            defaultValue = "false",
            description = "Enable this option to change working directory with the root of the project." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean useWorkingDirectory;

    @CommandLine.Option(
            names = "--generate-new-test-class",
            defaultValue = "false",
            description = "Enable the creation of a new test class." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean generateAmplifiedTestClass;

    @CommandLine.Option(
            names = "--keep-original-test-methods",
            defaultValue = "false",
            description = "If enabled, DSpot keeps original test methods of the amplified test class." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean keepOriginalTestMethods;

    @CommandLine.Option(
            names = "--use-maven-to-exe-test",
            defaultValue = "false",
            description = "If enabled, DSpot will use maven to execute the tests." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean useMavenToExecuteTest;

    @CommandLine.Option(
            names = "--execute-test-parallel",
            defaultValue = "false",
            description = "If enabled, DSpot will execute the tests in parallel. " +
                    "For JUnit5 tests it will use the number of given processors " +
                    "(specify 0 to take the number of available core processors). " +
                    "For JUnit4 tests, it will use the number of available CPU processors " +
                    "(given number of processors is ignored)." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean executeTestsInParallel;

    @CommandLine.Option(
            names = "--nb-parallel-exe-processors",
            defaultValue = "0",
            description = "Specify the number of processor to use for the parallel execution." +
                    "0 will make DSpot use all processors available." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private int numberParallelExecutionProcessors;

    @CommandLine.Option(
            names = {"--random-seed"},
            defaultValue = "23",
            description = "Specify a seed for the random object (used for all randomized operation)." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private long seed;

    @CommandLine.Option(
            names = {"--time-out"},
            defaultValue = "10000",
            description = "Specify the timeout value of the degenerated tests in millisecond." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private int timeOutInMs = 10000;

    @CommandLine.Option(
            names = {"--max-test-amplified"},
            defaultValue = "200",
            description = "Specify the maximum number of amplified tests that dspot keeps (before generating assertion)." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private Integer maxTestAmplified;

    @CommandLine.Option(
            names = {"--clean"},
            defaultValue = "false",
            description = "If enabled, DSpot will remove the out directory if exists, else it will append the results to the exist files." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean clean;

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            defaultValue = "false",
            description = "Enable verbose mode of DSpot." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean verbose;

    @CommandLine.Option(
            names = {"--with-comment"},
            defaultValue = "false",
            description = "Enable comment on amplified test: details steps of the Amplification." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean withComment;

    @CommandLine.Option(
            names = {"--allow-path-in-assertions"},
            defaultValue = "false",
            description = "If enabled, DSpot will generate assertions for values that seems like to be paths." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean allowPathInAssertion;

    @CommandLine.Option(
            names = {"--target-one-test-class"},
            defaultValue = "false",
            description = "Enable this option will make DSpot computing the mutation score of only one test class (the first pass through --test command line option)." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean targetOneTestClass;


    @CommandLine.Option(
            names = {"--full-classpath"},
            defaultValue = "",
            description = "Specify the classpath of the project. " +
                    "If this option is used, DSpot won't use an AutomaticBuilder (e.g. Maven) to clean, compile and get the classpath of the project. " +
                    "Please ensure that your project is in a good shape, i.e. clean and correctly compiled, sources and test sources."
    )
    private String dependencies = "";

    @CommandLine.Option(
            names = {"--input-ampl-distributor"},
            defaultValue = "RandomInputAmplDistributor",
            description = "Specify an input amplification distributor." +
                    "Valid values: ${COMPLETION-CANDIDATES}" +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private InputAmplDistributorEnum inputAmplDistributor;

    @CommandLine.Option(
            names = {"--example"},
            defaultValue = "false",
            description = "Run the example of DSpot and leave."
    )
    boolean example;

    @CommandLine.Option(
            names = {"--pit-output-format"},
            defaultValue = "XML",
            description = "Specify the Pit output format." +
                    "Valid values: ${COMPLETION-CANDIDATES}" +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private PitMutantScoreSelector.OutputFormat pitOutputFormat;

    @CommandLine.Option(
            names = {"--path-pit-result"},
            defaultValue = "",
            description = "Specify the path to the .xml or .csv of the original result of Pit Test. " +
                    "If you use this option the selector will be forced to PitMutantScoreSelector."
    )
    private String pathPitResult = "";

    /* DSpot-web related command line options. */

    @CommandLine.Option(
            names = {"--collector"},
            defaultValue = "NullCollector",
            description = "Set a collector: MongodbCollector to send info to Mongodb at end process, NullCollector which does nothing." +
                    "Valid values: ${COMPLETION-CANDIDATES}" +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private CollectorEnum collector;

    @CommandLine.Option(
            names = {"--mongo-url"},
            defaultValue = "mongodb://localhost:27017",
            description = "If valid url, DSpot will submit to Mongodb database." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String mongoUrl;

    @CommandLine.Option(
            names = {"--mongo-dbname"},
            defaultValue = "Dspot",
            description = "If a valid mongo-url is provided, DSpot will submit result to the database indicated by this name." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String mongoDbName;

    @CommandLine.Option(
            names = {"--mongo-colname"},
            defaultValue = "AmpRecords",
            description = "If valid mongo-url and a mongo-dbname are provided, " +
                    "DSpot will submit result to the provided collection name.." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String mongoColName;

    @CommandLine.Option(
            names = {"--repo-slug"},
            defaultValue = "UnknownSlug",
            description = "Slug of the repo for instance Stamp/Dspot. " +
                    "This is used by mongodb as a identifier for analyzed repo's submitted data." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String repoSlug;

    @CommandLine.Option(
            names = {"--repo-branch"},
            defaultValue = "UnknownBranch",
            description = "Branch name of the submitted repo, " +
                    "This is used by mongodb as a identifier for analyzed repo's submitted data." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String repoBranch;

    @CommandLine.Option(
            names = {"--restful"},
            defaultValue = "false",
            description = "If true, DSpot will enable restful mode for web Interface. " +
                    "It will look for a pending document in Mongodb with the corresponding slug and branch provided instead of creating a completely new one." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean restFul;

    @CommandLine.Option(
            names = {"--smtp-username"},
            defaultValue = "Unknown@gmail.com",
            description = "Username for Gmail, used for submit email at end-process." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String smtpUsername;

    @CommandLine.Option(
            names = {"--smtp-password"},
            defaultValue = "Unknown",
            description = "Password for Gmail, used for submit email at end-process." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String smtpPassword;

    @CommandLine.Option(
            names = {"--smtp-host"},
            defaultValue = "smtp.gmail.com",
            description = "Host server name." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String smtpHost;

    @CommandLine.Option(
            names = {"--smtp-port"},
            defaultValue = "587",
            description = "Host server port." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String smtpPort;

    @CommandLine.Option(
            names = {"--smtp-auth"},
            defaultValue = "false",
            description = "Enable this if the smtp host server require auth." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private boolean smtpAuth;

    @CommandLine.Option(
            names = {"--smtp-tls"},
            defaultValue = "false",
            description = "Enable this if the smtp host server require secure tls transport." +
                    " Default value: ${DEFAULT-VALUE}"
    )
    private String smtpTls;

    public String getAbsolutePathToTopProjectRoot() {
        return absolutePathToProjectRoot;
    }

    public String getAbsolutePathToProjectRoot() {
        return absolutePathToProjectRoot + (getTargetModule() != null && getTargetModule().isEmpty() ?
                "" : DSpotUtils.shouldAddSeparator.apply(getTargetModule()));
    }

    public UserInput setAbsolutePathToProjectRoot(String absolutePathToProjectRoot) {
        this.absolutePathToProjectRoot = DSpotUtils.shouldAddSeparator.apply(
                new File(absolutePathToProjectRoot).getAbsolutePath()
        );
        return this;
    }

    public String getTargetModule() {
        return targetModule;
    }

    public UserInput setTargetModule(String targetModule) {
        this.targetModule = DSpotUtils.shouldAddSeparator.apply(targetModule);
        return this;
    }

    public String getPathToSourceCode() {
        return pathToSourceCode;
    }

    public String getAbsolutePathToSourceCode() {
        return this.getAbsolutePathToProjectRoot() + this.getPathToSourceCode();
    }

    public String getPathToTestSourceCode() {
        return pathToTestSourceCode;
    }


    public String getAbsolutePathToTestSourceCode() {
        return this.getAbsolutePathToProjectRoot() + this.getPathToTestSourceCode();
    }

    public String getPathToClasses() {
        return pathToClasses;
    }

    public String getAbsolutePathToClasses() {
        return this.getAbsolutePathToProjectRoot() + this.getPathToClasses();
    }


    public String getPathToTestClasses() {
        return pathToTestClasses;
    }

    public String getAbsolutePathToTestClasses() {
        return this.getAbsolutePathToProjectRoot() + this.getPathToTestClasses();
    }
    /**
     * @return path to folders that contain both compiled classes and test classes as a classpath, <i>i.e.</i> separated by
     * the path separator of the system.
     */
    public String getClasspathClassesProject() {
        return this.getAbsolutePathToClasses() + AmplificationHelper.PATH_SEPARATOR + this.getAbsolutePathToTestClasses();
    }

    /**
     * This method compute the path to all dependencies of the project, separated by the path separator of the System.
     * The dependencies is compute by an implementation of a {@link eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder}
     *
     * @return the dependencies of the project
     */
    public String getDependencies() {
        return this.dependencies;
    }

    /**
     * @return the full classpath of the project. This full classpath is composed of: the returned values of {@link #getClasspathClassesProject}, {@link #getDependencies()} and {@link DSpotUtils#getAbsolutePathToDSpotDependencies()} separated by the path separator of the system, <i>i.e.</i> as a classpath.
     */
    public String getFullClassPathWithExtraDependencies() {
        return this.getClasspathClassesProject() + AmplificationHelper.PATH_SEPARATOR +
                this.getDependencies() + AmplificationHelper.PATH_SEPARATOR +
                this.getAbsolutePathToProjectRoot() + DSpotUtils.PATH_TO_DSPOT_DEPENDENCIES;
    }

    public String getAdditionalClasspathElements() {
        return additionalClasspathElements;
    }

    /**
     * This method return a processed version of the corresponding properties.
     * The value has been splitted by a comma ',',
     * then each elements has been concat to the absolute project root path,
     * and eventually each element is joined by the system path separator, e.g. ':' on Linux.
     *
     * @return the processed properties qdditionalClasspathElements
     */
    public String getProcessedAddtionalClasspathElements() {
        return Arrays.stream(additionalClasspathElements.split(","))
                .map(this.getAbsolutePathToProjectRoot()::concat)
                .collect(Collectors.joining(PATH_SEPARATOR));
    }

    public UserInput setAdditionalClasspathElements(String additionalClasspathElements) {
        this.additionalClasspathElements = additionalClasspathElements;
        return this;
    }

    public UserInput setMavenHome(String mavenHome) {
        this.mavenHome = mavenHome;
        return this;
    }

    public String getMavenHome() {
        return mavenHome;
    }

    public AutomaticBuilderEnum getBuilderEnum() {
        return this.automaticBuilder;
    }

    public UserInput setBuilderEnum(AutomaticBuilderEnum automaticBuilderEnum) {
        this.automaticBuilder = automaticBuilderEnum;
        return this;
    }

    private Factory factory;

    public Factory getFactory() {
        return factory;
    }

    public UserInput setFactory(Factory factory) {
        this.factory = factory;
        return this;
    }

    public UserInput setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    public double getDelta() {
        return delta;
    }

    public UserInput setDelta(double delta) {
        this.delta = delta;
        return this;
    }

    public String getAbsolutePathToSecondVersionProjectRoot() {
        return absolutePathToSecondVersionProjectRoot;
    }

    public UserInput setAbsolutePathToSecondVersionProjectRoot(String absolutePathToSecondVersionProjectRoot) {
        this.absolutePathToSecondVersionProjectRoot =
                DSpotUtils.shouldAddSeparator.apply(absolutePathToSecondVersionProjectRoot);
        return this;
    }

    public String getExcludedClasses() {
        return excludedClasses;
    }

    public UserInput setExcludedClasses(String excludedClasses) {
        this.excludedClasses = excludedClasses;
        return this;
    }

    public String getExcludedTestCases() {
        return excludedTestCases;
    }

    public UserInput setExcludedTestCases(String excludedTestCases) {
        this.excludedTestCases = excludedTestCases;
        return this;
    }

    public String getFilter() {
        return filter;
    }

    public UserInput setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public String getPitVersion() {
        return pitVersion;
    }

    public String getDescartesVersion() {
        return descartesVersion;
    }

    public String getJVMArgs() {
        return JVMArgs;
    }

    public UserInput setJVMArgs(String JVMArgs) {
        this.JVMArgs = JVMArgs;
        EntryPoint.JVMArgs = String.join(" ", JVMArgs.split(","));
        return this;
    }

    public void setSystemProperties(String systemProperties) {
        this.systemProperties = systemProperties;
    }

    public String getSystemProperties() {
        return this.systemProperties;
    }

    public String getDescartesMutators() {
        return descartesMutators;
    }

    public boolean isGregorMode() {
        return gregorMode;
    }

    public void setGregorMode(boolean gregorMode) {
        this.gregorMode = gregorMode;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public UserInput setVerbose(boolean verbose) {
        this.verbose = verbose;
        EntryPoint.verbose = this.isVerbose();
        return this;
    }

    public List<AmplifierEnum> getAmplifiers() {
        return this.amplifiers;
    }

    public UserInput setAmplifiers(List<AmplifierEnum> amplifiers) {
        this.amplifiers = amplifiers;
        return this;
    }

    public int getNbIteration() {
        return nbIteration;
    }

    public UserInput setNbIteration(int nbIteration) {
        this.nbIteration = nbIteration;
        return this;
    }

    public List<String> getTestClasses() {
        return testClasses;
    }

    public UserInput setTestClasses(List<String> testClasses) {
        this.testClasses = testClasses;
        return this;
    }

    public UserInput setSelector(SelectorEnum selector) {
        this.selector = selector;
        return this;
    }

    public String getPathPitResult() {
        return this.pathPitResult;
    }

    public PitMutantScoreSelector.OutputFormat getPitOutputFormat() {
        return this.pitOutputFormat;
    }

    public SelectorEnum getSelector() {
        return this.selector;
    }

    public List<String> getTestCases() {
        return testCases;
    }

    public UserInput setTestCases(List<String> testCases) {
        this.testCases = testCases;
        return this;
    }

    public long getSeed() {
        return seed;
    }

    public int getTimeOutInMs() {
        return timeOutInMs;
    }

    public Integer getMaxTestAmplified() {
        return maxTestAmplified;
    }

    public boolean shouldClean() {
        return clean;
    }

    public UserInput setClean(boolean clean) {
        this.clean = clean;
        return this;
    }

    public boolean withComment() {
        return withComment;
    }

    public InputAmplDistributorEnum getInputAmplDistributor() {
        return this.inputAmplDistributor;
    }

    public UserInput setInputAmplDistributor(InputAmplDistributorEnum inputAmplDistributor) {
        this.inputAmplDistributor = inputAmplDistributor;
        return this;
    }

    public boolean shouldGenerateAmplifiedTestClass() {
        return generateAmplifiedTestClass;
    }

    public boolean shouldUseMavenToExecuteTest() {
        return useMavenToExecuteTest;
    }

    public String getPreGoalsTestExecution() {
        return this.preGoalsTestExecution;
    }

    public boolean shouldKeepOriginalTestMethods() {
        return this.keepOriginalTestMethods;
    }

    public boolean shouldTargetOneTestClass() {
        return this.targetOneTestClass;
    }

    public UserInput setTargetOneTestClass(boolean targetOneTestClass) {
        this.targetOneTestClass = targetOneTestClass;
        return this;
    }

    public boolean shouldAllowPathInAssertion() {
        return this.allowPathInAssertion;
    }

    public boolean shouldExecuteTestsInParallel() {
        return executeTestsInParallel;
    }

    public int getNumberParallelExecutionProcessors() {
        if (this.numberParallelExecutionProcessors == 0) {
            this.numberParallelExecutionProcessors = Runtime.getRuntime().availableProcessors();
        }
        return numberParallelExecutionProcessors;
    }

    public Long getCacheSize() {
        return this.cacheSize;
    }

    public CollectorEnum getCollector() {
        return this.collector;
    }

    public boolean shouldRunExample() {
        return this.example;
    }

    public String getMongoUrl() {
        return mongoUrl;
    }

    public String getMongoDbName() {
        return mongoDbName;
    }

    public String getMongoColName() {
        return mongoColName;
    }

    public String getRepoSlug() {
        return repoSlug;
    }

    public String getRepoBranch() {
        return repoBranch;
    }

    public boolean isRestFul() {
        return restFul;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public String getSmtpTls() {
        return smtpTls;
    }

    public String getPathToTestListCSV() {
        return pathToTestListCSV;
    }

    public void configureExample() {
        try {
            this.setAbsolutePathToProjectRoot("src/test/resources/test-projects/");
            this.setNbIteration(1);
            this.setAmplifiers(Collections.singletonList(AmplifierEnum.FastLiteralAmplifier));
            this.setSelector(SelectorEnum.JacocoCoverageSelector);
            this.setInputAmplDistributor(InputAmplDistributorEnum.RandomInputAmplDistributor);
            this.setTestClasses(Collections.singletonList("example.TestSuiteExample"));
            this.setTestCases(Collections.emptyList());
            this.setVerbose(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setDependencies(String dependencies) {
        this.dependencies =dependencies;
    }

    public void initTestsToBeAmplified() {
        // clear both list of test classes and test cases
        this.getTestClasses().clear();
        this.getTestCases().clear();
        // add all test classes and test cases from the csv file
        try (BufferedReader buffer = new BufferedReader(new FileReader(this.pathToTestListCSV))) {
            buffer.lines().forEach(line -> {
                        final String[] splittedLine = line.split(";");
                        this.getTestClasses().add(splittedLine[0]);
                        for (int i = 1; i < splittedLine.length; i++) {
                            this.getTestCases().add(splittedLine[i]);
                        }
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

