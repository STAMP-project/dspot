package eu.stamp_project.dspot.budget;

import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * Reduces the number of amplified tests to a practical threshold (see {@link InputConfiguration#getMaxTestAmplified()}).
     * <p>
     * <p>The reduction aims at keeping a maximum of diversity. Because all the amplified tests come from the same
     * original test, they have a <em>lot</em> in common.
     * <p>
     * <p>Diversity is measured with the textual representation of amplified tests. We use the sum of the bytes returned
     * by the {@link String#getBytes()} method and keep the amplified tests with the most distant values.
     *
     * @param tests List of tests to be reduced
     * @return A subset of the input tests
     */
    public List<CtMethod<?>> reduce(List<CtMethod<?>> tests) {
        final List<CtMethod<?>> reducedTests = new ArrayList<>();
        if (tests.size() > InputConfiguration.get().getMaxTestAmplified()) {
            LOGGER.warn("Too many tests have been generated: {}", tests.size());
            final Map<Long, List<CtMethod<?>>> valuesToMethod = new HashMap<>();
            for (CtMethod<?> test : tests) {
                final long value = sumByteArrayToLong(test.toString().getBytes());
                if (!valuesToMethod.containsKey(value)) {
                    valuesToMethod.put(value, new ArrayList<>());
                }
                valuesToMethod.get(value).add(test);
            }
            final Long average = average(valuesToMethod.keySet());
            while (reducedTests.size() < InputConfiguration.get().getMaxTestAmplified()) {
                final Long furthest = furthest(valuesToMethod.keySet(), average);
                reducedTests.add(valuesToMethod.get(furthest).get(0));
                if (valuesToMethod.get(furthest).isEmpty()) {
                    valuesToMethod.remove(furthest);
                } else {
                    valuesToMethod.get(furthest).remove(0);
                    if (valuesToMethod.get(furthest).isEmpty()) {
                        valuesToMethod.remove(furthest);
                    }
                }
            }
            LOGGER.info("Number of generated test reduced to {}", reducedTests.size());
        }
        if (reducedTests.isEmpty()) {
            reducedTests.addAll(tests);
        } else {
            tests.stream()
                    .filter(test -> !reducedTests.contains(test))
                    .forEach(discardedTest -> AmplificationHelper.ampTestToParent.remove(discardedTest));
        }
        return reducedTests;
    }

    /**
     * Returns the average of a collection of double
     */
    private Long average(Collection<Long> values) {
        return values.stream().collect(Collectors.averagingLong(Long::longValue)).longValue();
    }

    /**
     * Returns the first, most distant element of a collection from a defined value.
     */
    private Long furthest(Collection<Long> values, Long average) {
        return values.stream()
                .max(Comparator.comparingLong(d -> Math.abs(d - average)))
                .orElse(null);
    }

    private long sumByteArrayToLong(byte[] byteArray) {
        long sum = 0L;
        for (byte aByteArray : byteArray) {
            sum += (int) aByteArray;
        }
        return sum;
    }
}
