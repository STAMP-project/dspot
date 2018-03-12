package fr.inria.diversify.mutant.descartes;

import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/03/17
 */
public class DescartesChecker {

    public static boolean shouldInjectDescartes(String pathToPom) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(pathToPom);
            final Node root = doc.getFirstChild();
            return checkDependency(root) || checkPlugin(root);
        } catch (ParserConfigurationException | IOException | SAXException pce) {
            throw new RuntimeException(pce);
        }
    }

    static Node getNodeNamedFrom(Node startNode, String name) {
        Node currentNode = startNode.getFirstChild();
        while (currentNode != null && !name.equals(currentNode.getNodeName())) {
            currentNode = currentNode.getNextSibling();
        }
        return currentNode;
    }

    private static boolean checkDependency(Node root) {
        final Node dependencies = getNodeNamedFrom(root, "dependencies");
        if (dependencies == null) {
            return true;
        }
        final List<String> expectedValues = new ArrayList<>(Arrays.asList("org.pitest", "pitest-maven", PitMutantScoreSelector.pitVersion));
        Optional<Node> checkDependency = getAllChildNodeNamedFrom(dependencies, "dependency").stream()
                .filter(dependency ->
                        checkThatHasTheGoodDependency(dependency, expectedValues)
                )
                .findFirst();
        return !checkDependency.isPresent();
    }

    private static List<Node> getAllChildNodeNamedFrom(Node parent, String name) {
        Node currentNode = parent.getFirstChild();
        if (currentNode == null) {
            return Collections.emptyList();
        }
        List<Node> children = new ArrayList<>();
        do {
            if (currentNode.getNodeName().equals(name)) {
                children.add(currentNode);
            }
            currentNode = currentNode.getNextSibling();
        } while (currentNode != null);
        return children;
    }

    // must be a <dependency> nodes
    private static boolean checkThatHasTheGoodDependency(Node dependency, List<String> expectedTextContent) {
        final List<String> expectedTags = new ArrayList<>(Arrays.asList("groupId", "artifactId", "version"));
        if (expectedTextContent.size() != expectedTags.size()) {
            return false;
        }
        Node currentNode = dependency.getFirstChild();
        while (currentNode != null && !(expectedTags.isEmpty())
                && !(expectedTextContent.isEmpty())) {
            if (expectedTags.contains(currentNode.getNodeName())
                    && expectedTextContent.contains(currentNode.getTextContent())) {
                expectedTags.remove(currentNode.getNodeName());
                expectedTextContent.remove(currentNode.getTextContent());
            }
            currentNode = currentNode.getNextSibling();
        }
        return expectedTags.isEmpty() && expectedTextContent.isEmpty();
    }

    private static Node getPlugin(Node root) {
        final Node build = getNodeNamedFrom(root, "build");
        if (build == null) {
            return null;
        }
        final Node plugins = getNodeNamedFrom(build, "plugins");
        if (plugins == null) {
            return null;
        }
        final List<String> expectedValues = new ArrayList<>(Arrays.asList("org.pitest", "pitest-maven", PitMutantScoreSelector.pitVersion));
        Optional<Node> checkDependency = getChildThatHasTheGoodDependency(plugins, expectedValues, "plugin");
        if (!checkDependency.isPresent()) {
            return null;
        }
        return checkDependency.get();
    }

    private static Optional<Node> getChildThatHasTheGoodDependency(Node parent, List<String> expectedValues,
                                                                   String targetedName) {
        return getAllChildNodeNamedFrom(parent, targetedName).stream()
                    .filter(plugin ->
                            checkThatHasTheGoodDependency(plugin, expectedValues)
                    )
                .findFirst();
    }

    private static boolean checkPlugin(Node root) {
        // 2 looking for the plugin

        final Node plugin = getPlugin(root);
        if (plugin == null) {
            return true;
        }

        final Node configuration = getNodeNamedFrom(plugin, "configuration");
        if (configuration == null) {
            return true;
        }
        final Node mutationEngine = getNodeNamedFrom(configuration, "mutationEngine");
        if (mutationEngine == null || !"descartes".equals(mutationEngine.getTextContent())) {
            return true;
        }

        final Node dependencies1 = getNodeNamedFrom(plugin, "dependencies");
        if (dependencies1 == null) {
            return true;
        }

        final List<String> expectedValues = new ArrayList<>(Arrays.asList("fr.inria.stamp", "descartes", PitMutantScoreSelector.descartesVersion));
        return !getChildThatHasTheGoodDependency(dependencies1, expectedValues, "dependency").isPresent();
    }

}
