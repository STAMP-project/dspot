package eu.stamp_project.automaticbuilder.maven;

import eu.stamp_project.utils.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/10/18
 * <p>
 * This class generate a copy of the original pom, with a new profile to run pit.
 * The profile is configured to fit the configuration of the Command and the properties file given to DSpot.
 * <p>
 * There is no more modification of the original pom.
 */
public class DSpotPOMCreator {

    /*
        PIT
     */

    private static final String GROUP_ID_PIT = "org.pitest";

    private static final String ARTIFACT_ID_PIT = "pitest-maven";

    private static final String MUTATION_ENGINE_GREGOR = "gregor";

    /*
        DESCARTES
     */

    private static final String GROUP_ID_DESCARTES = "eu.stamp-project";

    private static final String ARTIFACT_ID_DESCARTES = "descartes";

    private static final String MUTATION_ENGINE_DESCARTES = "descartes";

    private static final String[] outputFormats = new String[]{"CSV","XML"};

    /*
        MAVEN
     */

    private static final String ORG_MAVEN_PLUGINS_GROUP_ID = "org.apache.maven.plugins";

    /*
        PLUGIN TEST COMPILER
     */

    private static final String ARTIFACT_ID_TEST_COMPILE = "maven-compiler-plugin";

    /*
        PLUGIN TEST COMPILER
     */

    private static final String ARTIFACT_SUREFIRE_PLUGIN = "maven-surefire-plugin";

    /*
        GENERAL
     */

    public static final String POM_FILE = "pom.xml";

    private static final String PROJECT = "project";

    private static final String PROFILES = "profiles";

    private static final String DSPOT_POM_FILE = ".dspot_";

    private static final String SUFFIX_JUNIT5 = "junit5_";

    private static final String DSPOT_PARALLEL_POM_FILE = ".dspot_parallel_";

    public static boolean isCurrentlyJUnit5;

    /*
      This boolean is redundant with InputConfiguration.isJUnit5(), but it allows to create two pom directly.
      We build two DSpotPOMCreator with true and false, and generate two different pom.
      In this, way, we reuse the same code to generate.
      We do that because at the moment we generate the pom, we do not if we  amplify JUnit5 tests or JUnit4.
      Then, we use InputConfiguration.isJUnit5() to know which pom must be used.
   */
    private boolean isJUnit5;

    private String absolutePathToProjectRoot;

    private boolean shouldExecuteTestsInParallel;

    private boolean shouldUseMavenToExecuteTest;

    private boolean isDescartesMode;

    private String pitVersion;

    private String descartesVersion;

    private String additionalClasspathElements;

    private String JVMArgs;

    private String excludedClasses;

    private String descartesMutators;

    private String filter;

    private int timeOutInMs;

    private DSpotPOMCreator(InputConfiguration configuration, boolean isJUnit5) {
        this.isJUnit5 = isJUnit5;
        if (configuration != null) {
            this.absolutePathToProjectRoot = configuration.getAbsolutePathToProjectRoot();
            this.shouldUseMavenToExecuteTest = configuration.shouldUseMavenToExecuteTest();
            this.shouldExecuteTestsInParallel = configuration.shouldExecuteTestsInParallel();
            this.isDescartesMode = !configuration.isGregorMode();
            this.pitVersion = configuration.getPitVersion();
            this.descartesVersion = configuration.getDescartesVersion();
            this.additionalClasspathElements = configuration.getAdditionalClasspathElements();
            this.JVMArgs = configuration.getJVMArgs();
            this.excludedClasses = configuration.getExcludedClasses();
            this.descartesMutators = configuration.getDescartesMutators();
            this.filter = configuration.getFilter();
            this.timeOutInMs = configuration.getTimeOutInMs();
        }
    }

    public static void createNewPom(InputConfiguration configuration) {
        new DSpotPOMCreator(configuration, true)._innerCreatePom();
        new DSpotPOMCreator(configuration, false)._innerCreatePom();
    }

    // TODO
    public static String createNewPomForComputingClassPathWithParallelExecution(boolean isJUnit5, InputConfiguration configuration) {
        return new DSpotPOMCreator(configuration, isJUnit5).
                    _createNewPomForComputingClassPathWithParallelExecution();
    }

    public static void delete() {
        new DSpotPOMCreator(null, true)._delete();
        new DSpotPOMCreator(null, false)._delete();
    }

