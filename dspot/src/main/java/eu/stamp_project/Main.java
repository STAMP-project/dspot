package eu.stamp_project;

import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.configuration.check.Checker;
import eu.stamp_project.dspot.common.configuration.check.InputErrorException;
import picocli.CommandLine;

/**
 * Created by Benjamin DANGLOT benjamin.danglot@inria.fr on 2/9/17
 */
public class Main {

    public static void main(String[] args) {
        UserInput userInput = parse(args);
        if(userInput == null){
            return;
        }
        final DSpot dspot = new DSpot(userInput);
        dspot.run();
    }

    public static UserInput parse(String[] args) {
        UserInput userInput = new UserInput();
        final CommandLine commandLine = new CommandLine(userInput);
        commandLine.setUsageHelpWidth(120);
        try {
            commandLine.parseArgs(args);
        } catch (Exception e) {
            e.printStackTrace();
            commandLine.usage(System.err);
            return null;
        }
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return null;
        }
        if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return null;
        }
        if (userInput.shouldRunExample()) {
            userInput.configureExample();
        }
        try {
            Checker.preChecking(userInput);
        } catch (InputErrorException e) {
            e.printStackTrace();
            commandLine.usage(System.err);
            return null;
        }
        return userInput;
    }
}
