package eu.stamp_project;

import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.common.configuration.check.Checker;
import eu.stamp_project.dspot.common.configuration.check.InputErrorException;
import eu.stamp_project.dspot.common.configuration.InputConfiguration;
import picocli.CommandLine;

/**
 * Created by Benjamin DANGLOT benjamin.danglot@inria.fr on 2/9/17
 */
public class Main {

    public static void main(String[] args) {
        InputConfiguration inputConfiguration = parse(args);
        if(inputConfiguration == null){
            return;
        }
        final DSpot dspot = new DSpot(inputConfiguration);
        dspot.run();
    }

    public static InputConfiguration parse(String[] args) {
        InputConfiguration inputConfiguration = new InputConfiguration();
        final CommandLine commandLine = new CommandLine(inputConfiguration);
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
        if (inputConfiguration.shouldRunExample()) {
            inputConfiguration.configureExample();
        }
        try {
            Checker.preChecking(inputConfiguration);
        } catch (InputErrorException e) {
            e.printStackTrace();
            commandLine.usage(System.err);
            return null;
        }
        return inputConfiguration;
    }
}