    private void _delete() {
        try {
            FileUtils.forceDelete(new File(this._getPOMName()));
        } catch (IOException ignored) {
            //ignored
        }
    }

    public String _createNewPomForComputingClassPathWithParallelExecution() {
        try {
            //Duplicate target pom

            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document document = docBuilder.parse(this.absolutePathToProjectRoot + POM_FILE);

            final Node root = findSpecificNodeFromGivenRoot(document.getFirstChild(), PROJECT);

            //Add JUnit4/5 dependencies
            addJUnitDependencies(document, root);

            // write the content into xml file
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(document);
            String newPomFilename = this.absolutePathToProjectRoot + DSpotPOMCreator.getParallelPOMName();
            final StreamResult result = new StreamResult(new File(newPomFilename));
            transformer.transform(source, result);

            return newPomFilename;
        } catch (Exception e) {
            throw new RuntimeException(e);
            }
        }

    private void addSurefirePluginConfiguration(Document document, Node root) {
        final Node build = findOrCreateGivenNode(document, root, BUILD);
        final Node plugins = findOrCreateGivenNode(document, build, PLUGINS);
        final Node surefirePlugin = findPluginByArtifactId(document, plugins, ARTIFACT_SUREFIRE_PLUGIN);
        final Node configuration = findOrCreateGivenNode(document, surefirePlugin, CONFIGURATION);

        final Element version = document.createElement("version");
        version.setTextContent("2.22.0");
        surefirePlugin.appendChild(version);
        if (this.isJUnit5) {
            final Node dependencies = findOrCreateGivenNode(document, surefirePlugin, DEPENDENCIES);
            Element dependency;
            if (!hasDependencyByArtifactId(dependencies, "junit-platform-surefire-provider")) {
                dependency = createDependency(document,
                        "org.junit.platform",
                        "junit-platform-surefire-provider",
                        "1.3.2"
                );
                dependencies.appendChild(dependency);
            }
            if (!hasDependencyByArtifactId(dependencies, "junit-jupiter-engine")) {
                dependency = createDependency(document,
                        "org.junit.jupiter",
                        "junit-jupiter-engine",
                        "5.3.2"
                );
                dependencies.appendChild(dependency);
            }
        }else {
            final Element parallel = document.createElement("parallel");
            final Element useUnlimitedThreads = document.createElement("useUnlimitedThreads");
            parallel.setTextContent("methods");
            useUnlimitedThreads.setTextContent("true");
            configuration.appendChild(parallel);
            configuration.appendChild(useUnlimitedThreads);
        }
    }

    private void addJUnitDependencies(Document document, Node root) {
        final Node dependencies = findOrCreateGivenNode(document, root, DEPENDENCIES);
        Element dependency;
        if (!hasDependencyByArtifactId(dependencies, "junit-jupiter-api")) {
            dependency = createDependency(document,
                    "org.junit.jupiter",
                    "junit-jupiter-api",
                    "5.3.2"
            );
            dependencies.appendChild(dependency);
        }
        if (!hasDependencyByArtifactId(dependencies, "junit-jupiter-engine")) {
            dependency = createDependency(document,
                    "org.junit.jupiter",
                    "junit-jupiter-engine",
                    "5.3.2"
            );
            dependencies.appendChild(dependency);
        }
        if (!hasDependencyByArtifactId(dependencies, "junit-platform-engine")) {
            dependency = createDependency(document,
                    "org.junit.platform",
                    "junit-platform-engine",
                    "1.3.2"
            );
            dependencies.appendChild(dependency);
        }
        if (!hasDependencyByArtifactId(dependencies, "junit-platform-launcher")) {
            dependency = createDependency(document,
                    "org.junit.platform",
                    "junit-platform-launcher",
                    "1.3.2"
            );
            dependencies.appendChild(dependency);
        }
        if (!hasDependencyByArtifactId(dependencies, "junit-vintage-engine")) {
            dependency = createDependency(document,
                    "org.junit.vintage",
                    "junit-vintage-engine",
                    "5.3.2"
            );
            dependencies.appendChild(dependency);
        }
        if (!hasDependencyByArtifactId(dependencies, "junit-toolbox")) {
            dependency = createDependency(document,
                    "com.googlecode.junit-toolbox",
                    "junit-toolbox",
                    "2.4"
            );
            dependencies.appendChild(dependency);
        }
    }

