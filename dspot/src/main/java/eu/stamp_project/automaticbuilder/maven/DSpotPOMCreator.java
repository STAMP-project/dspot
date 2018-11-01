package eu.stamp_project.automaticbuilder.maven;

import eu.stamp_project.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import spoon.reflect.declaration.CtPackage;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Arrays;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/10/18
 *
 * This class generate a copy of the original pom, with a new profile to run pit.
 * The profile is configured to fit the configuration of the Command and the properties file given to DSpot.
 *
 * There is no more modification of the original pom.
 *
 */
public class DSpotPOMCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSpotPOMCreator.class);

    private static final String GROUP_ID_PIT = "org.pitest";

    private static final String ARTIFACT_ID_PIT = "pitest-maven";

    private static final String MUTATION_ENGINE_GREGOR = "gregor";

    private static final String GROUP_ID_DESCARTES = "eu.stamp-project";

    private static final String ARTIFACT_ID_DESCARTES = "descartes";

    private static final String MUTATION_ENGINE_DESCARTES = "descartes";

    private static final String[] outputFormats = new String[]{"CSV"};

    private static final String POM_FILE = "pom.xml";

    private static final String PROJECT = "project";

    private static final String PROFILES = "profiles";

    public static final String DSPOT_POM_FILE = ".dspot_pom.xml";

    public static void createNewPom() {
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document document = docBuilder.parse(InputConfiguration.get().getAbsolutePathToProjectRoot() + POM_FILE);

            final Node root = findSpecificNodeFromGivenRoot(document.getFirstChild(), PROJECT);

            final Element profile = createProfile(document);

            final Node profiles = findOrCreateProfiles(document, root);
            profiles.appendChild(profile);

            // write the content into xml file
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(document);
            final StreamResult result = new StreamResult(new File(InputConfiguration.get().getAbsolutePathToProjectRoot() + DSPOT_POM_FILE));
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
        POM XML MANAGEMENT
     */

    private static Node findOrCreateProfiles(Document document, Node root) {
        final Node existingProfiles = findSpecificNodeFromGivenRoot(root, PROFILES);
        if (existingProfiles != null) {
            return existingProfiles;
        } else {
            final Element profiles = document.createElement(PROFILES);
            root.appendChild(profiles);
            return profiles;
        }
    }

    private static Node findSpecificNodeFromGivenRoot(Node startingPoint, String nodeName) {
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

    private static Element createProfile(Document document) {
        final Element profile = document.createElement(PROFILE);
        final Element id = document.createElement(ID);

        id.setTextContent(PROFILE_ID);
        profile.appendChild(id);
        profile.appendChild(createBuild(document));

        return profile;
    }

    private static final String BUILD = "build";

    private static final String PLUGINS = "plugins";

    private static Element createBuild(Document document) {
        final Element build = document.createElement(BUILD);
        final Element plugins = document.createElement(PLUGINS);

        final Element plugin = createPlugin(document);
        plugins.appendChild(plugin);

        build.appendChild(plugins);

        return build;
    }

    private static final String PLUGIN = "plugin";

    private static final String GROUP_ID = "groupId";

    private static final String ARTIFACT_ID = "artifactId";

    private static final String VERSION = "version";

    private static Element createPlugin(Document document) {
        final Element plugin = document.createElement(PLUGIN);

        final Element groupId = document.createElement(GROUP_ID);
        groupId.setTextContent(GROUP_ID_PIT);
        plugin.appendChild(groupId);

        final Element artifactId = document.createElement(ARTIFACT_ID);
        artifactId.setTextContent(ARTIFACT_ID_PIT);
        plugin.appendChild(artifactId);

        final Element version = document.createElement(VERSION);
        version.setTextContent(InputConfiguration.get().getPitVersion());
        plugin.appendChild(version);

        final Element configuration = createConfiguration(document);
        plugin.appendChild(configuration);

        final Element dependencies = createDependencies(document);
        plugin.appendChild(dependencies);

        return plugin;
    }

    private static final String DEPENDENCIES = "dependencies";

    private static final String DEPENDENCY = "dependency";

    private static Element createDependencies(Document document) {
        final Element dependencies = document.createElement(DEPENDENCIES);

        final Element dependency = document.createElement(DEPENDENCY);

        final Element groupId = document.createElement(GROUP_ID);
        groupId.setTextContent(GROUP_ID_DESCARTES);
        dependency.appendChild(groupId);

        final Element artifactId = document.createElement(ARTIFACT_ID);
        artifactId.setTextContent(ARTIFACT_ID_DESCARTES);
        dependency.appendChild(artifactId);

        final Element version = document.createElement(VERSION);
        version.setTextContent(InputConfiguration.get().getDescartesVersion());
        dependency.appendChild(version);

        dependencies.appendChild(dependency);

        return dependencies;
    }

    private static final String CONFIGURATION = "configuration";

    private static final String MUTATION_ENGINE = "mutationEngine";

    private static final String OUTPUT_FORMATS = "outputFormats";

    private static final String JVM_ARGS = "jvmArgs";

    private static final String EXCLUDED_TEST_CLASSES = "excludedTestClasses";

    private static final String MUTATORS = "mutators";

    private static final String TARGET_CLASSES = "targetClasses";

    private static final String REPORT_DIRECTORY = "reportsDirectory";

    public static final String REPORT_DIRECTORY_VALUE = "target/pit-reports";

    private static final String TIME_OUT = "timeoutConstant";

    private static final String ADDITIONAL_CLASSPATH_ELEMENTS = "additionalClasspathElements";

    private static Element createConfiguration(Document document) {
        final Element configuration = document.createElement(CONFIGURATION);

        final Element mutationEngine = document.createElement(MUTATION_ENGINE);
        mutationEngine.setTextContent(InputConfiguration.get().isDescartesMode() ? MUTATION_ENGINE_DESCARTES : MUTATION_ENGINE_GREGOR);
        configuration.appendChild(mutationEngine);

        final Element outputFormats = document.createElement(OUTPUT_FORMATS);
        appendValuesToGivenNode(document, outputFormats, DSpotPOMCreator.outputFormats);
        configuration.appendChild(outputFormats);

        final Element targetClasses = document.createElement(TARGET_CLASSES);
        targetClasses.setTextContent(getFilter());
        configuration.appendChild(targetClasses);

        final Element reportsDirectory = document.createElement(REPORT_DIRECTORY);
        reportsDirectory.setTextContent(REPORT_DIRECTORY_VALUE);
        configuration.appendChild(reportsDirectory);

        final Element timeOut = document.createElement(TIME_OUT);
        timeOut.setTextContent(String.valueOf(InputConfiguration.get().getTimeOutInMs()));
        configuration.appendChild(timeOut);

        if (!InputConfiguration.get().getAdditionalClasspathElements().isEmpty()) {
            final Element additionalClasspathElements = document.createElement(ADDITIONAL_CLASSPATH_ELEMENTS );
            appendValuesToGivenNode(document, additionalClasspathElements, InputConfiguration.get().getAdditionalClasspathElements().split(","));
            configuration.appendChild(additionalClasspathElements);
        }

        if (!InputConfiguration.get().getJVMArgs().isEmpty()) {
            final Element jvmArgs = document.createElement(JVM_ARGS);
            appendValuesToGivenNode(document, jvmArgs, InputConfiguration.get().getJVMArgs().split(","));
            configuration.appendChild(jvmArgs);
        }

        if (!InputConfiguration.get().getExcludedClasses().isEmpty()) {
            final Element excludedTestClasses = document.createElement(EXCLUDED_TEST_CLASSES);
            appendValuesToGivenNode(document, excludedTestClasses, InputConfiguration.get().getExcludedClasses().split(","));
            configuration.appendChild(excludedTestClasses);
        }

        if (!InputConfiguration.get().getDescartesMutators().isEmpty()) {
            final Element mutators = document.createElement(MUTATORS);
            appendValuesToGivenNode(document, mutators, InputConfiguration.get().getDescartesMutators().split(","));
            configuration.appendChild(mutators);
        }

        return configuration;
    }


    /*
        UTILS
     */

    private static final String MESSAGE_WARN_PIT_NO_FILTER = "You gave an empty filter. To use PIT, it is recommend to specify a filter, at least, the top package of your program, otherwise, PIT may take a long time or could not be run.";

    private static String getFilter() {
        if (InputConfiguration.get().getFilter().isEmpty()) {
            LOGGER.warn(MESSAGE_WARN_PIT_NO_FILTER);
            LOGGER.warn("Trying to compute the top package of the project...");
            CtPackage currentPackage = InputConfiguration.get().getFactory().Package().getRootPackage();
            while (currentPackage.getTypes().isEmpty()) {
                currentPackage = (CtPackage) currentPackage.getPackages().toArray()[0];
            }
            InputConfiguration.get().setFilter(currentPackage.getQualifiedName() + ".*");
        }
        return InputConfiguration.get().getFilter();
    }

    private static void appendValuesToGivenNode(Document document, Element nodeParent, String... values) {
        Arrays.stream(values)
                .map(value -> createValue(document, value))
                .forEach(nodeParent::appendChild);
    }

    private static final String VALUE = "value";

    private static Element createValue(Document document, String stringValue) {
        final Element value = document.createElement(VALUE);
        value.setTextContent(stringValue);
        return value;
    }

}
