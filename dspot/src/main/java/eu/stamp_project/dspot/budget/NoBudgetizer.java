package eu.stamp_project.dspot.budget;

import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/07/18
 */
public class NoBudgetizer implements Budgetizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoBudgetizer.class);

    /**
     * Input amplification for a single test.
     *
     * @param test Test method
     * @return New generated tests
     */
    protected Stream<CtMethod<?>> inputAmplifyTest(CtMethod<?> test, int i) {
        return InputConfiguration.get()
                .getAmplifiers()
                .parallelStream()
                .flatMap(amplifier -> amplifier.amplify(test, i));
    }

    /**
     * Input amplification of multiple tests.
     *
     * @param testMethods Test methods
     * @return New generated tests
     */
    @Override
    public List<CtMethod<?>> inputAmplify(List<CtMethod<?>> testMethods, int i) {
        LOGGER.info("Amplification of inputs...");
        List<CtMethod<?>> amplifiedTests = testMethods.parallelStream()
                .flatMap(test -> {
                    DSpotUtils.printProgress(testMethods.indexOf(test), testMethods.size());
                    return inputAmplifyTest(test, i);
                }).collect(Collectors.toList());
        LOGGER.info("{} new tests generated", amplifiedTests.size());
        return amplifiedTests;
    }
}
