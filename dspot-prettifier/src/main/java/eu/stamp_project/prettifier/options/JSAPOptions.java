package eu.stamp_project.prettifier.options;

import com.martiansoftware.jsap.*;
import eu.stamp_project.utils.AmplificationHelper;

import java.util.Iterator;

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
        eu.stamp_project.utils.options.JSAPOptions.parse(args);
        JSAPResult jsapConfig = options.parse(args);
        if (!jsapConfig.success() || jsapConfig.getBoolean("help")) {
            System.err.println();
            for (Iterator<?> errs = jsapConfig.getErrorMessageIterator(); errs.hasNext(); ) {
                System.err.println("Error: " + errs.next());
            }
            showUsage();
        }

        InputConfiguration.get()
                .setPathToRootOfCode2Vec(jsapConfig.getString("path-to-code2vec"))
                .setRelativePathToModelForCode2Vec(jsapConfig.getString("path-to-code2vec-model"));

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
            jsap.registerParameter(pathToCode2Vec);
            jsap.registerParameter(pathToModel);
            jsap.registerParameter(help);
        } catch (JSAPException e) {
            throw new RuntimeException(e);
        }
        return jsap;
    }

}
