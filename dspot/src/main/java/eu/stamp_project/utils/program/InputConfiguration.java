package eu.stamp_project.utils.program;

import eu.stamp_project.Main;
import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.utils.DSpotCache;
import eu.stamp_project.utils.options.AmplifierEnum;
import eu.stamp_project.utils.options.InputAmplDistributorEnum;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.stamp_project.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * The input configuration class encapsulates all the data and associated behavior we obtain from the input properties
 * given by the user.
 * Created by marcel on 8/06/14.
 * This version of the InputConfiguration has been largely modified, and customized to be use in DSpot.
 */
public class InputConfiguration {

    private static InputConfiguration instance;

    private static final Logger LOGGER = LoggerFactory.getLogger(InputConfiguration.class);

    public static InputConfiguration get() {
        return InputConfiguration.instance;
    }

    private static void reset() {
        LOGGER.warn("Erasing old instance of InputConfiguration");
        DSpotCache.reset();
        Main.GLOBAL_REPORT.reset();
        AmplificationHelper.reset();
    }

    private InputConfiguration() {

    }

    public void initialize() {
        if (!this.systemProperties.isEmpty()) {
            Arrays.stream(this.systemProperties.split(","))
                    .forEach(systemProperty -> {
                        String[] keyValueInArray = systemProperty.split("=");
                        System.getProperties().put(keyValueInArray[0], keyValueInArray[1]);
                    });
        }

        this.automaticBuilder = this.automaticBuilderEnum.toAutomaticBuilder();
        this.amplifiers = this.amplifiersEnum.stream().map(amplifierEnum -> amplifierEnum.amplifier).collect(Collectors.toList());
        this.budgetizer = this.budgetizerEnum.getBudgetizer(this.amplifiers);
        this.selector = this.selectorEnum.buildSelector();
        // todo support pre-computed pit result

        if (this.dependencies.isEmpty()) {
            this.dependencies = this.automaticBuilder.compileAndBuildClasspath();
        }
        // TODO checks this. Since we support different Test Support, we may not need to add artificially junit in the classpath
        if (!this.dependencies.contains("junit" + File.separator + "junit" + File.separator + "4")) {
            this.dependencies = Test.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getFile() +
                    AmplificationHelper.PATH_SEPARATOR + this.dependencies;
        }
        if (!this.additionalClasspathElements.isEmpty()) {
            String pathToAdditionalClasspathElements = this.additionalClasspathElements;
            if (!Paths.get(this.additionalClasspathElements).isAbsolute()) {
                pathToAdditionalClasspathElements =
                        DSpotUtils.shouldAddSeparator.apply(this.absolutePathToProjectRoot + this.additionalClasspathElements);
            }
            this.dependencies += PATH_SEPARATOR + pathToAdditionalClasspathElements;
        }
        if (this.numberParallelExecutionProcessors == 0) {
            this.numberParallelExecutionProcessors = Runtime.getRuntime().availableProcessors();
        }
    }

    @CommandLine.Option(
            names = "--absolute-path-to-project-root",
            required = true,
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
    private String targetModule;

    @CommandLine.Option(
            names = "--relative-path-to-source-code",
            defaultValue = "src/main/java/",
            description = "Specify the relative path from --absolute-path-to-project-root/--target-module command-line options " +
                    "that points to the folder that contains sources (.java)."
    )
    private String pathToSourceCode;

    @CommandLine.Option(
            names = "--relative-path-to-test-code",
            defaultValue = "src/test/java/",
            description = "Specify the relative path from --absolute-path-to-project-root/--target-module command-line options " +
                    "that points to the folder that contains test sources (.java)."
    )
    private String pathToTestSourceCode;

    @CommandLine.Option(
            names = "--relative-path-to-classes",
            defaultValue = "target/classes/",
            description = "Specify the relative path from --absolute-path-to-project-root/--target-module command-line options " +
                    "that points to the folder that contains binaries of the source (.class)."
    )
    private String pathToClasses;

    @CommandLine.Option(
            names = "--relative-path-to-test-classes",
            defaultValue = "target/test-classes/",
            description = "Specify the relative path from --absolute-path-to-project-root/--target-module command-line options " +
                    "that points to the folder that contains binaries of the test source (.class)."
    )
    private String pathToTestClasses;

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
                    "Valid values: ${COMPLETION-CANDIDATES}"
    )
    private AutomaticBuilderEnum automaticBuilderEnum;

    private AutomaticBuilder automaticBuilder;

