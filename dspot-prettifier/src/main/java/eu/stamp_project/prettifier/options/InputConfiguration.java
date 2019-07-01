package eu.stamp_project.prettifier.options;

import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.options.check.InputErrorException;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class InputConfiguration {

    private static InputConfiguration instance;

    private InputConfiguration() {

    }

    /**
     * Getter on the singleton instance
     * @return the singleton instance of InputConfiguration
     */
    public static InputConfiguration get() {
        if (InputConfiguration.instance == null) {
            InputConfiguration.instance = new InputConfiguration();
        }
        return InputConfiguration.instance;
    }

    /*
        PROPERTY
     */

    private String pathToAmplifiedTestClass;

    public String getPathToAmplifiedTestClass() {
        return this.pathToAmplifiedTestClass;
    }

    public InputConfiguration setPathToAmplifiedTestClass(String pathToAmplifiedTestClass) {
        if (!pathToAmplifiedTestClass.endsWith(".java")) {
            // TODO must add this error to the global report
            throw new InputErrorException();
        }
        this.pathToAmplifiedTestClass = pathToAmplifiedTestClass;
        return this;
    }

    @Override
    public String toString() {
        return "InputConfiguration{" +
                "pathToAmplifiedTestClass='" + pathToAmplifiedTestClass + '\'' +
                ", pathToRootOfContext2Code='" + pathToRootOfContext2Code + '\'' +
                ", relativePathToModelForContext2Code='" + relativePathToModelForContext2Code + '\'' +
                ", pathToRootOfCode2Vec='" + pathToRootOfCode2Vec + '\'' +
                ", relativePathToModelForCode2Vec='" + relativePathToModelForCode2Vec + '\'' +
                '}';
    }

    // Context2Code

    private String pathToRootOfContext2Code;

    public String getPathToRootOfContext2Code() {
        return pathToRootOfContext2Code;
    }

    public InputConfiguration setPathToRootOfContext2Code(String pathToRootOfContext2Code) {
        this.pathToRootOfContext2Code = DSpotUtils.shouldAddSeparator.apply(pathToRootOfContext2Code);
        return this;
    }

    private String relativePathToModelForContext2Code;

    public String getRelativePathToModelForContext2Code() {
        return relativePathToModelForContext2Code;
    }

    public InputConfiguration setRelativePathToModelForContext2Code(String relativePathToModelForContext2Code) {
        this.relativePathToModelForContext2Code = relativePathToModelForContext2Code;
        return this;
    }

    private long timeToWaitForContext2codeInMillis = 90000;

    public long getTimeToWaitForContext2codeInMillis() {
        return this.timeToWaitForContext2codeInMillis;
    }

    public InputConfiguration setTimeToWaitForContext2codeInMillis(long timeToWaitForContext2codeInMillis) {
        this.timeToWaitForContext2codeInMillis = timeToWaitForContext2codeInMillis;
        return this;
    }

    // Code2Vec

    private String pathToRootOfCode2Vec;

    public String getPathToRootOfCode2Vec() {
        return pathToRootOfCode2Vec;
    }

    public InputConfiguration setPathToRootOfCode2Vec(String pathToRootOfCode2Vec) {
        this.pathToRootOfCode2Vec = DSpotUtils.shouldAddSeparator.apply(pathToRootOfCode2Vec);
        return this;
    }

    private String relativePathToModelForCode2Vec;

    public String getRelativePathToModelForCode2Vec() {
        return relativePathToModelForCode2Vec;
    }

    public InputConfiguration setRelativePathToModelForCode2Vec(String relativePathToModelForCode2Vec) {
        this.relativePathToModelForCode2Vec = relativePathToModelForCode2Vec;
        return this;
    }

    private long timeToWaitForCode2vecInMillis = 90000;

    public long getTimeToWaitForCode2vecInMillis() {
        return this.timeToWaitForCode2vecInMillis;
    }

    public InputConfiguration setTimeToWaitForCode2vecInMillis(long timeToWaitForCode2vecInMillis) {
        this.timeToWaitForCode2vecInMillis = timeToWaitForCode2vecInMillis;
        return this;
    }
}
