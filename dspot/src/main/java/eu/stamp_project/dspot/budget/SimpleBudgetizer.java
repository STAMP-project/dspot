package eu.stamp_project.dspot.budget;

import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.RandomHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/07/18
 */
public class SimpleBudgetizer implements Budgetizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBudgetizer.class);

    /**
     * Input amplification of multiple tests.
     *
     * @param testMethods Test methods
     * @return New generated tests
     */
    @Override
    public List<CtMethod<?>> inputAmplify(List<CtMethod<?>> testMethods, int iteration) {
        LOGGER.info("Amplification of inputs...");
        final int budget = InputConfiguration.get().getMaxTestAmplified();
        int totalBudget = InputConfiguration.get().getMaxTestAmplified();
        // copy the amplifiers, we will remove amplifier that does not generate more test
        final List<Amplifier> amplifiers = new ArrayList<>(InputConfiguration.get().getAmplifiers());
        // copy the test methods to be amplified
        final ArrayList<CtMethod<?>> testMethodsToBeAmplified = new ArrayList<>(testMethods);
        // Amplify all method using all amplifiers
        long time = System.currentTimeMillis();
        final Map<Amplifier, Map<CtMethod<?>, List<CtMethod<?>>>> amplifiedTestMethodPerAmplifierPerTestMethod =
                amplify(amplifiers, testMethodsToBeAmplified, iteration);
        LOGGER.info("Time to amplify: {}ms", System.currentTimeMillis() - time);

        final List<CtMethod<?>> amplifiedTestMethods = new ArrayList<>();
        while (totalBudget > 0 && !amplifiedTestMethodPerAmplifierPerTestMethod.isEmpty()) {
            DSpotUtils.printProgress(budget - totalBudget, budget);
            final int nbAmplifierRemaining = amplifiedTestMethodPerAmplifierPerTestMethod.size();
            int budgetPerAmplifier = totalBudget / nbAmplifierRemaining;
            if (budgetPerAmplifier == 0) { // not enough budget, we get last randomly and quit
                amplifiedTestMethods.addAll(getLastAmplifiedMethods(totalBudget, amplifiedTestMethodPerAmplifierPerTestMethod));
                break;
            }
            for (Amplifier amplifier : amplifiedTestMethodPerAmplifierPerTestMethod.keySet()) {
                totalBudget = selectAndAddAmplifiedTestMethods(totalBudget,
                        amplifiedTestMethodPerAmplifierPerTestMethod,
                        amplifiedTestMethods,
                        budgetPerAmplifier,
                        amplifier
                );
            }
            amplifiedTestMethodPerAmplifierPerTestMethod.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
        LOGGER.info("{} new tests generated", amplifiedTestMethods.size());
        return amplifiedTestMethods;
    }

    private Map<Amplifier, Map<CtMethod<?>, List<CtMethod<?>>>> amplify(List<Amplifier> amplifiers,
                                                                        ArrayList<CtMethod<?>> testMethodsToBeAmplified,
                                                                        int iteration) {
        Map<Amplifier, Map<CtMethod<?>, List<CtMethod<?>>>> amplifiedTestMethodPerAmplifierPerTestMethod = new HashMap<>();
        for (Amplifier amplifier : amplifiers) {
            amplifiedTestMethodPerAmplifierPerTestMethod.put(amplifier, new HashMap<>());
            for (CtMethod<?> testMethod : testMethodsToBeAmplified) {
                final List<CtMethod<?>> amplification = amplifier.amplify(testMethod, 0).collect(Collectors.toList());
                Collections.shuffle(amplification, RandomHelper.getRandom());
                amplifiedTestMethodPerAmplifierPerTestMethod.get(amplifier).put(testMethod, amplification);
            }
        }
        return amplifiedTestMethodPerAmplifierPerTestMethod;
    }

    private int selectAndAddAmplifiedTestMethods(int totalBudget,
                                                 Map<Amplifier, Map<CtMethod<?>, List<CtMethod<?>>>> amplifiedTestMethodPerAmplifierPerTestMethod,
                                                 List<CtMethod<?>> amplifiedTestMethods,
                                                 int budgetPerAmplifier,
                                                 Amplifier amplifier) {
        final Map<CtMethod<?>, List<CtMethod<?>>> amplificationPerTestMethod = amplifiedTestMethodPerAmplifierPerTestMethod.get(amplifier);
        // in case there is less budget per amplifier than methods, we take the correct upper bound
        int upperBound = Math.min(amplificationPerTestMethod.size(), budgetPerAmplifier);
        int budgetPerAmplifierPerTestMethod = Math.max(1, budgetPerAmplifier / upperBound);
        for (CtMethod<?> ctMethod : new ArrayList<>(amplificationPerTestMethod.keySet()).subList(0, upperBound)) {
            totalBudget = selectAndAddAmplifiedTestMethodsFromAnOriginalTestMethod(totalBudget, amplifiedTestMethods, amplificationPerTestMethod, budgetPerAmplifierPerTestMethod, ctMethod);
        }
        amplificationPerTestMethod.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        return totalBudget;
    }

    private int selectAndAddAmplifiedTestMethodsFromAnOriginalTestMethod(int totalBudget,
                                                                         List<CtMethod<?>> amplifiedTestMethods,
                                                                         Map<CtMethod<?>, List<CtMethod<?>>> amplificationPerTestMethod,
                                                                         int budgetPerAmplifierPerTestMethod,
                                                                         CtMethod<?> ctMethod) {
        final List<CtMethod<?>> candidate = amplificationPerTestMethod.get(ctMethod);
        int minSize = Math.min(budgetPerAmplifierPerTestMethod, candidate.size());
        totalBudget = totalBudget - minSize; // decrease the budget
        final List<CtMethod<?>> selectedAmplifiedTestMethods = candidate.subList(0, minSize);
        amplifiedTestMethods.addAll(selectedAmplifiedTestMethods); // adding all selected method to the list
        candidate.removeAll(selectedAmplifiedTestMethods);
        return totalBudget;
    }

    private List<CtMethod<?>> getLastAmplifiedMethods(int totalBudget,
                                                      Map<Amplifier, Map<CtMethod<?>, List<CtMethod<?>>>>
                                                              amplifiedTestMethodPerAmplifierPerTestMethod) {
        final List<CtMethod<?>> allAmplifiedTestMethods = amplifiedTestMethodPerAmplifierPerTestMethod.keySet()
                .stream()
                .flatMap(amplifier ->
                        amplifiedTestMethodPerAmplifierPerTestMethod.get(amplifier).keySet()
                                .stream()
                                .flatMap(ctMethod ->
                                        amplifiedTestMethodPerAmplifierPerTestMethod.get(amplifier).get(ctMethod).stream()
                                )
                ).collect(Collectors.toList());
        List<CtMethod<?>> lastAmplifiedTestMethods = new ArrayList<>();
        while (totalBudget > 0) {
            lastAmplifiedTestMethods.add(
                    allAmplifiedTestMethods.get(RandomHelper.getRandom().nextInt(allAmplifiedTestMethods.size())));
            totalBudget--;
        }
        return lastAmplifiedTestMethods;
    }

}
