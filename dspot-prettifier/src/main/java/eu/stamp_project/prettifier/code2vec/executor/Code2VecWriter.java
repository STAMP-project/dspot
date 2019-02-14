package eu.stamp_project.prettifier.code2vec.executor;

import eu.stamp_project.utils.DSpotUtils;
import spoon.reflect.declaration.CtMethod;

import java.io.FileWriter;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Code2VecWriter {

    public static final String FILENAME = "Input.java";

    private final String root;

    /**
     * @param root the path to the root folder of Code2Vec.
     */
    public Code2VecWriter(String root) {
        this.root = DSpotUtils.shouldAddSeparator.apply(root);
    }

    /**
     * write the given test method in the Input.java file.
     * @param testMethod
     */
    public void writeCtMethodToInputFile(CtMethod<?> testMethod) {
        try {
            FileWriter writer = new FileWriter(this.root + FILENAME, false);
            writer.write(testMethod.toString());
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
