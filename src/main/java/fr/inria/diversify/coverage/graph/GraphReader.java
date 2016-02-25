package fr.inria.diversify.coverage.graph;



import fr.inria.diversify.profiling.logger.KeyWord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * User: Simon
 * Date: 23/06/15
 * Time: 10:51
 */
public class GraphReader {
    Map<String, Graph> graphByTest;
    Map<String, String> idToMethod;
    String directory;

    public GraphReader(String directory) {
        this.directory = directory;
        graphByTest= new HashMap<>();

    }

    public List<Graph> load() throws IOException {
        loadInfo();
        File dir = new File(directory);

        for(File file : dir.listFiles()) {
            if(file.isFile() && file.getName().startsWith("log")) {
                parseFile(file);
            }
        }
        return new ArrayList<>(graphByTest.values());
    }

    protected void parseFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        List<String> methodsCall = new LinkedList();
        String currentTest = null;


        String line = br.readLine();
        String logEntry = "";
        while (line != null) {
            line = br.readLine();
            logEntry = logEntry + line;
            if (logEntry.endsWith("$$")) {
                logEntry = logEntry.substring(0, logEntry.length() - 2);
                String[] split = logEntry.split(";");
                switch (split[0]) {
                    case KeyWord.testStartObservation:
                        currentTest = split[1];
                        break;
                    case KeyWord.testEndObservation:
                        if (currentTest != null) {
                            buildGraph(currentTest, methodsCall);
                        }
                        currentTest = null;
                        methodsCall.clear();
                        break;
                    case KeyWord.methodCallObservation:
                        methodsCall.add(logEntry);
                        break;
                    default:
                        break;
                }
                logEntry = "";
            }
            if (logEntry.startsWith(KeyWord.testEndObservation) && currentTest != null) {
                buildGraph(currentTest, methodsCall);
            }
        }

    }

    protected void buildGraph(String currentTest, List<String> methodsCall) {
        Graph graph = getOrBuildGraph(currentTest);
        Stack<String> stack = new Stack<>();

        for(String methodCall : methodsCall) {
            String[] split = methodCall.split(KeyWord.simpleSeparator);
            int deep = Integer.parseInt(split[1]);
            String name = idToMethod.get(split[2]);

             while(stack.size() >= deep) {
                stack.pop();
            }

            if(stack.isEmpty()) {
                graph.addNode(name);
            } else {
                graph.addCall(stack.peek(), name);
            }
            stack.push(name);
        }
    }
    protected Graph getOrBuildGraph(String name) {
        if (!graphByTest.containsKey(name)) {
            graphByTest.put(name, new Graph(name));
        }
        return graphByTest.get(name);
    }

    protected void loadInfo() throws IOException {
        idToMethod = new HashMap<>();
        File infoFile = new File(directory + "/info");
        BufferedReader br = new BufferedReader(new FileReader(infoFile));

        String line = br.readLine();
        while (line != null) {
            if(line.startsWith("id")) {
                String[] split = line.split(";");
                idToMethod.put(split[1], split[2]);
            }
            line = br.readLine();
        }
    }

}
