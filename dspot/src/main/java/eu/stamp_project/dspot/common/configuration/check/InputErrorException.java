package eu.stamp_project.dspot.common.configuration.check;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/11/18
 */
public class InputErrorException extends RuntimeException {

    public InputErrorException() {
        super("Error in the provided input. Please check your properties file and your command-line options.");
    }

}
