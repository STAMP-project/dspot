package eu.stamp_project.diff_test_selection.configuration;

import com.martiansoftware.jsap.*;

import eu.stamp_project.diff_test_selection.diff.DiffComputer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 01/02/19
 */
public class Options {

    private static final Logger LOGGER = LoggerFactory.getLogger(Options.class);

    public static final JSAP options = initJSAP();

    public static void usage() {
        LOGGER.error("");
        LOGGER.error("Usage: java -jar target/dspot-<version>-jar-with-dependencies.jar");
        LOGGER.error("                          " + options.getUsage());
        LOGGER.error("");
        LOGGER.error(options.getHelp());
        System.exit(1);
    }

    private static String checksIfExistAndUseAbsolutePath(String pathFileToCheck) {
        final File file = new File(pathFileToCheck);
        if (!file.exists()) {
            LOGGER.error(pathFileToCheck + " does not exist, please check it out!", new IllegalArgumentException(pathFileToCheck));
            usage();
        }
        return file.getAbsolutePath();
    }

    /**
     * Build a configuration from a string array. The string array should come from the command line.
     *
     * @param args arguments of the command line to be parsed
     * @return an instance of {@link Configuration} with the values of command line options.
     */
    public static final Configuration parse(String[] args) {
        final JSAPResult arguments = options.parse(args);
        final String pathDirFirstVersion = checksIfExistAndUseAbsolutePath(arguments.getString("path-dir-first-version"));
        final String pathDirSecondVersion = checksIfExistAndUseAbsolutePath(arguments.getString("path-dir-second-version"));
        String pathToDiff = arguments.getString("path-to-diff");
        final String outputPath = arguments.getString("output-path");
        final String outputFormat = arguments.getString("output-format");
        final String module = arguments.getString("module");
        return new Configuration(pathDirFirstVersion,
                pathDirSecondVersion,
                outputPath,
                outputFormat,
                module,
                pathToDiff
        );
    }

    private static final JSAP initJSAP() {
        JSAP jsap = new JSAP();

        FlaggedOption pathDirectoryFirstVersion = new FlaggedOption("path-dir-first-version");
        pathDirectoryFirstVersion.setLongFlag("path-dir-first-version");
        pathDirectoryFirstVersion.setShortFlag('p');
        pathDirectoryFirstVersion.setRequired(true);
        pathDirectoryFirstVersion.setHelp("[Mandatory] Specify the path to root directory of the project in the first version.");
        pathDirectoryFirstVersion.setStringParser(JSAP.STRING_PARSER);

        FlaggedOption pathDirectorySecondVersion = new FlaggedOption("path-dir-second-version");
        pathDirectorySecondVersion.setLongFlag("path-dir-second-version");
        pathDirectorySecondVersion.setShortFlag('q');
        pathDirectorySecondVersion.setRequired(true);
        pathDirectorySecondVersion.setHelp("[Mandatory] Specify the path to root directory of the project in the second version.");
        pathDirectorySecondVersion.setStringParser(JSAP.STRING_PARSER);

        FlaggedOption outputPath = new FlaggedOption("output-path");
        outputPath.setRequired(false);
        outputPath.setLongFlag("output-path");
        outputPath.setShortFlag('o');
        outputPath.setDefault("");
        outputPath.setHelp("[Optional] Specify the path of the output.");
        outputPath.setStringParser(JSAP.STRING_PARSER);

        FlaggedOption outputFormat = new FlaggedOption("output-format");
        outputFormat.setRequired(false);
        outputFormat.setLongFlag("output-format");
        outputFormat.setDefault("CSV");
        outputFormat.setUsageName("[CSV]");
        outputFormat.setHelp("[Optional] Specify the format of the output. (For now, only the CSV format is available)");
        outputFormat.setStringParser(JSAP.STRING_PARSER);

        FlaggedOption module = new FlaggedOption("module");
        module.setRequired(false);
        module.setLongFlag("module");
        module.setShortFlag('m');
        module.setDefault("");
        module.setHelp("[Optional] In case of multi-module project, specify which module (a path from the project's root).");
        module.setStringParser(JSAP.STRING_PARSER);

        FlaggedOption pathToDiff = new FlaggedOption("path-to-diff");
        pathToDiff.setRequired(false);
        pathToDiff.setLongFlag("path-to-diff");
        pathToDiff.setShortFlag('d');
        pathToDiff.setDefault("");
        pathToDiff.setHelp("[Optional] Specify the path of a diff file. If it is not specified, it will be computed using diff command line.");
        pathToDiff.setStringParser(JSAP.STRING_PARSER);

        try {
            jsap.registerParameter(pathDirectoryFirstVersion);
            jsap.registerParameter(pathDirectorySecondVersion);
            jsap.registerParameter(outputPath);
            jsap.registerParameter(outputFormat);
            jsap.registerParameter(module);
            jsap.registerParameter(pathToDiff);
        } catch (JSAPException e) {
            e.printStackTrace();
            usage();
        }

        return jsap;
    }

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static String jsapParameterFormatToMojoParameterFormat(String jsapOptionFormatted) {
        StringBuilder mojoParameterFormat = new StringBuilder();
        for (int i = 0; i < jsapOptionFormatted.length(); i++) {
            if (jsapOptionFormatted.charAt(i) == '-') {
                mojoParameterFormat.append(Character.toUpperCase(jsapOptionFormatted.charAt(++i)));
            } else {
                mojoParameterFormat.append(jsapOptionFormatted.charAt(i));
            }
        }
        return mojoParameterFormat.toString();
    }