    @CommandLine.Option(
            names = {"--system-properties"},
            defaultValue = "",
            description = "Specify system properties. " +
                    "This value should be a list of couple property=value, separated by a comma \',\'. " +
                    "For example, systemProperties=admin=toto,passwd=tata. " +
                    "This defines two system properties."
    )
    private String systemProperties;

    @CommandLine.Option(
            names = "--absolute-path-to-second-version",
            defaultValue = "",
            description = "When using the ChangeDetectorSelector, you must specify this option. " +
                    "It should have for value the path to the root of the second version of the project. " +
                    "It is recommended to give an absolute path"
    )
    private String absolutePathToSecondVersionProjectRoot;

    @CommandLine.Option(
            names = {"--output-path", "--output-directory"},
            defaultValue = "target/dspot/output/",
            description = "specify a path folder for the output."
    )
    private String outputDirectory;

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
                    "It specifies the delta value."
    )
    private String delta;

    @CommandLine.Option(
            names = "--pit-filter-classes-to-keep",
            description = "Specify the filter of classes to keep used by PIT. " +
                    "This allow you restrict the scope of the mutation done by PIT. " +
                    "If this is not specified, DSpot will try to build on the " +
                    "fly a filter that takes into account the largest number of classes, e.g. the topest package. "
    )
    private String filter;

    @CommandLine.Option(
            names = "--pit-version",
            defaultValue =  "1.4.0",
            description = "Specify the version of PIT to use."
    )
    private String pitVersion;

    @CommandLine.Option(
            names = "--descartes-version",
            defaultValue =  "1.2.4",
            description = "Specify the version of pit-descartes to use."
    )
    private String descartesVersion;

    @CommandLine.Option(
            names = "--excluded-classes",
            defaultValue =  "",
            description = "Specify the full qualified name of excluded test classes. " +
                    "Each qualified name must be separated by a comma \',\'. " +
                    "These classes won't be amplified, nor executed during the mutation analysis, " +
                    "if the PitMutantScoreSelector is used." +
                    "This option can be valued by a regex."
    )
    private String excludedClasses = "";

    @CommandLine.Option(
            names = "--excluded-test-cases",
            defaultValue =  "",
            description = "Specify the list of test cases to be excluded. " +
                    "Each is the name of a test case, separated by a comma \',\'."
    )
    private String excludedTestCases;

    @CommandLine.Option(
            names = "--jvm-args",
            defaultValue =  "",
            description = "Specify JVM args to use when executing the test, PIT or other java process. " +
                    "This arguments should be a list, separated by a comma \',\', "+
                    "e.g. jvmArgs=Xmx2048m,-Xms1024m',-Dis.admin.user=admin,-Dis.admin.passwd=$2pRSid#"
    )
    private String JVMArgs = "";

    @CommandLine.Option(
            names = "--descartes-mutators",
            defaultValue =  "",
            description = "Specify the list of descartes mutators to be used separated by comma. " +
                    "Please refer to the descartes documentation for more details: " +
                    "https://github.com/STAMP-project/pitest-descartes"
    )
    private String descartesMutators = "";

    @CommandLine.Option(
            names = "--cache-size",
            defaultValue =  "10000",
            description = "Specify the size of the memory cache in terms of the number of store entries"
    )
    private Long cacheSize;

    @CommandLine.Option(
            names = "--descartes-mode",
            defaultValue =  "true",
            description = "Enable the descartes engine for Pit Mutant Score Selector."
    )
    @Deprecated
    private boolean descartesMode;

    @CommandLine.Option(
            names = "--gregor-mode",
            defaultValue =  "false",
            description = "Enable the gregor engine for Pit Mutant Score Selector."
    )
    @Deprecated
    private boolean gregorMode;

    @CommandLine.Option(
            names = "--use-working-directory",
            defaultValue =  "false",
            description = "Enable this option to change working directory with the root of the project."
    )
    private boolean useWorkingDirectory;

    @CommandLine.Option(
            names = "--generate-new-test-class",
            defaultValue =  "false",
            description = "Enable the creation of a new test class."
    )
    private boolean generateAmplifiedTestClass;

    @CommandLine.Option(
            names = "--keep-original-test-methods",
            defaultValue =  "false",
            description = "If enabled, DSpot keeps original test methods of the amplified test class."
    )
    private boolean keepOriginalTestMethods;

    @CommandLine.Option(
            names = "--use-maven-to-exe-test",
            defaultValue =  "false",
            description = "If enabled, DSpot will use maven to execute the tests."
    )
    private boolean useMavenToExecuteTest;

    @CommandLine.Option(
            names = "--execute-test-parallel",
            defaultValue =  "false",
            description = "If enabled, DSpot will execute the tests in parallel. " +
                    "For JUnit5 tests it will use the number of given processors " +
                    "(specify 0 to take the number of available core processors). " +
                    "For JUnit4 tests, it will use the number of available CPU processors " +
                    "(given number of processors is ignored)."
    )
    private boolean executeTestsInParallel;

    @CommandLine.Option(
            names = "--nb-parallel-exe-processors",
            defaultValue =  "0",
            description = "Specify the number of processor to use for the parallel execution." +
                    "0 will make DSpot use all processors available."
    )
    private int numberParallelExecutionProcessors;

    @CommandLine.Option(
            names = {"-i", "--iteration"},
            defaultValue =  "1",
            description = "Specify the number of amplification iterations. " +
                    "A larger number may help to improve the test criterion " +
                    "(e.g. a larger number of iterations may help to kill more mutants). " +
                    "This has an impact on the execution time: the more iterations, the longer DSpot runs."
    )
    private int nbIteration;

    @CommandLine.Option(
            names = {"-a", "--amplifiers"},
            defaultValue =  "None",
            description = "Specify the list of amplifiers to use. " +
                    "By default, DSpot does not use any amplifiers (None) and applies only assertion amplification. " +
                    "Valid values: ${COMPLETION-CANDIDATES}"
    )
    private List<AmplifierEnum> amplifiersEnum;

    private List<Amplifier> amplifiers;

    @CommandLine.Option(
            names = {"-t", "--test"},
            defaultValue =  "all",
            description = "Fully qualified names of test classes to be amplified. " +
                    "If the value is all, DSpot will amplify the whole test suite. " +
                    "You can also use regex to describe a set of test classes. " +
                    "By default, DSpot selects all the tests."
    )
    private List<String> testClasses;

    @CommandLine.Option(
            names = {"-s", "--test-criterion", "--test-selector"},
            defaultValue =  "PitMutantScoreSelector",
            description = "Specify the test adequacy criterion to be maximized with amplification. " +
                    "Valid values: ${COMPLETION-CANDIDATES}"
    )
    private SelectorEnum selectorEnum;

    private TestSelector selector;

    @CommandLine.Option(
            names = {"-c", "--cases", "--test-methods", "--test-cases"},
            description = "Specify the test cases to amplify."
    )
    private List<String> testCases;

    @CommandLine.Option(
            names = {"--random-seed"},
            defaultValue =  "23",
            description = "Specify a seed for the random object (used for all randomized operation)."
    )
    private long seed;

    @CommandLine.Option(
            names = {"--time-out"},
            defaultValue =  "10000",
            description = "Specify the timeout value of the degenerated tests in millisecond."
    )
    private int timeOutInMs;

    @CommandLine.Option(
            names = {"--max-test-amplified"},
            defaultValue =  "200",
            description = "Specify the maximum number of amplified tests that dspot keeps (before generating assertion)."
    )
    private Integer maxTestAmplified;

    @CommandLine.Option(
            names = {"--clean"},
            defaultValue =  "false",
            description = "If enabled, DSpot will remove the out directory if exists, else it will append the results to the exist files."
    )
    private boolean clean;

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            defaultValue =  "false",
            description = "Enable verbose mode of DSpot."
    )
    private boolean verbose;

    @CommandLine.Option(
            names = {"--with-comment"},
            defaultValue =  "false",
            description = "Enable comment on amplified test: details steps of the Amplification."
    )
    private boolean withComment;

    @CommandLine.Option(
            names = {"--allow-path-in-assertions"},
            defaultValue =  "false",
            description = "If enabled, DSpot will generate assertions for values that seems like to be paths."
    )
    private boolean allowPathInAssertion;

    @CommandLine.Option(
            names = {"--target-one-test-class"},
            defaultValue =  "false",
            description = "Enable this option will make DSpot computing the mutation score of only one test class (the first pass through --test command line option)."
    )
    private boolean targetOneTestClass;


    @CommandLine.Option(
            names = {"--full-classpath"},
            defaultValue =  "",
            description = "Specify the classpath of the project. " +
                    "If this option is used, DSpot won't use an AutomaticBuilder (e.g. Maven) to clean, compile and get the classpath of the project. " +
                    "Please ensure that your project is in a good shape, i.e. clean and correctly compiled, sources and test sources."
    )
    private String dependencies;

    @CommandLine.Option(
            names = {"--budgetizer"},
            defaultValue =  "RandomBudgetizer",
            description = "Specify a Bugdetizer." +
                    "Valid values: ${COMPLETION-CANDIDATES}"
    )
    private BudgetizerEnum budgetizerEnum;

    private Budgetizer budgetizer;

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
                    "Valid values: ${COMPLETION-CANDIDATES}"
    )
    private PitMutantScoreSelector.OutputFormat pitOutputFormat;

    @CommandLine.Option(
            names = {"--path-pit-result"},
            description = "Specify the path to the .xml or .csv of the original result of Pit Test. " +
                    "If you use this option the selector will be forced to PitMutantScoreSelector."
    )
    private String pathPitResult;

    @CommandLine.Option(
            names = {"--version"},
            versionHelp = true,
            description = "Display version info."
    )
    boolean versionInfoRequested;

    @CommandLine.Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Display this help message."
    )
    boolean usageHelpRequested;

    private boolean isJUnit5;

    public String getAbsolutePathToProjectRoot() {
        return absolutePathToProjectRoot;
    }

    public InputConfiguration setAbsolutePathToProjectRoot(String absolutePathToProjectRoot) {
        this.absolutePathToProjectRoot = DSpotUtils.shouldAddSeparator.apply(absolutePathToProjectRoot);
        return this;
    }

    public String getTargetModule() {
        return targetModule;
    }

    public InputConfiguration setTargetModule(String targetModule) {
        this.targetModule = DSpotUtils.shouldAddSeparator.apply(targetModule);
        return this;
    }

    public String getPathToSourceCode() {
        return pathToSourceCode;
    }

    public String getAbsolutePathToSourceCode() {
        return this.absolutePathToProjectRoot + this.getPathToSourceCode();
    }

    public InputConfiguration setPathToSourceCode(String pathToSourceCode) {
        this.pathToSourceCode = DSpotUtils.removeProjectRootIfAbsoluteAndAddSeparator(this.absolutePathToProjectRoot, pathToSourceCode);
        return this;
    }

    public String getPathToTestSourceCode() {
        return pathToTestSourceCode;
    }

    public InputConfiguration setPathToTestSourceCode(String pathToTestSourceCode) {
        this.pathToTestSourceCode = DSpotUtils.removeProjectRootIfAbsoluteAndAddSeparator(this.absolutePathToProjectRoot, pathToTestSourceCode);
        return this;
    }

    public String getAbsolutePathToTestSourceCode() {
        return this.absolutePathToProjectRoot + this.getPathToTestSourceCode();
    }

    public String getPathToClasses() {
        return pathToClasses;
    }

    public InputConfiguration setPathToClasses(String pathToClasses) {
        this.pathToClasses = DSpotUtils.removeProjectRootIfAbsoluteAndAddSeparator(this.absolutePathToProjectRoot, pathToClasses);
        return this;
    }

    public String getAbsolutePathToClasses() {
        return this.absolutePathToProjectRoot + this.getPathToClasses();
    }


    public String getPathToTestClasses() {
        return pathToTestClasses;
    }

    public String getAbsolutePathToTestClasses() {
        return this.absolutePathToProjectRoot + this.getPathToTestClasses();
    }

    public InputConfiguration setPathToTestClasses(String pathToTestClasses) {
        this.pathToTestClasses = DSpotUtils.removeProjectRootIfAbsoluteAndAddSeparator(this.absolutePathToProjectRoot, pathToTestClasses);
        return this;
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
     * The dependencies is compute by an implementation of a {@link eu.stamp_project.automaticbuilder.AutomaticBuilder}
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
                DSpotUtils.getAbsolutePathToDSpotDependencies();
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

    public InputConfiguration setAdditionalClasspathElements(String additionalClasspathElements) {
        this.additionalClasspathElements = additionalClasspathElements;
        return this;
    }

    public InputConfiguration setMavenHome(String mavenHome) {
        this.mavenHome = mavenHome;
        return this;
    }

    public String getMavenHome() {
        return mavenHome;
    }

    public AutomaticBuilder getBuilder() {
        return this.automaticBuilder;
    }

    public InputConfiguration setBuilder(AutomaticBuilder builder) {
        this.automaticBuilder = builder;
        return this;
    }

    private Factory factory;

    public Factory getFactory() {
        return factory;
    }

    public InputConfiguration setFactory(Factory factory) {
        this.factory = factory;
        return this;
    }

    public InputConfiguration setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    public String getDelta() {
        return delta;
    }

    public InputConfiguration setDelta(String delta) {
        this.delta = delta;
        return this;
    }

    public String getAbsolutePathToSecondVersionProjectRoot() {
        return absolutePathToSecondVersionProjectRoot;
    }

    public InputConfiguration setAbsolutePathToSecondVersionProjectRoot(String absolutePathToSecondVersionProjectRoot) {
        this.absolutePathToSecondVersionProjectRoot =
                DSpotUtils.shouldAddSeparator.apply(absolutePathToSecondVersionProjectRoot);
        return this;
    }

    public String getExcludedClasses() {
        return excludedClasses;
    }

    public InputConfiguration setExcludedClasses(String excludedClasses) {
        this.excludedClasses = excludedClasses;
        return this;
    }

    public static final Predicate<CtType> isNotExcluded = ctType ->
            InputConfiguration.get().getExcludedClasses().isEmpty() ||
                    Arrays.stream(InputConfiguration.get().getExcludedClasses().split(","))
                            .map(Pattern::compile)
                            .map(pattern -> pattern.matcher(ctType.getQualifiedName()))
                            .noneMatch(Matcher::matches);

    public String getExcludedTestCases() {
        return excludedTestCases;
    }

    public InputConfiguration setExcludedTestCases(String excludedTestCases) {
        this.excludedTestCases = excludedTestCases;
        return this;
    }

    public String getFilter() {
        return filter;
    }

    public InputConfiguration setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public String getPitVersion() {
        return pitVersion;
    }

    public InputConfiguration setPitVersion(String pitVersion) {
        this.pitVersion = pitVersion;
        return this;
    }

    public String getDescartesVersion() {
        return descartesVersion;
    }

    public InputConfiguration setDescartesVersion(String descartesVersion) {
        this.descartesVersion = descartesVersion;
        return this;
    }

    public String getJVMArgs() {
        return JVMArgs;
    }

    public InputConfiguration setJVMArgs(String JVMArgs) {
        this.JVMArgs = JVMArgs;
        EntryPoint.JVMArgs = String.join(" ", JVMArgs.split(","));
        return this;
    }

    public String getDescartesMutators() {
        return descartesMutators;
    }

    public InputConfiguration setDescartesMutators(String descartesMutators) {
        this.descartesMutators = descartesMutators;
        return this;
    }

    public boolean isDescartesMode() {
        return descartesMode;
    }

    public InputConfiguration setDescartesMode(boolean descartesMode) {
        this.descartesMode = descartesMode;
        if (this.descartesMode) {
            this.setPitVersion("1.4.0"); // forcing pit version 1.4.0 to work with descartes
        } else if (this.getPitVersion() == null) {
            this.setPitVersion("1.4.0");
        }
        return this;
    }

    public boolean shouldUseWorkingDirectory() {
        return useWorkingDirectory;
    }

    public InputConfiguration setUseWorkingDirectory(boolean useWorkingDirectory) {
        this.useWorkingDirectory = useWorkingDirectory;
        if (this.shouldUseWorkingDirectory()) {
            EntryPoint.workingDirectory = new File(this.getAbsolutePathToProjectRoot());
        }
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public InputConfiguration setVerbose(boolean verbose) {
        this.verbose = verbose;
        EntryPoint.verbose = this.isVerbose();
        return this;
    }

    public List<Amplifier> getAmplifiers() {
        return amplifiers;
    }

    public InputConfiguration setAmplifiers(List<Amplifier> amplifiers) {
        this.amplifiers = amplifiers;
        return this;
    }

    public int getNbIteration() {
        return nbIteration;
    }

    public InputConfiguration setNbIteration(int nbIteration) {
        this.nbIteration = nbIteration;
        return this;
    }

    public List<String> getTestClasses() {
        return testClasses;
    }

    public InputConfiguration setTestClasses(List<String> testClasses) {
        this.testClasses = testClasses;
        return this;
    }

    public InputConfiguration addTestClasses(String testClass) {
        this.testClasses.add(testClass);
        return this;
    }

    public TestSelector getSelector() {
        return selector;
    }

    public InputConfiguration setSelector(TestSelector selector) {
        this.selector = selector;
        return this;
    }

    public List<String> getTestCases() {
        return testCases;
    }

    public InputConfiguration setTestCases(List<String> testCases) {
        this.testCases = testCases;
        return this;
    }

    public InputConfiguration addTestCases(List<String> testCases) {
        this.testCases.addAll(testCases);
        return this;
    }

    public InputConfiguration addTestCase(String testCase) {
        this.testCases.add(testCase);
        return this;
    }

    public long getSeed() {
        return seed;
    }

    public InputConfiguration setSeed(long seed) {
        this.seed = seed;
        return this;
    }

    public int getTimeOutInMs() {
        return timeOutInMs;
    }

    public InputConfiguration setTimeOutInMs(int timeOutInMs) {
        this.timeOutInMs = timeOutInMs;
        AmplificationHelper.timeOutInMs = timeOutInMs; // TODO should not be redundant
        return this;
    }

    public Integer getMaxTestAmplified() {
        return maxTestAmplified;
    }

    public InputConfiguration setMaxTestAmplified(Integer maxTestAmplified) {
        this.maxTestAmplified = maxTestAmplified;
        return this;
    }

    public boolean shouldClean() {
        return clean;
    }

    public InputConfiguration setClean(boolean clean) {
        this.clean = clean;
        return this;
    }

    public boolean withComment() {
        return withComment;
    }

    public InputConfiguration setWithComment(boolean withComment) {
        this.withComment = withComment;
        return this;
    }

    private InputAmplDistributorEnum inputAmplDistributor;

    // TODO update after merging #888
    public InputAmplDistributorEnum getBudgetizerEnum() {
        return this.inputAmplDistributor;
    }

    // TODO update after merging #888
    public InputConfiguration setBudgetizerEnum(InputAmplDistributorEnum inputAmplDistributor) {
        this.inputAmplDistributor = inputAmplDistributor;
        return this;
    }

    public Budgetizer getBudgetizer() {
        return this.budgetizer;
    }

    public boolean shouldGenerateAmplifiedTestClass() {
        return generateAmplifiedTestClass;
    }

    public InputConfiguration setGenerateAmplifiedTestClass(boolean generateAmplifiedTestClass) {
        this.generateAmplifiedTestClass = generateAmplifiedTestClass;
        return this;
    }

    public boolean shouldUseMavenToExecuteTest() {
        return useMavenToExecuteTest;
    }

    public InputConfiguration setUseMavenToExecuteTest(boolean useMavenToExecuteTest) {
        this.useMavenToExecuteTest = useMavenToExecuteTest;
        return this;
    }

    public String getPreGoalsTestExecution() {
        return this.preGoalsTestExecution;
    }

    public InputConfiguration setPreGoalsTestExecution(String preGoalsTestExecution) {
        this.preGoalsTestExecution = preGoalsTestExecution;
        return this;
    }

    public boolean shouldKeepOriginalTestMethods() {
        return this.keepOriginalTestMethods;
    }

    public InputConfiguration setKeepOriginalTestMethods(boolean keepOriginalTestMethods) {
        this.keepOriginalTestMethods = keepOriginalTestMethods;
        return this;
    }

    public boolean isJUnit5() {
        return this.isJUnit5;
    }

    public void setJUnit5(boolean JUnit5) {
        isJUnit5 = JUnit5;
    }

    public boolean shouldTargetOneTestClass() {
        return this.targetOneTestClass;
    }

    public InputConfiguration setTargetOneTestClass(boolean targetOneTestClass) {
        this.targetOneTestClass = targetOneTestClass;
        return this;
    }

    public boolean shouldAllowPathInAssertion() {
        return this.allowPathInAssertion;
    }

    public InputConfiguration setAllowPathInAssertion(boolean allowPathInAssertion) {
        this.allowPathInAssertion = allowPathInAssertion;
        return this;
    }

    public boolean shouldExecuteTestsInParallel() {
        return executeTestsInParallel;
    }

    public InputConfiguration setExecuteTestsInParallel(boolean executeTestsInParallel) {
        this.executeTestsInParallel = executeTestsInParallel;
        return this;
    }

    public int getNumberParallelExecutionProcessors() {
        return numberParallelExecutionProcessors;
    }

    public InputConfiguration setNumberParallelExecutionProcessors(int numberParallelExecutionProcessors) {
        this.numberParallelExecutionProcessors = numberParallelExecutionProcessors;
        return this;
    }

    public static void main(String[] args) {
        final InputConfiguration configuration = new InputConfiguration();
        final CommandLine commandLine = new CommandLine(configuration);
        commandLine.parseArgs(
                "--absolute-path-to-project-root", "/home/bdanglot/workspace/dspot/dspot/src/test/resources/test-projects/"
        );
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
        }
        System.out.println("BP");
    }

}
