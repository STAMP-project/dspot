package fr.inria.diversify.logger;

import java.io.PrintWriter;
import java.util.*;

/**
 * User: Simon
 * Date: 16/06/15
 * Time: 13:57
 */
public class PathBuilder {
    protected Stack<StringBuilder> currentPaths;
    protected Stack<String> previousBranchs;
    protected boolean fullPath;

    //string : method id
    //Set<String> set of path
    protected Map<String, Set<String>> allPath;

    public PathBuilder(boolean fullPath) {
        this.fullPath = fullPath;
        currentPaths = new Stack<StringBuilder>();
        previousBranchs = new Stack<String>();
        allPath = new HashMap<String, Set<String>>();
    }

    public void addbranch(String id) {
        if (fullPath || previousBranchs.size() == 0 || previousBranchs.peek() != id) {
            currentPaths.peek().append(KeyWord.simpleSeparator);
            currentPaths.peek().append(id);
        }
        previousBranchs.pop();
        previousBranchs.push(id);
    }

    public void newPath() {
        previousBranchs.push(null);
        currentPaths.push(new StringBuilder());
    }

    public void printPath(String id, int deep, PrintWriter writer) {
        String path = deep + currentPaths.pop().toString();
        previousBranchs.pop();

        if (!allPath.containsKey(id)) {
            allPath.put(id, new HashSet<String>());
        }
        Set<String> paths = allPath.get(id);

        if (!paths.contains(path)) {
            paths.add(path);

            writer.append(KeyWord.endLine);
            writer.append("\n");
            writer.append(KeyWord.branchObservation);
            writer.append(KeyWord.simpleSeparator);
            writer.append(id);
            writer.append(KeyWord.simpleSeparator);
            writer.append(path);
        }
    }

    public void clear() {
        allPath.clear();
    }
}