    private Node findPluginByArtifactId(Document document, Node pluginsNode, String artifactId) {
        Node plugin = pluginsNode.getFirstChild();
        while (plugin != null && !hasArtifactId (plugin, artifactId)) {
            plugin = plugin.getNextSibling();
        }
        return plugin;
    }

    private boolean hasArtifactId(Node node, String artifactId) {
        Node currentChild = node.getFirstChild();
        while (currentChild != null && !"artifactId".equals(currentChild.getNodeName())) {
            currentChild = currentChild.getNextSibling();
        }
        return currentChild != null && currentChild.getTextContent().equals(artifactId);
    }

    public static String getParallelPOMName() {
        return DSPOT_PARALLEL_POM_FILE + (isCurrentlyJUnit5 ? SUFFIX_JUNIT5 : "") + POM_FILE;
    }

    private Node findChildByArtifactId(Node node, String artifactId) {
        Node child = node.getFirstChild();
        while (child != null && !hasArtifactId (child, artifactId)) {
            child = child.getNextSibling();
        }
        return child;
    }

    private boolean hasDependencyByArtifactId(Node dependencies, String artifactId) {
        return findChildByArtifactId(dependencies, artifactId) != null;
    }

    public static String getPOMName() {
        return DSPOT_POM_FILE + (isCurrentlyJUnit5 ? SUFFIX_JUNIT5 : "") + POM_FILE;
    }

    private String _getPOMName() {
        return DSPOT_POM_FILE + (this.isJUnit5 ? SUFFIX_JUNIT5 : "") + POM_FILE;
    }


