package eu.stamp_project.prettifier.code2vec.builder;

import spoon.compiler.Environment;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;

import java.io.File;
import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/12/18
 */
public class Output {

    public static final String PATH_TO_BENCHMARK = "benchmark";

    public static final String PATH_TRAINING = PATH_TO_BENCHMARK + "/training/";

    public static final double PROPORTION_TRAINING = .64D;

    public static final String PATH_VALIDATION = PATH_TO_BENCHMARK + "/validation/";

    public static final double PROPORTION_VALIDATION = .16D;

    public static final String PATH_TEST = PATH_TO_BENCHMARK + "/test/";

    public static final double PROPORTION_TEST = .20D;

    private File outputTraining;

    private File outputValidation;

    private File outputTest;

    public Output() {
        new File(PATH_TO_BENCHMARK).mkdir();
        this.outputTraining = new File(PATH_TRAINING);
        this.outputTraining.mkdir();
        this.outputValidation = new File(PATH_VALIDATION);
        this.outputValidation.mkdir();
        this.outputTest = new File(PATH_TEST);
        this.outputTest.mkdir();
    }


    /**
     * This method outputs the list of the given test classes in three folders: training, validation, test
     * The proportion is 64% for the training set, 16% for the validation set, and 20% for the test set
     * The way we split the set is as follow, we output test classes in the current set until we reach AT LEAST (so, maybe more)
     * the proportion desired.
     *
     * @param testClasses the java test classes to be outputted
     * @return an instance of {@link NumbersOfMethodsPerSet} that contains the number of test methods added to each set.
     */
    public NumbersOfMethodsPerSet output(List<CtType<?>> testClasses, int totalNumberOfMethods) {
        int numberOfMethodForTraining = (int) (PROPORTION_TRAINING * (double) totalNumberOfMethods);
        int numberOfMethodForValidation = (int) (PROPORTION_VALIDATION * (double) totalNumberOfMethods);
        int numberOfMethodForTest = (int) (PROPORTION_TEST * (double) totalNumberOfMethods);
        if (numberOfMethodForTraining + numberOfMethodForValidation + numberOfMethodForTest < totalNumberOfMethods) {
            numberOfMethodForTest += totalNumberOfMethods - (numberOfMethodForTraining + numberOfMethodForValidation + numberOfMethodForTest);
        }
        final Factory factory = testClasses.get(0).getFactory();
        int i = printUntilGoalIsReached(this.outputTraining, 0, numberOfMethodForTraining, testClasses, factory);
        i = printUntilGoalIsReached(this.outputValidation, i, numberOfMethodForValidation, testClasses, factory);
        printUntilGoalIsReached(this.outputTest, i, numberOfMethodForTest, testClasses, factory);
        return new NumbersOfMethodsPerSet(numberOfMethodForTraining, numberOfMethodForValidation, numberOfMethodForTest);
    }

    private int printUntilGoalIsReached(File currentOutput,
                                        int indexTestClass,
                                        int numberOfTestMethodGoal,
                                        List<CtType<?>> testClasses,
                                        Factory factory) {
        for (; indexTestClass < testClasses.size(); indexTestClass++) {
            Environment env = factory.getEnvironment();
            env.setAutoImports(true);
            env.setNoClasspath(true);
            env.setCommentEnabled(true);
            JavaOutputProcessor processor = new JavaOutputProcessor(new DefaultJavaPrettyPrinter(env));
            processor.setFactory(factory);
            processor.getEnvironment().setSourceOutputDirectory(currentOutput);
            processor.createJavaFile(testClasses.get(indexTestClass));
            env.setAutoImports(false);
            numberOfTestMethodGoal -= testClasses.get(indexTestClass).getMethods().size();
            if (numberOfTestMethodGoal <= 0) {
                return indexTestClass;
            }
        }
        return -1;
    }

}
