package eu.stamp_project.prettifier.code2vec;

import eu.stamp_project.prettifier.Main;
import eu.stamp_project.prettifier.options.InputConfiguration;
import eu.stamp_project.utils.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

import java.io.FileWriter;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Code2VecWriter {

    public static final String FILENAME = "Input.java";

    private static final Logger LOGGER = LoggerFactory.getLogger(Code2VecWriter.class);

    private String pathToRootOfCode2Vec;

    public Code2VecWriter(String pathToRootOfCove2Vec) {
        this.pathToRootOfCode2Vec = DSpotUtils.shouldAddSeparator.apply(pathToRootOfCove2Vec);
    }

    /**
     * write the given test method in the Input.java file.
     * @param testMethod method to output in the file
     */
    public void writeCtMethodToInputFile(CtMethod<?> testMethod) {
        try {
            final String fileName =  this.pathToRootOfCode2Vec + FILENAME;
            LOGGER.info("Writing content of {} into {}.", testMethod.getSimpleName(), fileName);
            FileWriter writer = new FileWriter(fileName, false);
            writer.write(testMethod.toString());
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
