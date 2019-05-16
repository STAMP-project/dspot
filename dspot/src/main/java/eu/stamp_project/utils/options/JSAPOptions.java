package eu.stamp_project.utils.options;

import com.martiansoftware.jsap.Flagged;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;
import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.utils.options.check.Checker;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/17/17
 */
public class JSAPOptions {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSAPOptions.class);

    private static final String SEPARATOR = AmplificationHelper.LINE_SEPARATOR + "\t\t - ";

    public static final JSAP options = initJSAP();

    /**
     * parse the command line argument
     *
     * @param args command line arguments. Refer to the README on the github page or use --help command line option to display all the accepted arguments.
     * @return true if --example is pass through the command line. It will run DSpot with a small example to make a demonstration of the usage of DSpot.
     * Otherwise, it returns false and DSpot will run normally, using the properties and the command line options.
     */
    public static boolean parse(String[] args) {
        JSAPResult jsapConfig = options.parse(args);
        if (!jsapConfig.success() || jsapConfig.getBoolean("help")) {
            System.err.println();
            for (Iterator<?> errs = jsapConfig.getErrorMessageIterator(); errs.hasNext(); ) {
                System.err.println("Error: " + errs.next());
            }
            showUsage();
        } else if (jsapConfig.getBoolean("example")) {
            return true;
        }

        // checking path to properties
        Checker.checkPathToPropertiesValue(jsapConfig);

        // checking enum values
        final List<String> amplifiers = new ArrayList<>(Arrays.asList(jsapConfig.getStringArray("amplifiers")));
        final String selector = jsapConfig.getString("test-criterion");
        final String budgetizer = jsapConfig.getString("budgetizer");
        Checker.checkEnum(AmplifierEnum.class, amplifiers, "amplifiers");
        Checker.checkEnum(SelectorEnum.class, selector, "test-criterion");
        Checker.checkEnum(BudgetizerEnum.class, budgetizer, "budgetizer");

        // pit output format
        PitMutantScoreSelector.OutputFormat consecutiveFormat;
        if (jsapConfig.getString("pit-output-format").toLowerCase().equals("xml")) {
            consecutiveFormat = PitMutantScoreSelector.OutputFormat.XML;
        } else if (jsapConfig.getString("pit-output-format").toLowerCase().equals("csv")) {
            consecutiveFormat = PitMutantScoreSelector.OutputFormat.CSV;
        } else {
            LOGGER.warn("You specified an invalid format. Forcing the Pit output format to XML.");
            consecutiveFormat = PitMutantScoreSelector.OutputFormat.XML;
        }

        // expert test selector mode
        TestSelector testCriterion;
        if (jsapConfig.getString("mutant") != null) {
            if (!"PitMutantScoreSelector".equals(jsapConfig.getString("test-criterion"))) {
                LOGGER.warn("You specified a path to a mutations file but you did not specify the right test-criterion");
                LOGGER.warn("Forcing the Selector to PitMutantScoreSelector");
            }
            String pathToOriginalResultOfPit = jsapConfig.getString("mutant");
            PitMutantScoreSelector.OutputFormat originalFormat;
            if (pathToOriginalResultOfPit.toLowerCase().endsWith(".xml")) {
                originalFormat = PitMutantScoreSelector.OutputFormat.XML;
            } else if (pathToOriginalResultOfPit.toLowerCase().endsWith(".csv")) {
                originalFormat = PitMutantScoreSelector.OutputFormat.CSV;
            } else {
                LOGGER.warn("You specified the wrong Pit format. Skipping expert mode.");
                originalFormat = PitMutantScoreSelector.OutputFormat.XML;
            }
            testCriterion = new PitMutantScoreSelector(jsapConfig.getString("mutant"), originalFormat, consecutiveFormat);

            // default test selector mode
        } else {
            if (!(jsapConfig.getString("output-format") == null)) {
                if (!"PitMutantScoreSelector".equals(jsapConfig.getString("test-criterion"))) {
                    LOGGER.warn("You specified an output format but you did not specify the right test-criterion");
                    LOGGER.warn("Forcing the Selector to PitMutantScoreSelector");
                }
                testCriterion = new PitMutantScoreSelector(consecutiveFormat);
            } else {
                testCriterion = SelectorEnum.valueOf(jsapConfig.getString("test-criterion")).buildSelector();
            }
        }

        // these values need to be checked when the factory is available
        // We check them in DSpot class since we have the codes that allow to check them easily
        // and thus, the Factory will be created.
        // Anyway, the verification in DSpot is not yet too late nor deep in the amplification's process.
        final List<String> testClasses = Arrays.asList(jsapConfig.getStringArray("test"));
        final List<String> testCases = Arrays.asList(jsapConfig.getStringArray("testCases"));

        // we check the properties before initializing the InputConfiguration.
        final Properties properties = InputConfiguration.loadProperties(jsapConfig.getString("path-to-properties"));
        Checker.checkProperties(properties);

        InputConfiguration.initialize(properties, jsapConfig.getString("builder"));
        if (InputConfiguration.get().getOutputDirectory().isEmpty()) {
            InputConfiguration.get().setOutputDirectory(jsapConfig.getString("output"));
        }

        // we check now the binaries folders after the compilation
        Checker.checkBinariesFolders(properties);

        InputConfiguration.get()
                .setAmplifiers(AmplifierEnum.buildAmplifiersFromString(amplifiers))
                .setNbIteration(jsapConfig.getInt("iteration"))
                .setTestClasses(testClasses)
                .setSelector(testCriterion)
                .setTestCases(testCases)
                .setSeed(jsapConfig.getLong("seed"))
                .setTimeOutInMs(jsapConfig.getInt("timeOut"))
                .setMaxTestAmplified(jsapConfig.getInt("maxTestAmplified"))
                .setBudgetizer(BudgetizerEnum.valueOf(budgetizer))
                .setClean(jsapConfig.getBoolean("clean"))
                .setMinimize(!jsapConfig.getBoolean("no-minimize"))
                .setVerbose(jsapConfig.getBoolean("verbose"))
                .setUseWorkingDirectory(jsapConfig.getBoolean("working-directory"))
                .setWithComment(jsapConfig.getBoolean("comment"))
                .setGenerateAmplifiedTestClass(jsapConfig.getBoolean("generate-new-test-class"))
                .setKeepOriginalTestMethods(jsapConfig.getBoolean("keep-original-test-methods"))
                .setDescartesMode(jsapConfig.getBoolean("descartes") && !jsapConfig.getBoolean("gregor"))
                .setUseMavenToExecuteTest(jsapConfig.getBoolean("use-maven-to-exe-test"))
                .setTargetOneTestClass(jsapConfig.getBoolean("targetOneTestClass"))
                .setAllowPathInAssertion(jsapConfig.getBoolean("allow-path-in-assertions"));
        return false;
    }

    private static String helpForEnums(Class<?> enumClass) {
        return AmplificationHelper.LINE_SEPARATOR + "Possible values are: " +
                SEPARATOR + String.join(SEPARATOR, Checker.getPossibleValues(enumClass))
                + AmplificationHelper.LINE_SEPARATOR;
    }

    public static void showUsage() {
        System.err.println();
        System.err.println("Usage: java -jar target/dspot-<version>-jar-with-dependencies.jar");
        System.err.println("                          " + options.getUsage());
        System.err.println();
        System.err.println(options.getHelp());
        System.exit(1);
    }

    private static JSAP initJSAP() {
        JSAP jsap = new JSAP();

        Switch help = new Switch("help");
        help.setDefault("false");
        help.setLongFlag("help");
        help.setShortFlag('h');
        help.setHelp("show this help");

        Switch example = new Switch("example");
        example.setDefault("false");
        example.setLongFlag("example");
        example.setShortFlag('e');
        example.setHelp("run the example of DSpot and leave");

        FlaggedOption pathToConfigFile = new FlaggedOption("path-to-properties");
        pathToConfigFile.setAllowMultipleDeclarations(false);
        pathToConfigFile.setLongFlag("path-to-properties");
        pathToConfigFile.setShortFlag('p');
        pathToConfigFile.setStringParser(JSAP.STRING_PARSER);
        pathToConfigFile.setUsageName("./path/to/myproject.properties");
        pathToConfigFile.setHelp("[mandatory] specify the path to the configuration file (format Java properties) of the target project (e.g. ./foo.properties).");

        FlaggedOption amplifiers = new FlaggedOption("amplifiers");
        amplifiers.setList(true);
        amplifiers.setLongFlag("amplifiers");
        amplifiers.setShortFlag('a');
        amplifiers.setStringParser(JSAP.STRING_PARSER);
        amplifiers.setUsageName("Amplifier");
        amplifiers.setDefault("None");
        amplifiers.setHelp("[optional] specify the list of amplifiers to use. By default, DSpot does not use any amplifiers (None) and applies only assertion amplification." + JSAPOptions.helpForEnums(AmplifierEnum.class));

        FlaggedOption iteration = new FlaggedOption("iteration");
        iteration.setDefault("3");
        iteration.setStringParser(JSAP.INTEGER_PARSER);
        iteration.setShortFlag('i');
        iteration.setLongFlag("iteration");
        iteration.setAllowMultipleDeclarations(false);
        iteration.setHelp("[optional] specify the number of amplification iterations. A larger number may help to improve the test criterion (e.g. a larger number of iterations may help to kill more mutants). This has an impact on the execution time: the more iterations, the longer DSpot runs.");

        FlaggedOption selector = new FlaggedOption("test-criterion");
        selector.setAllowMultipleDeclarations(false);
        selector.setLongFlag("test-criterion");
        selector.setShortFlag('s');
        selector.setStringParser(JSAP.STRING_PARSER);
        selector.setUsageName("PitMutantScoreSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector");
        selector.setHelp("[optional] specify the test adequacy criterion to be maximized with amplification." + JSAPOptions.helpForEnums(SelectorEnum.class));
        selector.setDefault("PitMutantScoreSelector");

        FlaggedOption pitOutputFormat = new FlaggedOption("pit-output-format");
        pitOutputFormat.setAllowMultipleDeclarations(false);
        pitOutputFormat.setLongFlag("pit-output-format");
        pitOutputFormat.setStringParser(JSAP.STRING_PARSER);
        pitOutputFormat.setUsageName("XML | CSV");
        pitOutputFormat.setHelp("[optional] specify the Pit output format." + JSAPOptions.helpForEnums(PitMutantScoreSelector.OutputFormat.class));
        pitOutputFormat.setDefault("XML");

        FlaggedOption specificTestClass = new FlaggedOption("test");
        specificTestClass.setStringParser(JSAP.STRING_PARSER);
        specificTestClass.setShortFlag('t');
        specificTestClass.setList(true);
        specificTestClass.setAllowMultipleDeclarations(false);
        specificTestClass.setLongFlag("test");
        specificTestClass.setDefault("all");
        specificTestClass.setUsageName("my.package.MyClassTest | all");
        specificTestClass.setHelp("[optional] fully qualified names of test classes to be amplified. If the value is all, DSpot will amplify the whole test suite. You can also use regex to describe a set of test classes. By default, DSpot selects all the tests (value all).");

        FlaggedOption output = new FlaggedOption("output");
        output.setStringParser(JSAP.STRING_PARSER);
        output.setAllowMultipleDeclarations(false);
        output.setShortFlag('o');
        output.setLongFlag("output-path");
        output.setDefault("target/dspot/output");
        output.setHelp("[optional] specify the output folder");

        Switch cleanOutput = new Switch("clean");
        cleanOutput.setLongFlag("clean");
        cleanOutput.setDefault("false");
        cleanOutput.setHelp("[optional] if enabled, DSpot will remove the out directory if exists, else it will append the results to the exist files.");

        FlaggedOption mutantScore = new FlaggedOption("mutant");
        mutantScore.setStringParser(JSAP.STRING_PARSER);
        mutantScore.setAllowMultipleDeclarations(false);
        mutantScore.setShortFlag('m');
        mutantScore.setLongFlag("path-pit-result");
        mutantScore.setUsageName("./path/to/mutations.csv");
        mutantScore.setHelp("[optional, expert mode] specify the path to the .xml or .csv of the original result of Pit Test. If you use this option the selector will be forced to PitMutantScoreSelector");

        Switch targetOneTestClass = new Switch("targetOneTestClass");
        targetOneTestClass.setLongFlag("targetOneTestClass");
        targetOneTestClass.setHelp("[optional, expert] enable this option will make DSpot computing the mutation score of only one test class (the first pass through --test command line option)");
        targetOneTestClass.setDefault("false");


        FlaggedOption testCases = new FlaggedOption("testCases");
        testCases.setList(true);
        testCases.setAllowMultipleDeclarations(false);
        testCases.setLongFlag("cases");
        testCases.setShortFlag('c');
        testCases.setStringParser(JSAP.STRING_PARSER);
        testCases.setHelp("specify the test cases to amplify");

        FlaggedOption seed = new FlaggedOption("seed");
        seed.setStringParser(JSAP.LONG_PARSER);
        seed.setLongFlag("randomSeed");
        seed.setUsageName("long integer");
        seed.setHelp("specify a seed for the random object (used for all randomized operation)");
        seed.setDefault("23");

        FlaggedOption timeOut = new FlaggedOption("timeOut");
        timeOut.setStringParser(JSAP.INTEGER_PARSER);
        timeOut.setLongFlag("timeOut");
        timeOut.setUsageName("long integer");
        timeOut.setHelp("specify the timeout value of the degenerated tests in millisecond");
        timeOut.setDefault("10000");

        FlaggedOption automaticBuilder = new FlaggedOption("builder");
        automaticBuilder.setStringParser(JSAP.STRING_PARSER);
        automaticBuilder.setLongFlag("automatic-builder");
        automaticBuilder.setUsageName("MavenBuilder | GradleBuilder");
        automaticBuilder.setHelp("[optional] specify the automatic builder to build the project");
        automaticBuilder.setDefault("");

        FlaggedOption mavenHome = new FlaggedOption("mavenHome");
        mavenHome.setStringParser(JSAP.STRING_PARSER);
        mavenHome.setLongFlag("maven-home");
        mavenHome.setUsageName("path to maven home");
        mavenHome.setHelp("specify the path to the maven home");

        Switch verbose = new Switch("verbose");
        verbose.setLongFlag("verbose");
        verbose.setDefault("false");
        verbose.setHelp("Enable verbose mode of DSpot.");

        FlaggedOption maxTestAmplified = new FlaggedOption("maxTestAmplified");
        maxTestAmplified.setStringParser(JSAP.INTEGER_PARSER);
        maxTestAmplified.setLongFlag("max-test-amplified");
        maxTestAmplified.setUsageName("integer");
        maxTestAmplified.setHelp("[optional] specify the maximum number of amplified tests that dspot keeps (before generating assertion)");
        maxTestAmplified.setDefault("200");

        FlaggedOption budgetizer = new FlaggedOption("budgetizer");
        budgetizer.setStringParser(JSAP.STRING_PARSER);
        budgetizer.setLongFlag("budgetizer");
        budgetizer.setUsageName("RandomBudgetizer | TextualDistanceBudgetizer | SimpleBudgetizer");
        budgetizer.setHelp("[optional] specify a Bugdetizer." + JSAPOptions.helpForEnums(BudgetizerEnum.class));
        budgetizer.setDefault("RandomBudgetizer");

        Switch withComment = new Switch("comment");
        withComment.setLongFlag("with-comment");
        withComment.setDefault("false");
        withComment.setHelp("Enable comment on amplified test: details steps of the Amplification.");

        Switch descartes = new Switch("descartes");
        descartes.setLongFlag("descartes");
        descartes.setDefault("true");
        descartes.setHelp("Enable the descartes engine for Pit Mutant Score Selector.");

        Switch gregor = new Switch("gregor");
        gregor.setLongFlag("gregor");
        gregor.setDefault("false");
        gregor.setHelp("Enable the gregor engine for Pit Mutant Score Selector.");

        Switch nominimize = new Switch("no-minimize");
        nominimize.setLongFlag("no-minimize");
        nominimize.setDefault("false");
        nominimize.setHelp("Disable the minimization of amplified tests.");

        Switch useWorkingDirectory = new Switch("working-directory");
        useWorkingDirectory.setLongFlag("working-directory");
        useWorkingDirectory.setDefault("false");
        useWorkingDirectory.setHelp("Enable this option to change working directory with the root of the project.");

        Switch generateNewTestClass = new Switch("generate-new-test-class");
        generateNewTestClass.setLongFlag("generate-new-test-class");
        generateNewTestClass.setDefault("false");
        generateNewTestClass.setHelp("Enable the creation of a new test class.");

        Switch keepOriginalTestMethods = new Switch("keep-original-test-methods");
        keepOriginalTestMethods.setLongFlag("keep-original-test-methods");
        keepOriginalTestMethods.setDefault("false");
        keepOriginalTestMethods.setHelp("If enabled, DSpot keeps original test methods of the amplified test class.");

        Switch useMavenToExecuteTests = new Switch("use-maven-to-exe-test");
        useMavenToExecuteTests.setLongFlag("use-maven-to-exe-test");
        useMavenToExecuteTests.setDefault("false");
        useMavenToExecuteTests.setHelp("If enabled, DSpot will use maven to execute the tests.");

        Switch allowPathInAssertions = new Switch("allow-path-in-assertions");
        allowPathInAssertions.setLongFlag("allow-path-in-assertions");
        allowPathInAssertions.setDefault("false");
        allowPathInAssertions.setHelp("If enabled, DSpot will generate assertions for values that seems like to be paths.");

        try {
            jsap.registerParameter(pathToConfigFile);
            jsap.registerParameter(amplifiers);
            jsap.registerParameter(iteration);
            jsap.registerParameter(selector);
            jsap.registerParameter(pitOutputFormat);
            jsap.registerParameter(budgetizer);
            jsap.registerParameter(maxTestAmplified);
            jsap.registerParameter(specificTestClass);
            jsap.registerParameter(testCases);
            jsap.registerParameter(output);
            jsap.registerParameter(cleanOutput);
            jsap.registerParameter(mutantScore);
            jsap.registerParameter(targetOneTestClass);
            jsap.registerParameter(descartes);
            jsap.registerParameter(gregor);
            jsap.registerParameter(automaticBuilder);
            jsap.registerParameter(mavenHome);
            jsap.registerParameter(seed);
            jsap.registerParameter(timeOut);
            jsap.registerParameter(verbose);
            jsap.registerParameter(withComment);
            jsap.registerParameter(nominimize);
            jsap.registerParameter(useWorkingDirectory);
            jsap.registerParameter(generateNewTestClass);
            jsap.registerParameter(keepOriginalTestMethods);
            jsap.registerParameter(useMavenToExecuteTests);
            jsap.registerParameter(allowPathInAssertions);
            jsap.registerParameter(example);
            jsap.registerParameter(help);
        } catch (JSAPException e) {
            throw new RuntimeException(e);
        }
        return jsap;
    }

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
                + ";" + AmplificationHelper.LINE_SEPARATOR;
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
        mojoParam += jsapParameterFormatToMojoParameterFormat(flaggedOption.getLongFlag()) + ";" + AmplificationHelper.LINE_SEPARATOR;
        return mojoParam;
    }

    private static String jsapParameterToMojoParameter(Parameter parameter) {
        String mojoParam = "";
        if (parameter.getHelp() != null) {
            mojoParam += "/**" + AmplificationHelper.LINE_SEPARATOR;
            mojoParam += Arrays.stream(
                    parameter.getHelp().split(AmplificationHelper.LINE_SEPARATOR))
                    .map(" *\t"::concat)
                    .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)) +
                    AmplificationHelper.LINE_SEPARATOR;
            mojoParam += " */" + AmplificationHelper.LINE_SEPARATOR;
        }
        mojoParam += "@Parameter(";
        if (parameter.getDefault() != null) {
            mojoParam += "defaultValue = \"" + parameter.getDefault()[0] + "\", ";
        }
        mojoParam += "property = \"" + ((Flagged) parameter).getLongFlag() + "\")" + AmplificationHelper.LINE_SEPARATOR;
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
