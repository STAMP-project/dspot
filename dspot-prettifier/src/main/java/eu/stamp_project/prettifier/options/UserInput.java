package eu.stamp_project.prettifier.options;

import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.configuration.check.InputErrorException;
import picocli.CommandLine;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
@CommandLine.Command(name = "eu.stamp_project.Main", mixinStandardHelpOptions = true)
public class UserInput extends eu.stamp_project.dspot.common.configuration.UserInput {

    public UserInput() {

    }

    @CommandLine.Option(
            names = "--path-to-amplified-test-class",
            description = "[mandatory] Specify the path to the java test class that has been amplified " +
                    "and that contains some amplified test methods to be \"prettified\"."
    )
    private String pathToAmplifiedTestClass;

    public String getPathToAmplifiedTestClass() {
        return this.pathToAmplifiedTestClass;
    }

    public UserInput setPathToAmplifiedTestClass(String pathToAmplifiedTestClass) {
        if (!pathToAmplifiedTestClass.endsWith(".java")) {
            // TODO must add this error to the global report
            throw new InputErrorException();
        }
        this.pathToAmplifiedTestClass = pathToAmplifiedTestClass;
        return this;
    }

    // Code2Vec

    @CommandLine.Option(
            names = "--path-to-code2vec",
            description = "[mandatory] Specify the path to the folder root of Code2Vec. " +
                    "This folder should be a fresh clone of https://github.com/tech-srl/code2vec.git" +
                    "We advise you to use absolute path."
    )
    private String pathToRootOfCode2Vec;

    public String getPathToRootOfCode2Vec() {
        return pathToRootOfCode2Vec;
    }

    public UserInput setPathToRootOfCode2Vec(String pathToRootOfCode2Vec) {
        this.pathToRootOfCode2Vec = DSpotUtils.shouldAddSeparator.apply(pathToRootOfCode2Vec);
        return this;
    }

    @CommandLine.Option(
            names = "--path-to-code2vec-model",
            description = "[mandatory] Specify the relative path to the model trained with Code2Vec. " +
                    "This path will be use relatively from --path-to-code2vec value."
    )
    private String relativePathToModelForCode2Vec;

    public String getRelativePathToModelForCode2Vec() {
        return relativePathToModelForCode2Vec;
    }

    public UserInput setRelativePathToModelForCode2Vec(String relativePathToModelForCode2Vec) {
        this.relativePathToModelForCode2Vec = relativePathToModelForCode2Vec;
        return this;
    }

    private long timeToWaitForCode2vecInMillis = 90000;

    public long getTimeToWaitForCode2vecInMillis() {
        return this.timeToWaitForCode2vecInMillis;
    }

    public UserInput setTimeToWaitForCode2vecInMillis(long timeToWaitForCode2vecInMillis) {
        this.timeToWaitForCode2vecInMillis = timeToWaitForCode2vecInMillis;
        return this;
    }
    
    // Context2Name

    private String pathToRootOfContext2Name;

    public String getPathToRootOfContext2Name() {
        return pathToRootOfContext2Name;
    }

    public UserInput setPathToRootOfContext2Name(String pathToRootOfContext2Name) {
        this.pathToRootOfContext2Name = DSpotUtils.shouldAddSeparator.apply(pathToRootOfContext2Name);
        return this;
    }

    private String relativePathToModelForContext2Name;

    public String getRelativePathToModelForContext2Name() {
        return relativePathToModelForContext2Name;
    }

    public UserInput setRelativePathToModelForContext2Name(String relativePathToModelForContext2Name) {
        this.relativePathToModelForContext2Name = relativePathToModelForContext2Name;
        return this;
    }

    private long timeToWaitForContext2nameInMillis = 90000;

    public long getTimeToWaitForContext2nameInMillis() {
        return this.timeToWaitForContext2nameInMillis;
    }

    public UserInput setTimeToWaitForContext2nameInMillis(long timeToWaitForContext2nameInMillis) {
        this.timeToWaitForContext2nameInMillis = timeToWaitForContext2nameInMillis;
        return this;
    }
}
