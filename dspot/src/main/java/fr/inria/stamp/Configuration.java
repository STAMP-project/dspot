package fr.inria.stamp;

import fr.inria.diversify.dspot.amplifier.Amplifier;
import fr.inria.diversify.dspot.selector.TestSelector;

import java.util.List;

public class Configuration {

    public final String pathToConfigurationFile;
    public final List<Amplifier> amplifiers;
    public final int nbIteration;
    public final List<String> testClasses;
    public final String pathToOutput;
    public final TestSelector selector;
    public final List<String> testCases;
    public final long seed;
    public final int timeOutInMs;
    public final String automaticBuilderName;
    public final String mavenHome;
    public final Integer maxTestAmplified;
    public final boolean clean;
    public final boolean minimize;

    public Configuration(String pathToConfigurationFile,
                         List<Amplifier> amplifiers,
                         int nbIteration,
                         List<String> testClasses,
                         String pathToOutput,
                         TestSelector selector,
                         List<String> testCases,
                         long seed,
                         int timeOutInMs,
                         String automaticBuilderName,
                         String mavenHome,
                         Integer maxTestAmplified,
                         boolean clean,
                         boolean minimize) {
        this.pathToConfigurationFile = pathToConfigurationFile;
        this.amplifiers = amplifiers;
        this.nbIteration = nbIteration;
        this.testClasses = testClasses;
        this.pathToOutput = pathToOutput;
        this.selector = selector;
        this.testCases = testCases;
        this.seed = seed;
        this.timeOutInMs = timeOutInMs;
        this.automaticBuilderName = automaticBuilderName;
        this.mavenHome = mavenHome;
        this.maxTestAmplified = maxTestAmplified;
        this.clean = clean;
        this.minimize = minimize;
    }
}