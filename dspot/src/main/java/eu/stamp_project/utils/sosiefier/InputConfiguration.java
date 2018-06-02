package eu.stamp_project.utils.sosiefier;

import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import static eu.stamp_project.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * The input configuration class encapsulates all the data and associated behavior we obtain from the input properties
 * given by the user.
 * Created by marcel on 8/06/14.
 * This version of the InputConfiguration has been largely modified, and customized to be use in DSpot.
 */
public class InputConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputConfiguration.class);

    /**
     * Internal properties
     */
    @Deprecated
    protected static Properties properties;

    private static Properties loadProperties(String pathToPropertiesFile) {
        try {
            Properties properties = new Properties();
            properties.setProperty("src", "src/main/java");
            properties.setProperty("testSrc", "src/test/java");
            properties.setProperty("classes", "target/classes");
            properties.setProperty("javaVersion", "5");
            properties.setProperty("tmpDir", "tmpDir"); // TODO Checks usage
            properties.setProperty("outputDirectory", "output");
            properties.setProperty("timeOut", "-1"); // TODO Checks usage
            properties.setProperty("logLevel", "2"); // TODO Checks usage
            properties.setProperty("builder", "maven");
            properties.setProperty("pom", "/pom.xml");
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

    public InputConfiguration() {
        this(loadProperties(null));
    }

    /**
     * Build an InputConfiguration from a properties file given as an InputStream.
     * See {@link InputConfiguration#InputConfiguration(String, String, String, String, String)}
     */
    public InputConfiguration(String pathToPropertiesFile) throws IOException {
        this(loadProperties(pathToPropertiesFile));
    }

    public InputConfiguration(Properties properties) {
        this(
                computeProgramDirectory(properties),
                DSpotUtils.shouldAddSeparator.apply(properties.getProperty("src", "src/main/java")),
                DSpotUtils.shouldAddSeparator.apply(properties.getProperty("test", "src/test/java")),
                DSpotUtils.shouldAddSeparator.apply(properties.getProperty("classes", "target/classes")),
                DSpotUtils.shouldAddSeparator.apply(properties.getProperty("testclasses", "target/test-classes"))
        );
    }

    /**
     * call {@link InputConfiguration#InputConfiguration(String, String, String, String, String, String, String)}
     * with two last parameters with null, which are optional.
     * @param absolutePathToProjectRoot
     * @param pathToSource
     * @param pathToTestSource
     * @param pathToClasses
     * @param pathToTestClasses
     */
    public InputConfiguration(String absolutePathToProjectRoot,
                              String pathToSource,
                              String pathToTestSource,
                              String pathToClasses,
                              String pathToTestClasses) {
        this(absolutePathToProjectRoot, pathToSource, pathToTestSource, pathToClasses, pathToTestClasses, null, null);
    }

    /**
     * Build an InputConfiguration from a properties file given as an InputStream
     * The InputConfiguration uses the following properties:
     * <ul>
     * <li><b>project</b><i>[mandatory]</i>: specify the path to the root of the project. This path can be absolute (recommended) but also relative to the working directory of the DSpot process. We consider as root of the project folder that contain the top-most parent in a multi-module project.</li>
     * <li><b>targetModule</b><i>[optional]</i>: specify a relative path from the path specified by the property <b>project</b> to a sub-module of the project. DSpot works at module level, if your project is multi-module, you must specify which module you want to amplify.</li>
     * </ul>
     * @param absolutePathToProjectRoot
     * @param pathToSource
     * @param pathToTestSource
     * @param pathToClasses
     * @param pathToTestClasses
     * @param additionalClasspathElements optional in case your program rely on classpath elements outside the traditionnal classpath, <i>i.e.</i> specified by {@code pathToClasses} or {@code pathToTestClasses} or in the classpath build with the builder, <i>e.g.</i> maven.
     * @param systemProperties optional in case your program need some system properties. Specify them with the following format:
     *                         sysProperty1=value1,sysProperty2=value2
     */
    public InputConfiguration(String absolutePathToProjectRoot,
                              String pathToSource,
                              String pathToTestSource,
                              String pathToClasses,
                              String pathToTestClasses,
                              String additionalClasspathElements,
                              String systemProperties) {
        this.setAbsolutePathToProjectRoot(absolutePathToProjectRoot);
        this.setPathToSourceCode(pathToSource);
        this.setPathToTestSourceCode(pathToTestSource);
        this.setPathToClasses(pathToClasses);
        this.setPathToTestClasses(pathToTestClasses);
        this.dependencies = AutomaticBuilderFactory.getAutomaticBuilder(this).buildClasspath();
        if (systemProperties != null) {
            String pathToAdditionalClasspathElements = additionalClasspathElements;
            if (!Paths.get(additionalClasspathElements).isAbsolute()) {
                pathToAdditionalClasspathElements =
                        DSpotUtils.shouldAddSeparator.apply(this.absolutePathToProjectRoot +
                                additionalClasspathElements
                        );
            }
            this.dependencies += PATH_SEPARATOR + pathToAdditionalClasspathElements;
        }
        if (additionalClasspathElements != null) {
            Arrays.stream(additionalClasspathElements.split(","))
                    .forEach(systemProperty -> {
                        String[] keyValueInArray = systemProperty.split("=");
                        System.getProperties().put(keyValueInArray[0], keyValueInArray[1]);
                    });
        }
    }

    public InputConfiguration(File project, File srcDir, File testDir, File classesDir, File testClassesDir,
                              File tempDir, String filter, File mavenHome) throws IOException {
        this();
        getProperties().setProperty("project", project.getAbsolutePath()+"/");
        this.absolutePathToProjectRoot = project.getAbsolutePath()+"/";
        getProperties().setProperty("src", getRelativePath(srcDir));
        this.setPathToSourceCode(getRelativePath(srcDir));
        getProperties().setProperty("testSrc", getRelativePath(testDir));
        this.setPathToTestSourceCode(getRelativePath(testDir));
//		getProperties().setProperty("testResources", getRelativePath(testResourcesDir));
//		getProperties().setProperty("srcResources", getRelativePath(srcResourcesDir));
        getProperties().setProperty("maven.home", mavenHome.getAbsolutePath());
        //getProperties().setProperty("classes", getRelativePath(classesDir));
        this.setPathToClasses(DSpotUtils.shouldAddSeparator.apply(getRelativePath(classesDir)));
        this.setPathToTestClasses(DSpotUtils.shouldAddSeparator.apply(getRelativePath(testClassesDir)));
        getProperties().setProperty("tmpDir", getRelativePath(tempDir));
        if (filter != null) {
            getProperties().setProperty("filter", filter);
        }
        getProperties().setProperty("javaVersion", "8");

        this.dependencies = AutomaticBuilderFactory.getAutomaticBuilder(this).buildClasspath();
        if (prop.getProperty("additionalClasspathElements") != null) {
            String pathToAdditionnalClasspathElements = prop.getProperty("additionalClasspathElements");
            if (!Paths.get(prop.getProperty("additionalClasspathElements")).isAbsolute()) {
                pathToAdditionnalClasspathElements =
                        DSpotUtils.shouldAddSeparator.apply(this.absolutePathToProjectRoot +
                                prop.getProperty("additionalClasspathElements")
                        );
            }
            this.dependencies += PATH_SEPARATOR + pathToAdditionnalClasspathElements;
        }
    }

    private String getRelativePath(File path) {
        String base = this.absolutePathToProjectRoot;
        String relative = new File(base).toURI().relativize(path.toURI()).getPath();
        return relative;
    }

    private static String computeProgramDirectory(Properties properties) {
        return DSpotUtils.shouldAddSeparator.apply(new File(
                DSpotUtils.shouldAddSeparator.apply(properties.getProperty("project"))
                        + (properties.getProperty("targetModule") != null ?
                        DSpotUtils.shouldAddSeparator.apply(properties.getProperty("targetModule")) : ""))
                .getAbsolutePath());
    }

    /**
     * Return the internal properties
     *
     * @return Proprties instance
     */
    @Deprecated
    public Properties getProperties() {
        return properties;
    }

    /**
     * Gets the specific value of a property
     *
     * @param key Key to the value
     * @return A string with the value                                                      g
     */
    @Deprecated
    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    /**
     * Gets the specific value of a property
     *
     * @param key          Key to the value
     * @param defaultValue Default value to set
     * @return A string with the value
     */
    @Deprecated
    public String getProperty(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    /**
     * Gets the project path. The project path is the parent directory where all files concerning a project are.
     *
     * @return String with the path
     */
    public String getProjectPath() {
        return getAbsolutePath(getProperty("project"));
    }

    /**
     * Returns the full path of the production (source) code of a project.
     *
     * @return String with the path
     */
    public String getRelativeSourceCodeDir() {
        return properties.getProperty("src");
    }

    /**
     * Returns the full path of the test (source) code of a project.
     *
     * @return String with the path
     */
    public String getRelativeTestSourceCodeDir() {
        return properties.getProperty("testSrc", "src/test/java");
    }


    /**
     * Returns the path of the built classes
     *
     * @return String with the path
     */
    public String getClassesDir() {
        return properties.getProperty("classes");
    }

    /**
     * Returns the output path
     *
     * @return
     */
    public String getOutputDirectory() {
        return properties.getProperty("outputDirectory", "output");
    }

    protected String getAbsolutePath(String path) {
        Path p = Paths.get(path);
        if (new File(path).exists() || p.isAbsolute()) {
            return path;
        }
        return p.normalize().toString().replace(File.separator, "/");
    }

    @Override
    public String toString() {
        String toReturn = "";
        Properties prop = this.getProperties();
        Set keys = prop.keySet();
        for (Object key : keys) {
            toReturn += key + ": " + prop.getProperty((String) key) + "\n";
        }
        toReturn += "ClassesDir: " + this.getClassesDir() + "\n";
        toReturn += "outputDirectory: " + this.getOutputDirectory() + "\n";
        toReturn += "projectPath: " + this.getProjectPath() + "\n";
        toReturn += "relativeSourceCodeDir: " + this.getRelativeSourceCodeDir() + "\n";
        toReturn += "relativeTestSourceCodeDir: " + this.getRelativeTestSourceCodeDir() + "\n";
        return toReturn;
    }

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

    private String pathToClasses;

    public String getPathToClasses() {
        return pathToClasses;
    }

    public void setPathToClasses(String pathToClasses) {
        this.pathToClasses = pathToClasses;
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
        this.pathToTestClasses = pathToTestClasses;
    }

    /**
     * @return path to folders that contain both compiled classes and test classes as a classpath, <i>i.e.</i> separated by
     * the path separator of the system.
     */
    public String getClasspathClassesProject() {
        return this.getAbsolutePathToClasses() + AmplificationHelper.PATH_SEPARATOR + this.getAbsolutePathToTestClasses();
    }

    private String pathToSourceCode;

    public String getPathToSourceCode() {
        return pathToSourceCode;
    }

    public String getAbsolutePathToSourceCode() {
        return this.absolutePathToProjectRoot + this.getPathToSourceCode();
    }

    public void setPathToSourceCode(String pathToSourceCode) {
        this.pathToSourceCode = pathToSourceCode;
    }

    private String pathToTestSourceCode;

    public String getPathToTestSourceCode() {
        return pathToTestSourceCode;
    }

    public void setPathToTestSourceCode(String pathToTestSourceCode) {
        this.pathToTestSourceCode = pathToTestSourceCode;
    }

    public String getAbsolutePathToTestSourceCode() {
        return this.absolutePathToProjectRoot + this.getPathToTestSourceCode();
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
}