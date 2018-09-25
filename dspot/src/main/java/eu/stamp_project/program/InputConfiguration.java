package eu.stamp_project.program;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.budget.Budgetizer;
import eu.stamp_project.dspot.budget.NoBudgetizer;
import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
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

    private static Properties loadProperties(String pathToPropertiesFile) {
        try {
            Properties properties = new Properties();
            if (pathToPropertiesFile == null || "".equals(pathToPropertiesFile)) {
                LOGGER.warn("You did not specify any path for the properties file. Using only default values.");
            } else {
                properties.load(new FileInputStream(pathToPropertiesFile));
            }
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputConfiguration get() {
        return InputConfiguration.instance;
    }

    /**
     * This method initialize the instance of the Singleton {@link InputConfiguration}.
     * You can retrieve this instance using {@link InputConfiguration#get()}
     * Build an InputConfiguration from a properties file, given as path.
     * This method will call the default constructor {@link InputConfiguration#InputConfiguration(String, String, String, String, String, String)}
     * Then, uses the properties to initialize other values.
     *
     * @param pathToPropertiesFile the path to the properties file. It is recommended to use an absolute path.
     * @return the new instance of the InputConfiguration
     */
    public static InputConfiguration initialize(String pathToPropertiesFile) {
        InputConfiguration.initialize(loadProperties(pathToPropertiesFile));
        InputConfiguration.instance.configPath = pathToPropertiesFile;
        return InputConfiguration.instance;
    }

    /**
     * This method initialize the instance of the Singleton {@link InputConfiguration}.
     * You can retrieve this instance using {@link InputConfiguration#get()}
     * Build an InputConfiguration from a properties file, given as path.
     * This method will call the default constructor {@link InputConfiguration#InputConfiguration(String, String, String, String, String, String)}
     * Then, uses the properties to initialize other values.
     * The given properties should have least values for :
     * <ul>
     * <li>{@link ConstantsProperties#PROJECT_ROOT_PATH}</li>
     * <li>{@link ConstantsProperties#SRC_CODE}</li>
     * <li>{@link ConstantsProperties#TEST_SRC_CODE}</li>
     * <li>{@link ConstantsProperties#SRC_CLASSES}</li>
     * <li>{@link ConstantsProperties#TEST_CLASSES}</li>
     * <li>{@link ConstantsProperties#MODULE}, in case of multi module project</li>
     * </ul>
     *
     * @param properties the properties. See {@link ConstantsProperties}
     * @return the new instance of the InputConfiguration
     */
    public static InputConfiguration initialize(Properties properties) {
        if (InputConfiguration.instance != null) {
            LOGGER.warn("Erasing old instance of InputConfiguration");
        }
        InputConfiguration.instance = new InputConfiguration(properties);
        InputConfiguration.instance.configPath = "";
        InputConfiguration.instance.initializeBuilder(properties);
        return InputConfiguration.instance;
    }

    private InputConfiguration(Properties properties) {
        // mandatory properties are used in the first constructor, except targetModule, which can be empty
        this(
                ConstantsProperties.PROJECT_ROOT_PATH.get(properties),
                ConstantsProperties.SRC_CODE.get(properties),
                ConstantsProperties.TEST_SRC_CODE.get(properties),
                ConstantsProperties.SRC_CLASSES.get(properties),
                ConstantsProperties.TEST_CLASSES.get(properties),
                ConstantsProperties.MODULE.get(properties)
        );

        this.setAbsolutePathToSecondVersionProjectRoot(new File(
                        DSpotUtils.shouldAddSeparator.apply(
                                ConstantsProperties.PATH_TO_SECOND_VERSION.get(properties)
                        ) + targetModule
                ).getAbsolutePath()
        )
                .setBuilderName(ConstantsProperties.AUTOMATIC_BUILDER_NAME.get(properties));

        final String systemProperties = ConstantsProperties.SYSTEM_PROPERTIES.get(properties);
        if (!systemProperties.isEmpty()) {
            Arrays.stream(systemProperties.split(","))
                    .forEach(systemProperty -> {
                        String[] keyValueInArray = systemProperty.split("=");
                        System.getProperties().put(keyValueInArray[0], keyValueInArray[1]);
                    });
        }

        this.setBudgetizer(new NoBudgetizer())
                .setOutputDirectory(ConstantsProperties.OUTPUT_DIRECTORY.get(properties))
                .setDelta(ConstantsProperties.DELTA_ASSERTS_FLOAT.get(properties))
                .setFilter(ConstantsProperties.FILTER.get(properties))
                .setPitVersion(ConstantsProperties.PIT_VERSION.get(properties))
                .setDescartesVersion(ConstantsProperties.DESCARTES_VERSION.get(properties))
                .setBaseSha(ConstantsProperties.BASE_SHA.get(properties))
                .setExcludedClasses(ConstantsProperties.EXCLUDED_CLASSES.get(properties))
                .setTimeoutPit(ConstantsProperties.TIMEOUT_PIT.get(properties))
                .setJVMArgs(ConstantsProperties.JVM_ARGS.get(properties))
                .setDescartesMutators(ConstantsProperties.DESCARTES_MUTATORS.get(properties))
                .setExcludedTestCases(ConstantsProperties.EXCLUDED_TEST_CASES.get(properties));
    }

    private void initializeBuilder(Properties properties) {
        this.setMavenHome(ConstantsProperties.MAVEN_HOME.get(properties));
        this.builder = AutomaticBuilderFactory.getAutomaticBuilder(ConstantsProperties.AUTOMATIC_BUILDER_NAME.getName());
        this.dependencies = this.builder.compileAndBuildClasspath();

        if (!this.dependencies.contains("junit/junit/4")) {
            this.dependencies = Test.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getFile() +
                    AmplificationHelper.PATH_SEPARATOR + this.dependencies;
        }

        final String additionalClasspathElements = ConstantsProperties.ADDITIONAL_CP_ELEMENTS.get(properties);
        if (!additionalClasspathElements.isEmpty()) {
            String pathToAdditionalClasspathElements = additionalClasspathElements;
            if (!Paths.get(additionalClasspathElements).isAbsolute()) {
                pathToAdditionalClasspathElements =
                        DSpotUtils.shouldAddSeparator.apply(this.absolutePathToProjectRoot +
                                additionalClasspathElements
                        );
            }
            this.dependencies += PATH_SEPARATOR + pathToAdditionalClasspathElements;
        }
        this.setAdditionalClasspathElements(ConstantsProperties.ADDITIONAL_CP_ELEMENTS.get(properties));
    }

    /**
     * This constructor is a proxy for {@link InputConfiguration#InputConfiguration(String, String, String, String, String, String)} with
     * an empty target module
     *
     * @param pathToProjectRoot absolute or relative path to the root of the project.
     * @param pathToSource      relative path from {@code pathToProjectRoot} to the folder that contains the program sources (.java).
     * @param pathToTestSource  relative path from {@code pathToProjectRoot} to the folder that contains the test sources (.java).
     * @param pathToClasses     relative path from {@code pathToProjectRoot} to the folder that contains the program binaries (.class).
     * @param pathToTestClasses relative path from {@code pathToProjectRoot} to the folder that contains the test binaries (.class).
     */
    private InputConfiguration(String pathToProjectRoot,
                               String pathToSource,
                               String pathToTestSource,
                               String pathToClasses,
                               String pathToTestClasses) {
        this(pathToProjectRoot,
                pathToSource,
                pathToTestSource,
                pathToClasses,
                pathToTestClasses,
                ""
        );
    }

    /**
     * Default Constructor. This constructor takes as input the minimal parameters to run DSpot.
     *
     * @param pathToProjectRoot absolute or relative path to the root of the project.
     * @param pathToSource      relative path from {@code pathToProjectRoot} to the folder that contains the program sources (.java).
     * @param pathToTestSource  relative path from {@code pathToProjectRoot} to the folder that contains the test sources (.java).
     * @param pathToClasses     relative path from {@code pathToProjectRoot} to the folder that contains the program binaries (.class).
     * @param pathToTestClasses relative path from {@code pathToProjectRoot} to the folder that contains the test binaries (.class).
     * @param targetModule      relative path from {@code pathToProjectRoot} to the targeted sub-module. This argument can be empty ("") in case of single module project.
     */
    private InputConfiguration(String pathToProjectRoot,
                               String pathToSource,
                               String pathToTestSource,
                               String pathToClasses,
                               String pathToTestClasses,
                               String targetModule) {
        this.setAbsolutePathToProjectRoot(new File(
                        DSpotUtils.shouldAddSeparator.apply(
                                pathToProjectRoot
                        ) + targetModule
                ).getAbsolutePath()
        )
                .setPathToSourceCode(pathToSource)
                .setPathToTestSourceCode(pathToTestSource)
                .setPathToClasses(pathToClasses)
                .setPathToTestClasses(pathToTestClasses)
                .setTargetModule(targetModule)
                .setVerbose(true);
        // force here verbose mode, to have debug during the construction of the InputConfiguration
        // then it will take the command line value (default: false)
    }

    /*
        Paths project properties
     */

    private String absolutePathToProjectRoot;

    /**
     * This method return the absolute path to the project.
     * If the project is multi-modules, the returned path is the path to the specified targetModule properties
     *
     * @return absolute path to the project root
     */
    public String getAbsolutePathToProjectRoot() {
        return absolutePathToProjectRoot;
    }

    /**
     * set the absolute path to the root of the project, and add a / at the end if needed
     *
     * @param absolutePathToProjectRoot
     */
    public InputConfiguration setAbsolutePathToProjectRoot(String absolutePathToProjectRoot) {
        this.absolutePathToProjectRoot = DSpotUtils.shouldAddSeparator.apply(absolutePathToProjectRoot);
        return this;
    }

    private String targetModule;

    public String getTargetModule() {
        return targetModule;
    }

    public InputConfiguration setTargetModule(String targetModule) {
        this.targetModule = targetModule;
        return this;
    }

    private String pathToSourceCode;

    public String getPathToSourceCode() {
        return pathToSourceCode;
    }

    public String getAbsolutePathToSourceCode() {
        return this.absolutePathToProjectRoot + this.getPathToSourceCode();
    }

    public InputConfiguration setPathToSourceCode(String pathToSourceCode) {
        if (new File(pathToSourceCode).isAbsolute()) {
            pathToSourceCode = pathToSourceCode.substring(this.absolutePathToProjectRoot.length());
        }
        this.pathToSourceCode = DSpotUtils.shouldAddSeparator.apply(pathToSourceCode);
        return this;
    }

    private String pathToTestSourceCode;

    public String getPathToTestSourceCode() {
        return pathToTestSourceCode;
    }

    public InputConfiguration setPathToTestSourceCode(String pathToTestSourceCode) {
        if (new File(pathToTestSourceCode).isAbsolute()) {
            pathToTestSourceCode = pathToTestSourceCode.substring(this.absolutePathToProjectRoot.length());
        }
        this.pathToTestSourceCode = DSpotUtils.shouldAddSeparator.apply(pathToTestSourceCode);
        return this;
    }

    public String getAbsolutePathToTestSourceCode() {
        return this.absolutePathToProjectRoot + this.getPathToTestSourceCode();
    }

    /*
        Compilation and dependencies properties
     */

    private String pathToClasses;

    public String getPathToClasses() {
        return pathToClasses;
    }

    public InputConfiguration setPathToClasses(String pathToClasses) {
        if (new File(pathToClasses).isAbsolute()) {
            pathToClasses = pathToClasses.substring(this.absolutePathToProjectRoot.length());
        }
        this.pathToClasses = DSpotUtils.shouldAddSeparator.apply(pathToClasses);
        return this;
    }

    public String getAbsolutePathToClasses() {
        return this.absolutePathToProjectRoot + this.getPathToClasses();
    }

    private String pathToTestClasses;

    public String getPathToTestClasses() {
        return pathToTestClasses;
    }

    public String getAbsolutePathToTestClasses() {
        return this.absolutePathToProjectRoot + this.getPathToTestClasses();
    }

    public InputConfiguration setPathToTestClasses(String pathToTestClasses) {
        if (new File(pathToTestClasses).isAbsolute()) {
            pathToTestClasses = pathToTestClasses.substring(this.absolutePathToProjectRoot.length());
        }
        this.pathToTestClasses = DSpotUtils.shouldAddSeparator.apply(pathToTestClasses);
        return this;
    }

    /**
     * @return path to folders that contain both compiled classes and test classes as a classpath, <i>i.e.</i> separated by
     * the path separator of the system.
     */
    public String getClasspathClassesProject() {
        return this.getAbsolutePathToClasses() + AmplificationHelper.PATH_SEPARATOR + this.getAbsolutePathToTestClasses();
    }

    private String dependencies;

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

    private String additionalClasspathElements;


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

    /*
        Builder properties
     */

    private String builderName;

    public String getBuilderName() {
        return builderName;
    }

    public InputConfiguration setBuilderName(String builderName) {
        this.builderName = builderName;
        return this;
    }

    @Deprecated
    private String mavenHome;

    @Deprecated
    public InputConfiguration setMavenHome(String mavenHome) {
        this.mavenHome = mavenHome;
        return this;
    }

    @Deprecated
    public String getMavenHome() {
        return mavenHome;
    }

    private AutomaticBuilder builder;

    public AutomaticBuilder getBuilder() {
        return this.builder;
    }

    public InputConfiguration setBuilder(AutomaticBuilder builder) {
        this.builder = builder;
        return this;
    }

    /*
        General properties
     */

    private Factory factory;

    /**
     * Spoon factory to process all AST elements
     */
    public Factory getFactory() {
        return factory;
    }

    public InputConfiguration setFactory(Factory factory) {
        this.factory = factory;
        return this;
    }

    private String outputDirectory;

    public InputConfiguration setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    @Deprecated
    private String configPath;

    @Deprecated
    public String getConfigPath() {
        return configPath;
    }

    @Deprecated
    public InputConfiguration setConfigPath(String configPath) {
        this.configPath = configPath;
        return this;
    }

    /*
        Assertions properties
     */

    private String delta;

    public String getDelta() {
        return delta;
    }

    public InputConfiguration setDelta(String delta) {
        this.delta = delta;
        return this;
    }

    /*
        ChangeDetector properties
     */

    private String absolutePathToSecondVersionProjectRoot;

    public String getAbsolutePathToSecondVersionProjectRoot() {
        return absolutePathToSecondVersionProjectRoot;
    }

    public InputConfiguration setAbsolutePathToSecondVersionProjectRoot(String absolutePathToSecondVersionProjectRoot) {
        this.absolutePathToSecondVersionProjectRoot =
                DSpotUtils.shouldAddSeparator.apply(absolutePathToSecondVersionProjectRoot);
        return this;
    }

    private String baseSha;

    public String getBaseSha() {
        return baseSha;
    }

    public InputConfiguration setBaseSha(String baseSha) {
        this.baseSha = baseSha;
        return this;
    }

    /*
        Amplification and Pit properties
     */

    private String excludedClasses;

    public String getExcludedClasses() {
        return excludedClasses;
    }

    public InputConfiguration setExcludedClasses(String excludedClasses) {
        this.excludedClasses = excludedClasses;
        return this;
    }

    private String excludedTestCases;

    public String getExcludedTestCases() {
        return excludedTestCases;
    }

    public InputConfiguration setExcludedTestCases(String excludedTestCases) {
        this.excludedTestCases = excludedTestCases;
        return this;
    }

    /*
        Pit properties
     */

    private String filter;

    public String getFilter() {
        return filter;
    }

    public InputConfiguration setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    private String pitVersion;

    public String getPitVersion() {
        return pitVersion;
    }

    public InputConfiguration setPitVersion(String pitVersion) {
        this.pitVersion = pitVersion;
        return this;
    }

    private String descartesVersion;

    public String getDescartesVersion() {
        return descartesVersion;
    }

    public InputConfiguration setDescartesVersion(String descartesVersion) {
        this.descartesVersion = descartesVersion;
        return this;
    }

    private String timeoutPit;

    public String getTimeoutPit() {
        return timeoutPit;
    }

    public InputConfiguration setTimeoutPit(String timeoutPit) {
        this.timeoutPit = timeoutPit;
        return this;
    }

    private String JVMArgs;

    public String getJVMArgs() {
        return JVMArgs;
    }

    public InputConfiguration setJVMArgs(String JVMArgs) {
        this.JVMArgs = JVMArgs;
        return this;
    }

    private String descartesMutators;

    public String getDescartesMutators() {
        return descartesMutators;
    }

    public InputConfiguration setDescartesMutators(String descartesMutators) {
        this.descartesMutators = descartesMutators;
        return this;
    }

    private boolean descartesMode;

    public boolean isDescartesMode() {
        return descartesMode;
    }

    public InputConfiguration setDescartesMode(boolean descartesMode) {
        this.descartesMode = descartesMode;
        return this;
    }

    /*
        Inherited from old Configuration (from command line)
     */

    private List<Amplifier> amplifiers = Collections.emptyList();
    private int nbIteration = 3;
    private List<String> testClasses = Collections.singletonList("all");
    private TestSelector selector = new PitMutantScoreSelector();
    private List<String> testCases = Collections.emptyList();
    private long seed = 23L;
    private int timeOutInMs = 10000;
    private String automaticBuilderName = "MAVEN";
    private Integer maxTestAmplified = 200;
    private boolean clean = false;
    private boolean minimize = false;
    private boolean verbose = false;
    private boolean useWorkingDirectory = false;
    private boolean withComment = false;

    public boolean shouldUseWorkingDirectory() {
        return useWorkingDirectory;
    }

    /**
     * Side effect: assign the same value to {@link eu.stamp_project.testrunner.EntryPoint#workingDirectory}
     *
     * @param useWorkingDirectory of the verbose mode.
     * @return an instance of this InputConfiguration
     */
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

    /**
     * Side effect: assign the same value to {@link eu.stamp_project.testrunner.EntryPoint#verbose}
     *
     * @param verbose value of the verbose mode.
     * @return an instance of this InputConfiguration
     */
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

    public String getAutomaticBuilderName() {
        return automaticBuilderName;
    }

    public InputConfiguration setAutomaticBuilderName(String automaticBuilderName) {
        this.automaticBuilderName = automaticBuilderName;
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

    public boolean shouldMinimize() {
        return minimize;
    }

    public InputConfiguration setMinimize(boolean minimize) {
        this.minimize = minimize;
        return this;
    }

    public boolean withComment() {
        return withComment;
    }

    public InputConfiguration setWithComment(boolean withComment) {
        this.withComment = withComment;
        return this;
    }

    private Budgetizer budgetizer;

    public Budgetizer getBudgetizer() {
        return budgetizer;
    }

    public InputConfiguration setBudgetizer(Budgetizer budgetizer) {
        this.budgetizer = budgetizer;
        return this;
    }

    private boolean generateAmplifiedTestClass;

    /**
     * If this is true, then DSpot will creates new test class with only amplified test methods.
     * This new test class will be named with "Ampl" as suffix or prefix depending of the name of the original test class:
     * <i>e.g.</i> MyClassTest will be AmplMyClassTest and TestMyClass will be TestMyClassAmpl
     */
    public boolean isGenerateAmplifiedTestClass() {
        return generateAmplifiedTestClass;
    }

    public InputConfiguration setGenerateAmplifiedTestClass(boolean generateAmplifiedTestClass) {
        this.generateAmplifiedTestClass = generateAmplifiedTestClass;
        return this;
    }

    /**
     * This boolean say if we must use maven to execute the test. If not, the tests will be executed with a java command line
     */
    private boolean useMavenToExecuteTest = false;

    public boolean isUseMavenToExecuteTest() {
        return useMavenToExecuteTest;
    }

    public InputConfiguration setUseMavenToExecuteTest(boolean useMavenToExecuteTest) {
        this.useMavenToExecuteTest = useMavenToExecuteTest;
        return this;
    }
}