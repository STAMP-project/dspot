package fr.inria.stamp;

import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.selector.TestSelector;

import java.util.List;

public class Configuration {

        public final String pathToConfigurationFile;
        public final List<Amplifier> amplifiers;
        public final int nbIteration;
        public final List<String> testCases;
        public final String pathToOutput;
        public final TestSelector selector;
        public final List<String> namesOfTestCases;
        public final long seed;

        public Configuration(String pathToConfigurationFile, List<Amplifier> amplifiers, int nbIteration, List<String> testCases, String pathToOutput, TestSelector selector, List<String> namesOfTestCases, long seed) {
            this.pathToConfigurationFile = pathToConfigurationFile;
            this.amplifiers = amplifiers;
            this.nbIteration = nbIteration;
            this.testCases = testCases;
            this.pathToOutput = pathToOutput;
            this.selector = selector;
            this.namesOfTestCases = namesOfTestCases;
            this.seed = seed;
        }
    }