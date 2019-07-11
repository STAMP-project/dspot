package eu.stamp_project.prettifier.context2name;

import eu.stamp_project.prettifier.options.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

import java.io.FileWriter;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Context2NameWriter {

    public static final String FILENAME = "Input.java";

    private static final Logger LOGGER = LoggerFactory.getLogger(Context2NameWriter.class);

    /**
     * write the given test method in the Input.java file.
     * @param testMethod
     */
    public void writeCtMethodToInputFile(CtMethod<?> testMethod) {
        try {
            final String fileName = InputConfiguration.get().getPathToRootOfContext2Name() + FILENAME;
            LOGGER.info("Writing content of {} into {}.", testMethod.getSimpleName(), fileName);
            FileWriter writer = new FileWriter(fileName, false);
            writer.write(testMethod.toString());
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
