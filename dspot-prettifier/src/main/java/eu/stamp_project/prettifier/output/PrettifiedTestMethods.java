package eu.stamp_project.prettifier.output;

import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/02/19
 */
public class PrettifiedTestMethods {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrettifiedTestMethods.class);

    private String outputDirectory;

    public PrettifiedTestMethods(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void output(
            CtType<?> testClass,
            List<CtMethod<?>> prettifiedAmplifiedTestMethods) {
        testClass.getMethods().stream()
                .filter(TestFramework.get()::isTest)
                .forEach(testClass::removeMethod);
        prettifiedAmplifiedTestMethods.forEach(testClass::addMethod);
        DSpotUtils.printCtTypeToGivenDirectory(testClass, new File(outputDirectory), true);
        LOGGER.info("Print {} in {}", testClass.getQualifiedName(), outputDirectory);
    }

}
