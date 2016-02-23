package fr.inria.diversify.coverage.graph;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 23/06/15
 * Time: 10:16
 */
public class Node {
    String name;
    Set<Node> call;

    public Node(String name) {
        this.name = name;
        call = new HashSet<>();
    }

    public void addCall(Node node) {
        call.add(node);
    }

    public String getName() {
        return name;
    }


    public Set<String> callMinus(Node node) throws Exception {
        if(!name.equals(node.getName())) {
            throw new Exception("not the same node");
        }
        Set<String> set = call.stream()
                .map(n -> n.name)
                .collect(Collectors.toSet());

        set.removeAll(node.call.stream()
                .map(n -> n.name)
                .collect(Collectors.toSet()));

        return set.stream()
                .map(n -> name + " -> " + n)
                .collect(Collectors.toSet());
    }

    public Set<String> getEdges() {
        return call.stream()
                .map(node -> name + " -> " + node.getName())
                .collect(Collectors.toSet());
    }

    public void toDot(Writer writer) throws IOException {
        writer.append(hashCode() + " [label=\""+ name + "\"];\n");
        for (Node node : call) {
            writer.append(hashCode() + " -> " + node.hashCode() + ";\n");
        }
    }
}