    private void _innerCreatePom() {
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document document = docBuilder.parse(this.absolutePathToProjectRoot + POM_FILE);

            final Node root = findSpecificNodeFromGivenRoot(document.getFirstChild(), PROJECT);

            // CONFIGURATION TO RUN INSTRUMENTED TEST
            configureForInstrumentedTests(document, root);

            if (this.shouldExecuteTestsInParallel && this.shouldUseMavenToExecuteTest) {
                //Add JUnit4/5 dependencies for parallel execution
                //Add Surefire plugin configuration for parallel execution
                addJUnitDependencies(document, root);
                addSurefirePluginConfiguration (document, root);
            }

            final Element profile = createProfile(document);

            final Node profiles = findOrCreateGivenNode(document, root, PROFILES);
            profiles.appendChild(profile);

            // write the content into xml file
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(document);
            final StreamResult result = new StreamResult(new File(this.absolutePathToProjectRoot + this._getPOMName()));
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void configureForInstrumentedTests(Document document, Node root) {
        // This will add two plugins to the build (or create a new build)

        // First, we override the test compile lifecycle in order to not compile with mvn test
        // otherwise, we would loose compilation done by dspot, more specifically
        // instrumented test methods' compilation with observations points
        final Element pluginTestCompile =
                createPlugin(document, ORG_MAVEN_PLUGINS_GROUP_ID, ARTIFACT_ID_TEST_COMPILE, "");
        final Element executions = createExecutions(document);
        final Element execution = createExecution(document, ID_VALUE_TEST_COMPILE, PHASE_VALUE_TEST_COMPILE);
        executions.appendChild(execution);
        pluginTestCompile.appendChild(executions);
        final Node build = findOrCreateGivenNode(document, root, BUILD);
        final Node plugins = findOrCreateGivenNode(document, build, PLUGINS);

        plugins.appendChild(pluginTestCompile);

        // Second, we add an additionnal classpath element: the folder that contains
        // instrumentation classes, i.e. eu.stamp_project.compare package
        final Element surefirePlugin =
                createPlugin(document, ORG_MAVEN_PLUGINS_GROUP_ID, ARTIFACT_SUREFIRE_PLUGIN, "");
        final Element configuration = document.createElement(CONFIGURATION);
        final Element additionalClasspathElements = document.createElement("additionalClasspathElements");
        final Element additionalClasspathElement = document.createElement("additionalClasspathElement");
        additionalClasspathElement.setTextContent("target/dspot/dependencies/");
        additionalClasspathElements.appendChild(additionalClasspathElement);
        configuration.appendChild(additionalClasspathElements);
        surefirePlugin.appendChild(configuration);

        plugins.appendChild(surefirePlugin);
    }

    /*
        POM XML MANAGEMENT
     */

    private Node findOrCreateGivenNode(Document document, Node root, String nodeToFind) {
        final Node existingProfiles = findSpecificNodeFromGivenRoot(root.getFirstChild(), nodeToFind);
        if (existingProfiles != null) {
            return existingProfiles;
        } else {
            final Element profiles = document.createElement(nodeToFind);
            root.appendChild(profiles);
            return profiles;
        }
    }

    private Node findSpecificNodeFromGivenRoot(Node startingPoint, String nodeName) {
        Node currentChild = startingPoint;
        while (currentChild != null && !nodeName.equals(currentChild.getNodeName())) {
            currentChild = currentChild.getNextSibling();
        }
        return currentChild;
    }

    /*
        CREATE PROFILE FOR THE POM WITH ALL ITS CONTENT
     */

    private static final String PROFILE = "profile";

    private static final String ID = "id";

    public static final String PROFILE_ID = "id-descartes-for-dspot";

    private Element createProfile(Document document) {
        final Element profile = document.createElement(PROFILE);
        final Element id = document.createElement(ID);

        id.setTextContent(PROFILE_ID);
        profile.appendChild(id);
        final Element build = createBuild(document);
        profile.appendChild(build);

        return profile;
    }

    private static final String BUILD = "build";

    private static final String PLUGINS = "plugins";

    private static final String ID_VALUE_TEST_COMPILE = "default-testCompile";

    private static final String PHASE_VALUE_TEST_COMPILE = "none";

    private Element createBuild(Document document) {
        final Element build = document.createElement(BUILD);
        final Element plugins = document.createElement(PLUGINS);

        // PIT PLUGIN
        final Element pluginPit = createPlugin(document, GROUP_ID_PIT, ARTIFACT_ID_PIT, this.pitVersion);
        final Element configuration = createConfiguration(document);
        pluginPit.appendChild(configuration);

        if (this.isDescartesMode || this.isJUnit5) {
            final Element dependencies = createDependencies(document);
            pluginPit.appendChild(dependencies);
        }
        plugins.appendChild(pluginPit);

        build.appendChild(plugins);

        return build;
    }

    private static final String PLUGIN = "plugin";

    private static final String GROUP_ID = "groupId";

    private static final String ARTIFACT_ID = "artifactId";

    private static final String VERSION = "version";

    private Element createPlugin(Document document,
                                 String groupIdValue,
                                 String artifactIdValue,
                                 String versionValue) {
        final Element plugin = document.createElement(PLUGIN);

        final Element groupId = document.createElement(GROUP_ID);
        groupId.setTextContent(groupIdValue);
        plugin.appendChild(groupId);

        final Element artifactId = document.createElement(ARTIFACT_ID);
        artifactId.setTextContent(artifactIdValue);
        plugin.appendChild(artifactId);

        if (!versionValue.isEmpty()) {
            final Element version = document.createElement(VERSION);
            version.setTextContent(versionValue);
            plugin.appendChild(version);
        }

        return plugin;
    }

    private static final String EXECUTIONS = "executions";

    private Element createExecutions(Document document) {
        return document.createElement(EXECUTIONS);
    }

    private static final String EXECUTION = "execution";

    private static final String PHASE = "phase";

    private Element createExecution(Document document, String idValue, String phaseValue) {
        final Element execution = document.createElement(EXECUTION);

        final Element id = document.createElement(ID);
        id.setTextContent(idValue);
        execution.appendChild(id);

        final Element phase = document.createElement(PHASE);
        phase.setTextContent(phaseValue);
        execution.appendChild(phase);

        return execution;
    }

    private static final String DEPENDENCIES = "dependencies";

    private static final String DEPENDENCY = "dependency";

    private static final String JUNIT5_PIT_PLUGIN = "pitest-junit5-plugin";

    private static final String JUNIT5_PIT_PLUGIN_VERSION = "0.7";

    private Element createDependencies(Document document) {
        final Element dependencies = document.createElement(DEPENDENCIES);

        if (this.isDescartesMode) {
            final Element dependency = createDependency(document,
                    GROUP_ID_DESCARTES,
                    ARTIFACT_ID_DESCARTES,
                    this.descartesVersion
            );
            dependencies.appendChild(dependency);
        }

        if (this.isJUnit5) {
            final Element dependency = createDependency(
                    document,
                    GROUP_ID_PIT,
                    JUNIT5_PIT_PLUGIN,
                    JUNIT5_PIT_PLUGIN_VERSION
            );
            dependencies.appendChild(dependency);
        }

        return dependencies;
    }

    private Element createDependency(Document document,
                                     String groupIdValue,
                                     String artifactIdValue,
                                     String versionValue) {
        final Element dependency = document.createElement(DEPENDENCY);

        final Element groupId = document.createElement(GROUP_ID);
        groupId.setTextContent(groupIdValue);
        dependency.appendChild(groupId);

        final Element artifactId = document.createElement(ARTIFACT_ID);
        artifactId.setTextContent(artifactIdValue);
        dependency.appendChild(artifactId);

        final Element version = document.createElement(VERSION);
        version.setTextContent(versionValue);
        dependency.appendChild(version);
        return dependency;
    }

    private static final String CONFIGURATION = "configuration";

    private static final String MUTATION_ENGINE = "mutationEngine";

    private static final String OUTPUT_FORMATS = "outputFormats";

    private static final String JVM_ARGS = "jvmArgs";

    private static final String EXCLUDED_TEST_CLASSES = "excludedTestClasses";

    private static final String MUTATORS = "mutators";

    private static final String GREGOR_MUTATORS = "ALL";

    private static final String TARGET_CLASSES = "targetClasses";

    private static final String REPORT_DIRECTORY = "reportsDirectory";

    public static final String REPORT_DIRECTORY_VALUE = "target/pit-reports/";

    private static final String TIME_OUT = "timeoutConstant";

    private static final String ADDITIONAL_CLASSPATH_ELEMENTS = "additionalClasspathElements";

    private Element createConfiguration(Document document) {
        final Element configuration = document.createElement(CONFIGURATION);

        final Element mutationEngine = document.createElement(MUTATION_ENGINE);
        mutationEngine.setTextContent(this.isDescartesMode ? MUTATION_ENGINE_DESCARTES : MUTATION_ENGINE_GREGOR);
        configuration.appendChild(mutationEngine);

        final Element outputFormats = document.createElement(OUTPUT_FORMATS);
        appendValuesToGivenNode(document, outputFormats, DSpotPOMCreator.outputFormats);
        configuration.appendChild(outputFormats);

        if (this.filter != null &&
                !this.filter.isEmpty()) {
            final Element targetClasses = document.createElement(TARGET_CLASSES);
            targetClasses.setTextContent(this.filter);
            configuration.appendChild(targetClasses);
        }

        final Element reportsDirectory = document.createElement(REPORT_DIRECTORY);
        reportsDirectory.setTextContent(REPORT_DIRECTORY_VALUE);
        configuration.appendChild(reportsDirectory);

        final Element timeOut = document.createElement(TIME_OUT);
        timeOut.setTextContent(String.valueOf(this.timeOutInMs));
        configuration.appendChild(timeOut);

        if (!this.additionalClasspathElements.isEmpty()) {
            final Element additionalClasspathElements = document.createElement(ADDITIONAL_CLASSPATH_ELEMENTS);
            appendValuesToGivenNode(document, additionalClasspathElements, this.additionalClasspathElements.split(","));
            configuration.appendChild(additionalClasspathElements);
        }

        if (!this.JVMArgs.isEmpty()) {
            final Element jvmArgs = document.createElement(JVM_ARGS);
            appendValuesToGivenNode(document, jvmArgs, this.JVMArgs.split(","));
            configuration.appendChild(jvmArgs);
        }

        if (!this.excludedClasses.isEmpty()) {
            final Element excludedTestClasses = document.createElement(EXCLUDED_TEST_CLASSES);
            appendValuesToGivenNode(document, excludedTestClasses, this.excludedClasses.split(","));
            configuration.appendChild(excludedTestClasses);
        }

        if (!this.descartesMutators.isEmpty() || !this.isDescartesMode) {
            final Element mutators = document.createElement(MUTATORS);
            if (!this.descartesMutators.isEmpty() && this.isDescartesMode) {
                appendValuesToGivenNode(document, mutators, this.descartesMutators.split(","));
            } else {
                appendValuesToGivenNode(document, mutators, GREGOR_MUTATORS);
            }
            configuration.appendChild(mutators);
        }

        return configuration;
    }


    /*
        UTILS
     */

    private void appendValuesToGivenNode(Document document, Element nodeParent, String... values) {
        Arrays.stream(values)
                .map(value -> createValue(document, value))
                .forEach(nodeParent::appendChild);
    }

    private static final String VALUE = "value";

    private Element createValue(Document document, String stringValue) {
        final Element value = document.createElement(VALUE);
        value.setTextContent(stringValue);
        return value;
    }

}
