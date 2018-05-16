package eu.stamp_project;

import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.selector.TestSelector;

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

    @Override
    public String toString() {
    	String toReturn = " path to configuration file: " + this.pathToConfigurationFile + "\n";
    	toReturn += "amplifiers: " + this.amplifiers + "\n";
    	toReturn += "nb iteration: " + this.nbIteration + "\n";
    	toReturn += "test classes: " + this.testClasses + "\n";
    	toReturn += "path to output: " + this.pathToOutput + "\n";
    	toReturn += "selector: " + this.selector + "\n";
    	toReturn += "test cases: " + this.testCases + "\n";
    	toReturn += "seed: " + this.seed + "\n";
    	toReturn += "time out in ms: " + this.timeOutInMs + "\n";
    	toReturn += "automatic builder name: " + this.automaticBuilderName + "\n";
    	toReturn += "maven home: " + this.mavenHome + "\n";
    	toReturn += "max test amplified: " + this.maxTestAmplified + "\n";
    	toReturn += "clean: " + this.clean + "\n";
    	toReturn += "minimize: " + this.minimize + "\n";
    	return toReturn;
    }
}