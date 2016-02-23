package fr.inria.diversify.coverage.graph;


import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Simon
 * Date: 23/06/15
 * Time: 10:16
 */
public class Graph {
    String name;
    Map<String, Node> nodes;

    public Graph(String name) {
        this.name = name;
        nodes = new HashMap<>();
    }

    public void addCall(String caller, String call) {
        getNodeOrBuild(caller).addCall(getNodeOrBuild(call));
    }

    protected Node getNodeOrBuild(String name) {
        if(!nodes.containsKey(name)) {
            nodes.put(name, new Node(name));
        }
        return nodes.get(name);
    }

    public void addNode(String name) {
        getNodeOrBuild(name);
    }

    public void toDot(String fileName) throws IOException {
        Writer writer = new FileWriter(fileName);

        writer.append("digraph g {\n");
        for (Node node : nodes.values()) {
            node.toDot(writer);
        }
        writer.append("\n}");
        writer.close();
    }

    public String getName() {
        return name;
    }
}
