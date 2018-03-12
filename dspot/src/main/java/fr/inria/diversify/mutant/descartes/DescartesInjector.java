package fr.inria.diversify.mutant.descartes;

import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/03/17
 */
public class DescartesInjector {

    private static Node getNodeNamedFromOrBuildIfDoesnotExist(Document doc, Node startNode, String name) {
        Node currentNode = DescartesChecker.getNodeNamedFrom(startNode, name);
        if (currentNode == null) {
            currentNode = doc.createElement(name);
            startNode.appendChild(currentNode);
        }
        return currentNode;
    }

    private static List<Node> buildNodesDependency(Document doc, String groupIdValue,
                                                   String artifactIdValue, String versionValue) {
        final Element groupId = doc.createElement("groupId");
        groupId.setTextContent(groupIdValue);
        final Element artifactId = doc.createElement("artifactId");
        artifactId.setTextContent(artifactIdValue);
        final Element version = doc.createElement("version");
        version.setTextContent(versionValue);
        return Arrays.asList(groupId, artifactId, version);
    }

    private static Node buildDependencyToPitTest(Document doc) {
        final Element dependency = doc.createElement("dependency");
        buildNodesDependency(doc, "org.pitest", "pitest-maven", PitMutantScoreSelector.pitVersion).forEach(dependency::appendChild);
        return dependency;
    }

    private static Node buildPlugin(Document doc) {
        final Element plugin = doc.createElement("plugin");
        buildNodesDependency(doc, "org.pitest", "pitest-maven", PitMutantScoreSelector.pitVersion).forEach(plugin::appendChild);
        plugin.appendChild(buildConfiguration(doc));
        plugin.appendChild(buildDependencies(doc));
        return plugin;
    }

    private static Node buildDependency(Document doc) {
        final Element dependency = doc.createElement("dependency");
        buildNodesDependency(doc, "fr.inria.stamp", "descartes", PitMutantScoreSelector.descartesVersion).forEach(dependency::appendChild);
        return dependency;
    }

    private static Node buildDependencies(Document doc) {
        final Element dependencies = doc.createElement("dependencies");
        dependencies.appendChild(buildDependency(doc));
        return dependencies;
    }

    private static Node buildMutators(Document doc, String name) {
        final Element mutator = doc.createElement("mutator");
        mutator.setTextContent(name);
        return mutator;
    }

    private static List<Node> buildListOfMutators(Document doc) {
        return Arrays.stream(MavenPitCommandAndOptions.VALUE_MUTATORS_DESCARTES)
                .collect(ArrayList<Node>::new,
                        (nodes, name) -> nodes.add(buildMutators(doc, name)),
                        ArrayList<Node>::addAll
                );
    }

    private static Node buildConfiguration(Document doc) {
        final Element configuration = doc.createElement("configuration");
        final Element mutationEngine = doc.createElement("mutationEngine");
        mutationEngine.setTextContent("descartes");
        configuration.appendChild(mutationEngine);
        final Element mutators = doc.createElement("mutators");
        buildListOfMutators(doc).forEach(mutators::appendChild);
        configuration.appendChild(mutators);
        return configuration;
    }

    public static void injectDescartesIntoPom(String pathToPom) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(pathToPom);

            final Node root = doc.getFirstChild();
            final Node dependencies = getNodeNamedFromOrBuildIfDoesnotExist(doc, root,
                    "dependencies");
            dependencies.appendChild(buildDependencyToPitTest(doc));
            dependencies.appendChild(buildDependency(doc));

            Node build = getNodeNamedFromOrBuildIfDoesnotExist(doc, root,
                    "build");
            getNodeNamedFromOrBuildIfDoesnotExist(doc, build,"plugins")
                    .appendChild(buildPlugin(doc));

            getNodeNamedFromOrBuildIfDoesnotExist(doc, root, "repositories")
                    .appendChild(buildRepository(doc));

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(pathToPom));
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException | IOException | SAXException pce) {
            throw new RuntimeException(pce);
        }
    }

    private static final String ID_REPOSITORY = "stamp-maven-repository-mvn-repo";

    private static final String URL_REPOSITORY = "https://stamp-project.github.io/stamp-maven-repository";

    private static Node buildRepository(Document doc) {
        final Element repository = doc.createElement("repository");
        buildListOfChildrenRepository(doc).forEach(repository::appendChild);
        return repository;
    }

    private static List<Node> buildListOfChildrenRepository(Document doc) {
        final Element id = doc.createElement("id");
        id.setTextContent(ID_REPOSITORY);
        final Element url = doc.createElement("url");
        url.setTextContent(URL_REPOSITORY);
        final Element snapshots = doc.createElement("snapshots");
        final Element enabled = doc.createElement("enabled");
        enabled.setTextContent("true");
        final Element updatePolicy = doc.createElement("updatePolicy");
        updatePolicy.setTextContent("always");
        snapshots.appendChild(enabled);
        snapshots.appendChild(updatePolicy);
        return Arrays.asList(id, url, snapshots);
    }
}
