package eu.stamp_project.prettifier.options;

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

    private String pathToRootOfCode2Vec;

    public String getPathToRootOfCode2Vec() {
        return pathToRootOfCode2Vec;
    }

    public InputConfiguration setPathToRootOfCode2Vec(String pathToRootOfCode2Vec) {
        this.pathToRootOfCode2Vec = pathToRootOfCode2Vec;
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
}
