package fr.inria.stamp;

import com.martiansoftware.jsap.*;
import eu.stamp.project.testrunner.EntryPoint;
import fr.inria.diversify.dspot.amplifier.*;
import fr.inria.diversify.dspot.selector.ChangeDetectorSelector;
import fr.inria.diversify.dspot.selector.JacocoCoverageSelector;
import fr.inria.diversify.dspot.selector.CloverCoverageSelector;
import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.dspot.selector.ExecutedMutantSelector;
import fr.inria.diversify.dspot.selector.TakeAllSelector;
import fr.inria.diversify.dspot.selector.TestSelector;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/17/17
 */
public class JSAPOptions {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSAPOptions.class);

    public static final JSAP options = initJSAP();

    public enum SelectorEnum {
        PitMutantScoreSelector {
            @Override
            public TestSelector buildSelector() {
                return new PitMutantScoreSelector();
            }
        },
        JacocoCoverageSelector {
            @Override
            public TestSelector buildSelector() {
                return new JacocoCoverageSelector();
            }
        },
        TakeAllSelector {
            @Override
            public TestSelector buildSelector() {
                return new TakeAllSelector();
            }
        },CloverCoverageSelector {
            @Override
            public TestSelector buildSelector() {
                return new CloverCoverageSelector();
            }
        },
        ExecutedMutantSelector {
            @Override
            public TestSelector buildSelector() {
                return new ExecutedMutantSelector();
            }
        },
        ChangeDetectorSelector {
            @Override
            public TestSelector buildSelector() {
                return new ChangeDetectorSelector();
            }
        };
        public abstract TestSelector buildSelector();
    }

    public enum AmplifierEnum {
        MethodAdd(new TestMethodCallAdder()),
        MethodRemove(new TestMethodCallRemover()),
        TestDataMutator(new TestDataMutator()),
        StatementAdd(new StatementAdd()),
        StringLiteralAmplifier(new StringLiteralAmplifier()),
        NumberLiteralAmplifier(new NumberLiteralAmplifier()),
        BooleanLiteralAmplifier(new BooleanLiteralAmplifier()),
        CharLiteralAmplifier(new CharLiteralAmplifier()),
        AllLiteralAmplifiers(new AllLiteralAmplifiers()),
        ReplacementAmplifier(new ReplacementAmplifier()),
        None(null);
        public final Amplifier amplifier;

        private AmplifierEnum(Amplifier amplifier) {
            this.amplifier = amplifier;
        }
    }

    public static Configuration parse(String[] args) {
        JSAPResult jsapConfig = options.parse(args);
        Main.verbose = jsapConfig.getBoolean("verbose");
        EntryPoint.verbose = Main.verbose;
        if (!jsapConfig.success() || jsapConfig.getBoolean("help")) {
            System.err.println();
            for (Iterator<?> errs = jsapConfig.getErrorMessageIterator(); errs.hasNext(); ) {
                System.err.println("Error: " + errs.next());
            }
            showUsage();
        } else if (jsapConfig.getBoolean("example")) {
            return null;
        }

        if (jsapConfig.getString("path") == null) {
            System.err.println("Error: Parameter 'path' is required.");
            showUsage();
        }

        TestSelector testCriterion;
        if (jsapConfig.getString("mutant") != null) {
            if (!"PitMutantScoreSelector".equals(jsapConfig.getString("test-criterion"))) {
                LOGGER.warn("You specify a path to mutations.csv but you did not specified the right test-criterion");
                LOGGER.warn("Forcing the Selector to PitMutantScoreSelector");
            }
            testCriterion = new PitMutantScoreSelector(jsapConfig.getString("mutant"));
        } else {
            testCriterion = SelectorEnum.valueOf(jsapConfig.getString("test-criterion")).buildSelector();
        }

        PitMutantScoreSelector.descartesMode = jsapConfig.getBoolean("descartes");

        DSpotUtils.withComment = jsapConfig.getBoolean("comment");

        return new Configuration(jsapConfig.getString("path"),
                buildAmplifiersFromString(jsapConfig.getStringArray("amplifiers")),
                jsapConfig.getInt("iteration"),
                Arrays.asList(jsapConfig.getStringArray("test")),
                jsapConfig.getString("output"),
                testCriterion,
                Arrays.asList(jsapConfig.getStringArray("testCases")),
                jsapConfig.getLong("seed"),
                jsapConfig.getInt("timeOut"),
                jsapConfig.getString("builder"),
                jsapConfig.getString("mavenHome"),
                jsapConfig.getInt("maxTestAmplified"),
                jsapConfig.getBoolean("clean"),
                !jsapConfig.getBoolean("no-minimize")
        );
    }

    public static Amplifier stringToAmplifier(String amplifier) {
        try {
            return AmplifierEnum.valueOf(amplifier).amplifier;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Wrong values for amplifiers: {} is not recognized", amplifier);
            LOGGER.warn("Possible values are: {}", getPossibleValuesForInputAmplifier());
            LOGGER.warn("No amplifier has been added for {}", amplifier);
            return null;
        }
    }

    @NotNull
    private static String getPossibleValuesForInputAmplifier() {
        return AmplificationHelper.LINE_SEPARATOR + "\t\t - " +
        Arrays.stream(new String[] {
                "StringLiteralAmplifier",
                "NumberLiteralAmplifier",
                "CharLiteralAmplifier",
                "BooleanLiteralAmplifier",
                "AllLiteralAmplifiers",
                "MethodAdd",
                "MethodRemove",
                "TestDataMutator (deprecated)",
                "StatementAdd",
                "ReplacementAmplifier",
                "None"
        }).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR + "\t\t - "));
    }

    public static List<Amplifier> buildAmplifiersFromString(String[] amplifiersAsString) {
        if (amplifiersAsString.length == 0 || "None".equals(amplifiersAsString[0])) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(amplifiersAsString)
                    .map(JSAPOptions::stringToAmplifier)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    private static void showUsage() {
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
        help.setLongFlag("help");
        help.setShortFlag('h');
        help.setHelp("show this help");

        Switch example = new Switch("example");
        example.setLongFlag("example");
        example.setShortFlag('e');
        example.setHelp("run the example of DSpot and leave");

        FlaggedOption pathToConfigFile = new FlaggedOption("path");
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
        amplifiers.setHelp("[optional] specify the list of amplifiers to use. Default with all available amplifiers. " + getPossibleValuesForInputAmplifier());

        FlaggedOption iteration = new FlaggedOption("iteration");
        iteration.setDefault("3");
        iteration.setStringParser(JSAP.INTEGER_PARSER);
        iteration.setShortFlag('i');
        iteration.setLongFlag("iteration");
        iteration.setAllowMultipleDeclarations(false);
        iteration.setHelp("[optional] specify the number of amplification iterations. A larger number may help to improve the test criterion (eg a larger number of iterations may help to kill more mutants). This has an impact on the execution time: the more iterations, the longer DSpot runs.");

        FlaggedOption selector = new FlaggedOption("test-criterion");
        selector.setAllowMultipleDeclarations(false);
        selector.setLongFlag("test-criterion");
        selector.setShortFlag('s');
        selector.setStringParser(JSAP.STRING_PARSER);
        selector.setUsageName("PitMutantScoreSelector | ExecutedMutantSelector | CloverCoverageSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector");
        selector.setHelp("[optional] specify the test adequacy criterion to be maximized with amplification");
        selector.setDefault("PitMutantScoreSelector");

        FlaggedOption specificTestClass = new FlaggedOption("test");
        specificTestClass.setStringParser(JSAP.STRING_PARSER);
        specificTestClass.setShortFlag('t');
        specificTestClass.setList(true);
        specificTestClass.setAllowMultipleDeclarations(false);
        specificTestClass.setLongFlag("test");
        specificTestClass.setDefault("all");
        specificTestClass.setUsageName("my.package.MyClassTest");
        specificTestClass.setHelp("[optional] fully qualified names of test classes to be amplified. If the value is all, DSpot will amplify the whole test suite. You can also use regex to describe a set of test classes.");

        FlaggedOption output = new FlaggedOption("output");
        output.setStringParser(JSAP.STRING_PARSER);
        output.setAllowMultipleDeclarations(false);
        output.setShortFlag('o');
        output.setLongFlag("output-path");
        output.setHelp("[optional] specify the output folder (default: dspot-report)");

        Switch cleanOutput = new Switch("clean");
        cleanOutput.setLongFlag("clean");
        cleanOutput.setHelp("[optional] if enabled, DSpot will remove the out directory if exists, else it will append the results to the exist files. (default: off)");

        FlaggedOption mutantScore = new FlaggedOption("mutant");
        mutantScore.setStringParser(JSAP.STRING_PARSER);
        mutantScore.setAllowMultipleDeclarations(false);
        mutantScore.setShortFlag('m');
        mutantScore.setLongFlag("path-pit-result");
        mutantScore.setUsageName("./path/to/mutations.csv");
        mutantScore.setHelp("[optional, expert mode] specify the path to the .csv of the original result of Pit Test. If you use this option the selector will be forced to PitMutantScoreSelector");

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
        automaticBuilder.setDefault("MavenBuilder");

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

        Switch withComment = new Switch("comment");
        withComment.setLongFlag("with-comment");
        withComment.setDefault("false");
        withComment.setHelp("Enable comment on amplified test: details steps of the Amplification.");

        Switch descartes = new Switch("descartes");
        descartes.setLongFlag("descartes");
        descartes.setDefault("false");
        descartes.setHelp("Enable the descartes engine for Pit Mutant Score Selector.");

        Switch nominimize = new Switch("no-minimize");
        nominimize.setLongFlag("no-minimize");
        nominimize.setDefault("false");
        nominimize.setHelp("Disable the minimization of amplified tests.");

        try {
            jsap.registerParameter(pathToConfigFile);
            jsap.registerParameter(amplifiers);
            jsap.registerParameter(iteration);
            jsap.registerParameter(selector);
            jsap.registerParameter(maxTestAmplified);
            jsap.registerParameter(specificTestClass);
            jsap.registerParameter(testCases);
            jsap.registerParameter(output);
            jsap.registerParameter(cleanOutput);
            jsap.registerParameter(mutantScore);
            jsap.registerParameter(descartes);
            jsap.registerParameter(automaticBuilder);
            jsap.registerParameter(mavenHome);
            jsap.registerParameter(seed);
            jsap.registerParameter(timeOut);
            jsap.registerParameter(verbose);
            jsap.registerParameter(withComment);
            jsap.registerParameter(nominimize);
            jsap.registerParameter(example);
            jsap.registerParameter(help);
        } catch (JSAPException e) {
            throw new RuntimeException(e);
        }

        return jsap;
    }

}