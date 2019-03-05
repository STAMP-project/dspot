package eu.stamp_project.prettifier.code2vec;

import eu.stamp_project.prettifier.options.InputConfiguration;
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

    /**
     * write the given test method in the Input.java file.
     * @param testMethod
     */
    public void writeCtMethodToInputFile(CtMethod<?> testMethod) {
        try {
            FileWriter writer = new FileWriter(InputConfiguration.get().getPathToRootOfCode2Vec() + FILENAME, false);
            writer.write(testMethod.toString());
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
