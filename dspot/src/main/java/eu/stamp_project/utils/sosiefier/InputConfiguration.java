package eu.stamp_project.utils.sosiefier;

import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private String computeProgramDirectory() {
        return DSpotUtils.shouldAddSeparator.apply(new File(
                DSpotUtils.shouldAddSeparator.apply(this.getProperty("project"))
                        + (this.getProperty("targetModule") != null ?
                        DSpotUtils.shouldAddSeparator.apply(this.getProperty("targetModule")) : ""))
                .getAbsolutePath());
    }

    /**
     * Internal properties
     */
    @Deprecated
    protected Properties prop;

    public InputConfiguration() {
        prop = new Properties();
        setDefaultProperties();
    }

    /**
     * Build a InputConfiguration from a properties file given as an InputStream
     * The InputConfiguration uses the following properties:
     * <ul>
     * <li><b>project</b><i>[mandatory]</i>: specify the path to the root of the project. This path can be absolute (recommended) but also relative to the working directory of the DSpot process. We consider as root of the project folder that contain the top-most parent in a multi-module project.</li>
     * <li><b>targetModule</b><i>[optional]</i>: specify a relative path from the path specified by the property <b>project</b> to a sub-module of the project. DSpot works at module level, if your project is multi-module, you must specify which module you want to amplify.</li>
     * </ul>
     *
     * @param stream
     * @throws IOException
     */
    public InputConfiguration(InputStream stream) throws IOException {
        prop = new Properties();
        setDefaultProperties();
        prop.load(stream);
        this.setPathToClasses(
                DSpotUtils.shouldAddSeparator.apply(prop.getProperty("classes", "target/classes"))
        );
        this.setPathToTestClasses(
                DSpotUtils.shouldAddSeparator.apply(prop.getProperty("testclasses", "target/test-classes"))
        );
        this.setPathToSourceCode(DSpotUtils.shouldAddSeparator.apply(prop.getProperty("src", "src/main/java")));
        this.setPathToTestSourceCode(DSpotUtils.shouldAddSeparator.apply(prop.getProperty("test", "src/test/java")));
        this.absolutePathToProjectRoot = this.computeProgramDirectory();
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

        if (prop.getProperty("systemProperties") != null) {
            Arrays.stream(prop.getProperty("systemProperties").split(","))
                    .forEach(systemProperty -> {
                        String[] keyValueInArray = systemProperty.split("=");
                        System.getProperties().put(keyValueInArray[0], keyValueInArray[1]);
                    });
        }
    }

    public InputConfiguration(File project, File srcDir, File testDir, File classesDir, File testClassesDir,
                              File tempDir, String filter, File mavenHome) throws IOException {
        this();
        getProperties().setProperty("project", project.getAbsolutePath());
        getProperties().setProperty("src", getRelativePath(srcDir));
        getProperties().setProperty("testSrc", getRelativePath(testDir));
//		getProperties().setProperty("testResources", getRelativePath(testResourcesDir));
//		getProperties().setProperty("srcResources", getRelativePath(srcResourcesDir));
        getProperties().setProperty("maven.home", mavenHome.getAbsolutePath());
        getProperties().setProperty("classes", getRelativePath(classesDir));
        getProperties().setProperty("tmpDir", getRelativePath(tempDir));
        if (filter != null) {
            getProperties().setProperty("filter", filter);
        }
        getProperties().setProperty("javaVersion", "8");
    }

    private String getRelativePath(File path) {
        String base = getProperties().getProperty("project");
        String relative = new File(base).toURI().relativize(path.toURI()).getPath();
        return relative;
    }

    public InputConfiguration(String file) throws IOException {
        this(new FileInputStream(file));
    }

    /**
     * Return the internal properties
     *
     * @return Proprties instance
     */
    @Deprecated
    public Properties getProperties() {
        return prop;
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
        return prop.getProperty("src");
    }

    /**
     * Returns the full path of the test (source) code of a project.
     *
     * @return String with the path
     */
    public String getRelativeTestSourceCodeDir() {
        return prop.getProperty("testSrc", "src/test/java");
    }


    /**
     * Returns the path of the built classes
     *
     * @return String with the path
     */
    public String getClassesDir() {
        return prop.getProperty("classes");
    }

    /**
     * Returns the output path
     *
     * @return
     */
    public String getOutputDirectory() {
        return prop.getProperty("outputDirectory", "output");
    }

    protected void setDefaultProperties() {
        prop.setProperty("src", "src/main/java");
        prop.setProperty("testSrc", "src/test/java");
        prop.setProperty("classes", "target/classes");
        prop.setProperty("javaVersion", "5");
        prop.setProperty("tmpDir", "tmpDir"); // TODO Checks usage
        prop.setProperty("outputDirectory", "output");
        prop.setProperty("timeOut", "-1"); // TODO Checks usage
        prop.setProperty("logLevel", "2"); // TODO Checks usage
        prop.setProperty("builder", "maven");
        prop.setProperty("pom", "/pom.xml");
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