    private static String jsapSwitchOptionToMojoParameter(Switch jsapSwitch) {
        return "Boolean " +
                jsapParameterFormatToMojoParameterFormat(jsapSwitch.getLongFlag())
                + ";" + LINE_SEPARATOR;
    }

    private static String jsapFlaggedOptionToMojoParameter(FlaggedOption flaggedOption) {
        String mojoParam = "";
        final String type;
        if (flaggedOption.getStringParser().equals(JSAP.STRING_PARSER)) {
            type = "String";
        } else if (flaggedOption.getStringParser().equals(JSAP.LONG_PARSER)) {
            type = "Long";
        } else {
            type = "Integer";
        }
        if (flaggedOption.isList()) {
            mojoParam += "List<" + type + "> ";
        } else {
            mojoParam += type + " ";
        }
        mojoParam += jsapParameterFormatToMojoParameterFormat(flaggedOption.getLongFlag()) + ";" + LINE_SEPARATOR;
        return mojoParam;
    }

    private static String jsapParameterToMojoParameter(Parameter parameter) {
        String mojoParam = "";
        if (parameter.getHelp() != null) {
            mojoParam += "/**" + LINE_SEPARATOR;
            mojoParam += Arrays.stream(
                    parameter.getHelp().split(LINE_SEPARATOR))
                    .map(" *\t"::concat)
                    .collect(Collectors.joining(LINE_SEPARATOR)) +
                    LINE_SEPARATOR;
            mojoParam += " */" + LINE_SEPARATOR;
        }
        mojoParam += "@Parameter(";
        if (parameter.getDefault() != null) {
            mojoParam += "defaultValue = \"" + parameter.getDefault()[0] + "\", ";
        }
        mojoParam += "property = \"" + ((Flagged) parameter).getLongFlag() + "\")" + LINE_SEPARATOR;
        mojoParam += "private ";
        if (parameter instanceof FlaggedOption) {
            return mojoParam + jsapFlaggedOptionToMojoParameter((FlaggedOption) parameter);
        } else if (parameter instanceof Switch) {
            return mojoParam + jsapSwitchOptionToMojoParameter((Switch) parameter);
        } else {
            System.out.println("Unsupported class: " + parameter.getClass());
            return "";
        }
    }

    /**
     * Main to be used to generate the DSpotMojo properties from the JSAPOptions.
     */
    public static void main(String[] args) {
        final Iterator iterator = options.getIDMap().idIterator();
        while (iterator.hasNext()) {
            final Object id = iterator.next();
            System.out.println(jsapParameterToMojoParameter(options.getByID((String) id)));
        }
    }

}
