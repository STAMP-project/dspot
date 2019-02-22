package eu.stamp_project.prettifier;

import eu.stamp_project.prettifier.code2vec.Code2VecExecutor;
import eu.stamp_project.prettifier.code2vec.Code2VecParser;
import eu.stamp_project.prettifier.code2vec.Code2VecWriter;
import eu.stamp_project.prettifier.minimization.GeneralMinimizer;
import eu.stamp_project.prettifier.options.InputConfiguration;
import eu.stamp_project.prettifier.options.JSAPOptions;
import eu.stamp_project.prettifier.output.PrettifiedTestMethods;
import eu.stamp_project.test_framework.TestFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /*
        Apply the following algorithm:
            1) Minimize the amplified test methods.
            2) Rename local variables
            3) rename the test methods
     */

    public static void main(String[] args) {
        JSAPOptions.parse(args);
        final CtType<?> amplifiedTestClass = loadAmplifiedTestClass();
        final List<CtMethod<?>> prettifiedAmplifiedTestMethods = run(amplifiedTestClass);
        // output now
        PrettifiedTestMethods.output(amplifiedTestClass, prettifiedAmplifiedTestMethods);
    }

    public static CtType<?> loadAmplifiedTestClass() {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource(InputConfiguration.get().getPathToAmplifiedTestClass());
        launcher.buildModel();
        return launcher.getFactory().Class().getAll().get(0);
    }

    public static List<CtMethod<?>> run(CtType<?> amplifiedTestClass) {
        // 1
        final List<CtMethod<?>> minimizedAmplifiedTestMethods = applyMinimization(
                TestFramework.getAllTest(amplifiedTestClass)
        );
        // 2

        // TODO

        // 3
        applyCode2Vec(InputConfiguration.get().getPathToRootOfCode2Vec(),
                InputConfiguration.get().getRelativePathToModelForCode2Vec(),
                minimizedAmplifiedTestMethods
        );
        return minimizedAmplifiedTestMethods;
    }

    public static List<CtMethod<?>> applyMinimization(List<CtMethod<?>> amplifiedTestMethodsToBeRenamed) {
        final GeneralMinimizer generalMinimizer = new GeneralMinimizer();
        return amplifiedTestMethodsToBeRenamed.stream()
                .map(generalMinimizer::minimize)
                .collect(Collectors.toList());

    }

    public static void applyCode2Vec(String pathToRootOfCode2Vec,
                                     String relativePathToModel,
                                     List<CtMethod<?>> amplifiedTestMethodsToBeRenamed) {
        Code2VecWriter writer = new Code2VecWriter(pathToRootOfCode2Vec);
        Code2VecExecutor code2VecExecutor = null;
        try {
            code2VecExecutor = new Code2VecExecutor(pathToRootOfCode2Vec, relativePathToModel);
            for (CtMethod<?> amplifiedTestMethodToBeRenamed : amplifiedTestMethodsToBeRenamed) {
                writer.writeCtMethodToInputFile(amplifiedTestMethodToBeRenamed);
                code2VecExecutor.run();
                final String code2vecOutput = code2VecExecutor.getOutput();
                final String predictedSimpleName = Code2VecParser.parse(code2vecOutput);
                LOGGER.info("Code2Vec predicted {} for {} as new name", predictedSimpleName, amplifiedTestMethodToBeRenamed.getSimpleName());
                amplifiedTestMethodToBeRenamed.setSimpleName(predictedSimpleName);
            }
        } finally {
            if (code2VecExecutor != null) {
                code2VecExecutor.stop();
            }
        }
    }

}
