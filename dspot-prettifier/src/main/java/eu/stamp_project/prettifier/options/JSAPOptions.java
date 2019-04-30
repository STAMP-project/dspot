package eu.stamp_project.prettifier.options;

import com.martiansoftware.jsap.*;
import eu.stamp_project.utils.AmplificationHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class JSAPOptions extends eu.stamp_project.utils.options.JSAPOptions {

    public static final JSAP prettifierOptions = initPrettifierOptions();

    /**
     * parse the command line argument
     * This method will initialize both {@link eu.stamp_project.prettifier.options.InputConfiguration} and {@link eu.stamp_project.utils.program.InputConfiguration}
     * @param args command line arguments. Refer to the README on the github page or use --help command line option to display all the accepted arguments.
     * @return always true
     */
    public static boolean parse(String[] args) {
        SplittedCommandLineOptions splittedOptions = new SplittedCommandLineOptions(args);
        System.out.println(splittedOptions);
        eu.stamp_project.utils.options.JSAPOptions.parse(splittedOptions.getDSpotOptions());
        JSAPResult jsapConfig = prettifierOptions.parse(splittedOptions.getPrettifierOptions());
        if (!jsapConfig.success() || jsapConfig.getBoolean("help")) {
            System.err.println();
            for (Iterator<?> errs = jsapConfig.getErrorMessageIterator(); errs.hasNext(); ) {
                System.err.println("Error: " + errs.next());
            }
            showUsage();
        }

        eu.stamp_project.utils.program.InputConfiguration.get().setVerbose(true);
        //eu.stamp_project.utils.program.InputConfiguration.get().setDescartesMode(false);

        InputConfiguration.get()
                .setPathToAmplifiedTestClass(jsapConfig.getString("path-to-amplified-test-class"))
                .setPathToRootOfCode2Vec(jsapConfig.getString("path-to-code2vec"))
                .setRelativePathToModelForCode2Vec(jsapConfig.getString("path-to-code2vec-model"));

        System.out.println(InputConfiguration.get());

        return true;
    }

    public static void showUsage() {
        eu.stamp_project.utils.options.JSAPOptions.showUsage();
        System.err.println();
        System.err.println("Usage: java -jar target/dspot-<version>-jar-with-dependencies.jar");
        System.err.println("                          " + prettifierOptions.getUsage());
        System.err.println();
        System.err.println(prettifierOptions.getHelp());
        System.exit(1);
    }

    private static class SplittedCommandLineOptions {
        private List<String> dspotOptions;
        private List<String> prettifierOptions;
        public SplittedCommandLineOptions(String [] args) {
            this.dspotOptions = new ArrayList<>();
            this.prettifierOptions = new ArrayList<>();
            for (int i = 0; i < args.length ;) {
                if (PRETTIFIER_OPTIONS.contains(args[i])) {
                    this.prettifierOptions.add(args[i++]);
                    this.prettifierOptions.add(args[i++]);
                } else {
                    this.dspotOptions.add(args[i++]);
                    this.dspotOptions.add(args[i++]);
                }
            }
        }
        public String[] getDSpotOptions() {
            return this.dspotOptions.toArray(new String[this.dspotOptions.size()]);
        }
        public String[] getPrettifierOptions() {
            return this.prettifierOptions.toArray(new String[this.prettifierOptions.size()]);
        }

        @Override
        public String toString() {
            return "SplittedCommandLineOptions{" +
                    "dspotOptions=" + dspotOptions +
                    ", prettifierOptions=" + prettifierOptions +
                    '}';
        }
    }

    private final static List<String> PRETTIFIER_OPTIONS = new ArrayList<>();

    static {
        PRETTIFIER_OPTIONS.add("--path-to-amplified-test-class");
        PRETTIFIER_OPTIONS.add("--path-to-code2vec");
        PRETTIFIER_OPTIONS.add("--path-to-code2vec-model");
    }

    private static JSAP initPrettifierOptions() {

        /*
            Here, we define extra command line option to apply DSpot-prettifier
                Since we use also the command line options of DSpot
                We will use only long flag, in order to not have any conflict with the Core options.
         */

        JSAP jsap = new JSAP();

        Switch help = new Switch("help");
        help.setDefault("false");
        help.setLongFlag("help");
        help.setShortFlag('h');
        help.setHelp("show this help");

        FlaggedOption pathToAmplifiedTestClass = new FlaggedOption("path-to-amplified-test-class");
        pathToAmplifiedTestClass.setStringParser(JSAP.STRING_PARSER);
        pathToAmplifiedTestClass.setLongFlag("path-to-amplified-test-class");
        pathToAmplifiedTestClass.setRequired(true);
        pathToAmplifiedTestClass.setHelp("[mandatory] Specify the path to the java test class that has been amplified " + AmplificationHelper.LINE_SEPARATOR +
                    "and that contains some amplified test methods to be \"prettified\".");

        FlaggedOption pathToCode2Vec = new FlaggedOption("path-to-code2vec");
        pathToCode2Vec.setStringParser(JSAP.STRING_PARSER);
        pathToCode2Vec.setLongFlag("path-to-code2vec");
        pathToCode2Vec.setRequired(true);
        pathToCode2Vec.setHelp("[mandatory] Specify the path to the folder root of Code2Vec. " + AmplificationHelper.LINE_SEPARATOR +
                "This folder should be a fresh clone of https://github.com/tech-srl/code2vec.git" + AmplificationHelper.LINE_SEPARATOR +
                "We advise you to use absolute path.");

        FlaggedOption pathToModel = new FlaggedOption("path-to-code2vec-model");
        pathToModel.setStringParser(JSAP.STRING_PARSER);
        pathToModel.setLongFlag("path-to-code2vec-model");
        pathToModel.setRequired(true);
        pathToModel.setHelp("[mandatory] Specify the relative path to the model trained with Code2Vec. " + AmplificationHelper.LINE_SEPARATOR +
                "This path will be use relatively from --path-to-code2vec value.");

        try {
            jsap.registerParameter(pathToAmplifiedTestClass);
            jsap.registerParameter(pathToCode2Vec);
            jsap.registerParameter(pathToModel);
            jsap.registerParameter(help);
        } catch (JSAPException e) {
            throw new RuntimeException(e);
        }
        return jsap;
    }

}
