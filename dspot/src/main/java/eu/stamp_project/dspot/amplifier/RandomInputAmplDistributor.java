package eu.stamp_project.dspot.amplifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stamp_project.dspot.amplifier.amplifiers.Amplifier;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.configuration.UserInput;
import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT, Yosu Gorroñogoitia
 * benjamin.danglot@inria.fr, jesus.gorroñogoitia@atos.net
 * on 24/04/19
 */
public class RandomInputAmplDistributor extends AbstractInputAmplDistributor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomInputAmplDistributor.class);

    public RandomInputAmplDistributor(int maxNumTests, List<Amplifier> amplifiers) {
        super(maxNumTests, amplifiers);
    }

    /**
     * Input amplification for a single test.
     *
     * @param test Test method
     * @param i current iteration
     * @return New generated tests
     */
    protected Stream<CtMethod<?>> inputAmplifyTest(CtMethod<?> test, int i) {
        return this.amplifiers.parallelStream()
                .flatMap(amplifier -> amplifier.amplify(test, i));
    }

    /**
     * Input amplification of multiple tests.
     *
     * @param testMethods Test methods
     * @param i current iteration
     * @return New generated tests
     */
    @Override
    public List<CtMethod<?>> inputAmplify(List<CtMethod<?>> testMethods, int i) {
        LOGGER.info("Amplification of inputs...");
        List<CtMethod<?>> inputAmplifiedTests = testMethods.parallelStream()
                .flatMap(test -> {
                    final Stream<CtMethod<?>> inputAmplifiedTestMethods = inputAmplifyTest(test, i);
                    DSpotUtils.printProgress(testMethods.indexOf(test), testMethods.size());
                    return inputAmplifiedTestMethods;
                }).collect(Collectors.toList());
        LOGGER.info("{} new tests generated", inputAmplifiedTests.size());
        return reduce(inputAmplifiedTests);
    }

    /**
     * Reduces the number of amplified tests to a practical threshold (see {@link UserInput#getMaxTestAmplified()}).
     * This method randomly selects the tests to keep
     * @param tests List of tests to be reduced
     * @return A subset of the input tests
     */

    public List<CtMethod<?>> reduce(List<CtMethod<?>> tests) {
        final List<CtMethod<?>> reducedTests = new ArrayList<>();

        final int testsSize = tests.size();
        if (testsSize > maxNumTests) {
            Random random = new Random();
            LOGGER.warn("Too many tests have been generated: {}", testsSize);
            for (int i=0;i<maxNumTests; i++) {
                reducedTests.add(tests.get(random.nextInt(testsSize)));
            }
            LOGGER.info("Number of generated test reduced to {}", reducedTests.size());
        }
        if (reducedTests.isEmpty()) {
            reducedTests.addAll(tests);
        }
        return reducedTests;
    }
}
