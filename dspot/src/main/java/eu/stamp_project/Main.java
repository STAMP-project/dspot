package eu.stamp_project;

import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.utils.options.check.Checker;
import eu.stamp_project.utils.options.check.InputErrorException;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.GlobalReport;
import eu.stamp_project.utils.report.error.ErrorReportImpl;
import eu.stamp_project.utils.report.output.OutputReportImpl;
import eu.stamp_project.utils.report.output.selector.TestSelectorReportImpl;
import picocli.CommandLine;

/**
 * Created by Benjamin DANGLOT benjamin.danglot@inria.fr on 2/9/17
 */
public class Main {

    public static final GlobalReport GLOBAL_REPORT =
            new GlobalReport(new OutputReportImpl(), new ErrorReportImpl(), new TestSelectorReportImpl());
    public static boolean verbose = false;

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
        verbose = inputConfiguration.isVerbose();
        return inputConfiguration;
    }
}
