package eu.stamp_project.program;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(InputConfiguration.class);

    private static Properties loadProperties(String pathToPropertiesFile) {
        try {
            Properties properties = new Properties();
            if (pathToPropertiesFile == null) {
                LOGGER.warn("You did not specify any path for the properties file. Using only default values.");
            } else {
                properties.load(new FileInputStream(pathToPropertiesFile));
            }
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build an InputConfiguration from a properties file, given as path.
     * This constructor will call the default constructor {@link InputConfiguration#InputConfiguration(String, String, String, String, String, String)}
     * Then, uses the properties to initialize other values.
     *
     * @param pathToPropertiesFile the path to the properties file. It is recommended to use an absolute path.
     */
    public InputConfiguration(String pathToPropertiesFile) throws IOException {
        this(loadProperties(pathToPropertiesFile));
        this.configPath = pathToPropertiesFile;
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
        );
        this.setBuilderName(ConstantsProperties.AUTOMATIC_BUILDER_NAME.get(properties));
        this.builder = AutomaticBuilderFactory.getAutomaticBuilder(this);
        this.dependencies = this.builder.buildClasspath();

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

        final String systemProperties = ConstantsProperties.SYSTEM_PROPERTIES.get(properties);
        if (!systemProperties.isEmpty()) {
            Arrays.stream(systemProperties.split(","))
                    .forEach(systemProperty -> {
                        String[] keyValueInArray = systemProperty.split("=");
                        System.getProperties().put(keyValueInArray[0], keyValueInArray[1]);
                    });
        }

        this.setOutputDirectory(ConstantsProperties.OUTPUT_DIRECTORY.get(properties));
        this.setMavenHome(ConstantsProperties.MAVEN_HOME.get(properties));
        this.setDelta(ConstantsProperties.DELTA_ASSERTS_FLOAT.get(properties));
        this.setFilter(ConstantsProperties.FILTER.get(properties));
        this.setPitVersion(ConstantsProperties.PIT_VERSION.get(properties));
        this.setDescartesVersion(ConstantsProperties.DESCARTES_VERSION.get(properties));
        this.setBaseSha(ConstantsProperties.BASE_SHA.get(properties));
        this.setExcludedClasses(ConstantsProperties.EXCLUDED_CLASSES.get(properties));
        this.setTimeoutPit(ConstantsProperties.TIMEOUT_PIT.get(properties));
        this.setJVMArgs(ConstantsProperties.JVM_ARGS.get(properties));
        this.setDescartesVersion(ConstantsProperties.DESCARTES_MUTATORS.get(properties));
        this.setExcludedTestCases(ConstantsProperties.EXCLUDED_TEST_CASES.get(properties));
    }

    /**
     * This constructor is a proxy for {@link InputConfiguration#InputConfiguration(String, String, String, String, String, String)} with
     * an empty target module
     * @param pathToProjectRoot absolute or relative path to the root of the project.
     * @param pathToSource      relative path from {@code pathToProjectRoot} to the folder that contains the program sources (.java).
     * @param pathToTestSource  relative path from {@code pathToProjectRoot} to the folder that contains the test sources (.java).
     * @param pathToClasses     relative path from {@code pathToProjectRoot} to the folder that contains the program binaries (.class).
     * @param pathToTestClasses relative path from {@code pathToProjectRoot} to the folder that contains the test binaries (.class).
     */
    public InputConfiguration(String pathToProjectRoot,
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
    public InputConfiguration(String pathToProjectRoot,
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
        );
        this.setPathToSourceCode(pathToSource);
        this.setPathToTestSourceCode(pathToTestSource);
        this.setPathToClasses(pathToClasses);
        this.setPathToTestClasses(pathToTestClasses);
        this.setTargetModule(targetModule);
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
    public void setAbsolutePathToProjectRoot(String absolutePathToProjectRoot) {
        this.absolutePathToProjectRoot = DSpotUtils.shouldAddSeparator.apply(absolutePathToProjectRoot);
    }

    private String targetModule;

    public String getTargetModule() {
        return targetModule;
    }

    public void setTargetModule(String targetModule) {
        this.targetModule = targetModule;
    }

    private String pathToSourceCode;

    public String getPathToSourceCode() {
        return pathToSourceCode;
    }

    public String getAbsolutePathToSourceCode() {
        return this.absolutePathToProjectRoot + this.getPathToSourceCode();
    }

    public void setPathToSourceCode(String pathToSourceCode) {
        this.pathToSourceCode = DSpotUtils.shouldAddSeparator.apply(pathToSourceCode);
    }

    private String pathToTestSourceCode;

    public String getPathToTestSourceCode() {
        return pathToTestSourceCode;
    }

    public void setPathToTestSourceCode(String pathToTestSourceCode) {
        this.pathToTestSourceCode = DSpotUtils.shouldAddSeparator.apply(pathToTestSourceCode);
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

    public void setPathToClasses(String pathToClasses) {
        this.pathToClasses = DSpotUtils.shouldAddSeparator.apply(pathToClasses);
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

    public void setPathToTestClasses(String pathToTestClasses) {
        this.pathToTestClasses = DSpotUtils.shouldAddSeparator.apply(pathToTestClasses);
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
     * @return the full classpath of the project. This full classpath is composed of: the returned values of {@link #getClasspathClassesProject}, {@link #getDependencies()} and {@link DSpotUtils#PATH_TO_EXTRA_DEPENDENCIES_TO_DSPOT_CLASSES} separated by the path separator of the system, <i>i.e.</i> as a classpath.
     */
    public String getFullClassPathWithExtraDependencies() {
        return this.getClasspathClassesProject() + AmplificationHelper.PATH_SEPARATOR +
                this.getDependencies() + AmplificationHelper.PATH_SEPARATOR +
                DSpotUtils.PATH_TO_EXTRA_DEPENDENCIES_TO_DSPOT_CLASSES;
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

    public void setAdditionalClasspathElements(String additionalClasspathElements) {
        this.additionalClasspathElements = additionalClasspathElements;
    }

    /*
        Builder properties
     */

    private String builderName;

    public String getBuilderName() {
        return builderName;
    }

    public void setBuilderName(String builderName) {
        this.builder = null;
        this.builderName = builderName;
    }

    private String mavenHome;

    public void setMavenHome(String mavenHome) {
        this.mavenHome = mavenHome;
    }

    public String getMavenHome() {
        return mavenHome;
    }

    private AutomaticBuilder builder;

    public AutomaticBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(AutomaticBuilder builder) {
        this.builder = builder;
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

    public void setFactory(Factory factory) {
        this.factory = factory;
    }

    private String outputDirectory;

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    private String configPath;

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    /*
        Assertions properties
     */

    private String delta;

    public String getDelta() {
        return delta;
    }

    public void setDelta(String delta) {
        this.delta = delta;
    }

    /*
        ChangeDetector properties
     */

    private String absolutePathToSecondVersionProjectRoot;

    public String getAbsolutePathToSecondVersionProjectRoot() {
        return absolutePathToSecondVersionProjectRoot;
    }

    public void setAbsolutePathToSecondVersionProjectRoot(String absolutePathToSecondVersionProjectRoot) {
        this.absolutePathToSecondVersionProjectRoot =
                DSpotUtils.shouldAddSeparator.apply(absolutePathToSecondVersionProjectRoot);
    }

    private String baseSha;

    public String getBaseSha() {
        return baseSha;
    }

    public void setBaseSha(String baseSha) {
        this.baseSha = baseSha;
    }

    /*
        Amplification and Pit properties
     */

    private String excludedClasses;

    public String getExcludedClasses() {
        return excludedClasses;
    }

    public void setExcludedClasses(String excludedClasses) {
        this.excludedClasses = excludedClasses;
    }

    private String excludedTestCases;

    public String getExcludedTestCases() {
        return excludedTestCases;
    }

    public void setExcludedTestCases(String excludedTestCases) {
        this.excludedTestCases = excludedTestCases;
    }

    /*
        Pit properties
     */

    private String filter;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private String pitVersion;

    public String getPitVersion() {
        return pitVersion;
    }

    public void setPitVersion(String pitVersion) {
        this.pitVersion = pitVersion;
    }

    private String descartesVersion;

    public String getDescartesVersion() {
        return descartesVersion;
    }

    public void setDescartesVersion(String descartesVersion) {
        this.descartesVersion = descartesVersion;
    }

    private String timeoutPit;

    public String getTimeoutPit() {
        return timeoutPit;
    }

    public void setTimeoutPit(String timeoutPit) {
        this.timeoutPit = timeoutPit;
    }

    private String JVMArgs;

    public String getJVMArgs() {
        return JVMArgs;
    }

    public void setJVMArgs(String JVMArgs) {
        this.JVMArgs = JVMArgs;
    }

    private String descartesMutators;

    public String getDescartesMutators() {
        return descartesMutators;
    }

    public void setDescartesMutators(String descartesMutators) {
        this.descartesMutators = descartesMutators;
    }

    @Override
    public String toString() {
        return "InputConfiguration{" +
                "absolutePathToProjectRoot='" + absolutePathToProjectRoot + '\'' +
                ", targetModule='" + targetModule + '\'' +
                ", pathToSourceCode='" + pathToSourceCode + '\'' +
                ", pathToTestSourceCode='" + pathToTestSourceCode + '\'' +
                ", pathToClasses='" + pathToClasses + '\'' +
                ", pathToTestClasses='" + pathToTestClasses + '\'' +
                ", dependencies='" + dependencies + '\'' +
                ", additionalClasspathElements='" + additionalClasspathElements + '\'' +
                ", builderName='" + builderName + '\'' +
                ", mavenHome='" + mavenHome + '\'' +
                ", builder=" + builder +
                ", factory=" + factory +
                ", outputDirectory='" + outputDirectory + '\'' +
                ", configPath='" + configPath + '\'' +
                ", delta='" + delta + '\'' +
                ", absolutePathToSecondVersionProjectRoot='" + absolutePathToSecondVersionProjectRoot + '\'' +
                ", baseSha='" + baseSha + '\'' +
                ", excludedClasses='" + excludedClasses + '\'' +
                ", excludedTestCases='" + excludedTestCases + '\'' +
                ", filter='" + filter + '\'' +
                ", pitVersion='" + pitVersion + '\'' +
                ", descartesVersion='" + descartesVersion + '\'' +
                ", timeoutPit='" + timeoutPit + '\'' +
                ", JVMArgs='" + JVMArgs + '\'' +
                ", descartesMutators='" + descartesMutators + '\'' +
                '}';
    }
}