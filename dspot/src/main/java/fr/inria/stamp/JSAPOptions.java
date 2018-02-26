package fr.inria.stamp;

import com.martiansoftware.jsap.*;
import fr.inria.diversify.dspot.amplifier.*;
import fr.inria.diversify.dspot.selector.ChangeDetectorSelector;
import fr.inria.diversify.dspot.selector.JacocoCoverageSelector;
import fr.inria.diversify.dspot.selector.CloverCoverageSelector;
import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.dspot.selector.ExecutedMutantSelector;
import fr.inria.diversify.dspot.selector.TakeAllSelector;
import fr.inria.diversify.dspot.selector.TestSelector;
import fr.inria.diversify.mutant.pit.GradlePitTaskAndOptions;
import fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions;
import fr.inria.stamp.test.runner.TestRunnerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        MethodAdd(Collections.singletonList(new TestMethodCallAdder())),
        MethodRemove(Collections.singletonList(new TestMethodCallRemover())),
        TestDataMutator(Collections.singletonList(new TestDataMutator())),
        StatementAdd(Collections.singletonList(new StatementAdd())),
        StringLiteralAmplifier(Collections.singletonList(new StringLiteralAmplifier())),
        NumberLiteralAmplifier(Collections.singletonList(new NumberLiteralAmplifier())),
        BooleanLiteralAmplifier(Collections.singletonList(new BooleanLiteralAmplifier())),
        CharLiteralAmplifier(Collections.singletonList(new CharLiteralAmplifier())),
        AllLiteralAmplifiers(Arrays.asList(new StringLiteralAmplifier(),
                new NumberLiteralAmplifier(),
                new BooleanLiteralAmplifier(),
                new CharLiteralAmplifier())
        ),
        None(null);
        public final List<Amplifier> amplifiers;

        private AmplifierEnum(List<Amplifier> amplifiers) {
            this.amplifiers = amplifiers;
        }
    }

    public static Configuration parse(String[] args) {
        JSAPResult jsapConfig = options.parse(args);
        Main.verbose = jsapConfig.getBoolean("verbose");
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

        MavenPitCommandAndOptions.descartesMode = jsapConfig.getBoolean("descartes");
        MavenPitCommandAndOptions.evosuiteMode = jsapConfig.getBoolean("evosuite");

        GradlePitTaskAndOptions.descartesMode = jsapConfig.getBoolean("descartes");
        GradlePitTaskAndOptions.evosuiteMode = jsapConfig.getBoolean("evosuite");

        TestRunnerFactory.useReflectiveTestRunner = false;

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
                jsapConfig.getBoolean("clean")
        );
    }

    public static Stream<Amplifier> stringToAmplifier(String amplifier) {
        try {
            return AmplifierEnum.valueOf(amplifier).amplifiers.stream();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Wrong values for amplifiers: {} is not recognized", amplifier);
            LOGGER.warn("Possible values are: StringLiteralAmplifier | NumberLiteralAmplifier | CharLiteralAmplifier | BooleanLiteralAmplifier | AllLiteralAmplifiers | MethodAdd | MethodRemove | TestDataMutator | StatementAdd | None");
            LOGGER.warn("No amplifier has been added for {}", amplifier);
            return Stream.of();
        }
    }

    public static List<Amplifier> buildAmplifiersFromString(String[] amplifiersAsString) {
        if (amplifiersAsString.length == 0 || "None".equals(amplifiersAsString[0])) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(amplifiersAsString)
                    .flatMap(JSAPOptions::stringToAmplifier)
                    .collect(Collectors.toList());
        }
    }

    private static void showUsage() {
        System.err.println();
        System.err.println("Usage: java -jar target/dspot-1.0.0-jar-with-dependencies.jar");
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
        help.setHelp("shows this help");

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
        amplifiers.setHelp("[optional] specify the list of amplifiers to use. Default with all available amplifiers. Possible values: StringLiteralAmplifier | NumberLiteralAmplifier | CharLiteralAmplifier | BooleanLiteralAmplifier | AllLiteralAmplifiers | MethodAdd | MethodRemove | TestDataMutator | StatementAdd | None");

        FlaggedOption iteration = new FlaggedOption("iteration");
        iteration.setDefault("3");
        iteration.setStringParser(JSAP.INTEGER_PARSER);
        iteration.setShortFlag('i');
        iteration.setLongFlag("iteration");
        iteration.setAllowMultipleDeclarations(false);
        iteration.setHelp("[optional] specify the number of amplification iteration. A larger number may help to improve the test criterion (eg a larger number of iterations mah help to kill more mutants). This has an impact on the execution time: the more iterations, the longer DSpot runs.");

        FlaggedOption selector = new FlaggedOption("test-criterion");
        selector.setAllowMultipleDeclarations(false);
        selector.setLongFlag("test-criterion");
        selector.setShortFlag('s');
        selector.setStringParser(JSAP.STRING_PARSER);
        selector.setUsageName("PitMutantScoreSelector | ExecutedMutantSelector | CloverCoverageSelector | JacocoCoverageSelector | TakeAllSelector | ChangeDetectorSelector");
        selector.setHelp("[optional] specify the test adequacy criterion to be maximized with amplification");
        selector.setDefault("PitMutantScoreSelector");

        FlaggedOption specificTestCase = new FlaggedOption("test");
        specificTestCase.setStringParser(JSAP.STRING_PARSER);
        specificTestCase.setShortFlag('t');
        specificTestCase.setList(true);
        specificTestCase.setAllowMultipleDeclarations(false);
        specificTestCase.setLongFlag("test");
        specificTestCase.setDefault("all");
        specificTestCase.setUsageName("my.package.MyClassTest");
        specificTestCase.setHelp("[optional] fully qualified names of test classes to be amplified. If the value is all, DSpot will amplify the whole test suite. You can also use regex to describe a set of test classes.");

        FlaggedOption output = new FlaggedOption("output");
        output.setStringParser(JSAP.STRING_PARSER);
        output.setAllowMultipleDeclarations(false);
        output.setShortFlag('o');
        output.setLongFlag("output-path");
        output.setHelp("[optional] specify the output folder (default: dspot-report)");

        Switch cleanOutput = new Switch("clean");
        cleanOutput.setShortFlag('q');
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
        seed.setShortFlag('r');
        seed.setUsageName("long integer");
        seed.setHelp("specify a seed for the random object (used for all randomized operation)");
        seed.setDefault("23");

        FlaggedOption timeOut = new FlaggedOption("timeOut");
        timeOut.setStringParser(JSAP.INTEGER_PARSER);
        timeOut.setLongFlag("timeOut");
        timeOut.setShortFlag('v');
        timeOut.setUsageName("long integer");
        timeOut.setHelp("specify the timeout value of the degenerated tests in millisecond");
        timeOut.setDefault("10000");

        FlaggedOption automaticBuilder = new FlaggedOption("builder");
        automaticBuilder.setStringParser(JSAP.STRING_PARSER);
        automaticBuilder.setLongFlag("automatic-builder");
        automaticBuilder.setShortFlag('b');
        automaticBuilder.setUsageName("MavenBuilder | GradleBuilder");
        automaticBuilder.setHelp("[optional] specify the automatic builder to build the project");
        automaticBuilder.setDefault("MavenBuilder");

        FlaggedOption mavenHome = new FlaggedOption("mavenHome");
        mavenHome.setStringParser(JSAP.STRING_PARSER);
        mavenHome.setLongFlag("maven-home");
        mavenHome.setShortFlag('j');
        mavenHome.setUsageName("path to maven home");
        mavenHome.setHelp("specify the path to the maven home");

        Switch descartesMode = new Switch("descartes");
        descartesMode.setShortFlag('d');
        descartesMode.setLongFlag("descartes");

        Switch evosuiteMode = new Switch("evosuite");
        evosuiteMode.setShortFlag('k');
        evosuiteMode.setLongFlag("evosuite");

        Switch verbose = new Switch("verbose");
        verbose.setLongFlag("verbose");
        verbose.setDefault("false");

        FlaggedOption maxTestAmplified = new FlaggedOption("maxTestAmplified");
        maxTestAmplified.setStringParser(JSAP.INTEGER_PARSER);
        maxTestAmplified.setLongFlag("max-test-amplified");
        maxTestAmplified.setShortFlag('g');
        maxTestAmplified.setUsageName("integer");
        maxTestAmplified.setHelp("[optional] specify the maximum number of amplified test that dspot keep (before generating assertion)");
        maxTestAmplified.setDefault("200");

        try {
            jsap.registerParameter(pathToConfigFile);
            jsap.registerParameter(amplifiers);
            jsap.registerParameter(iteration);
            jsap.registerParameter(selector);
            jsap.registerParameter(maxTestAmplified);
            jsap.registerParameter(descartesMode);
            jsap.registerParameter(evosuiteMode);
            jsap.registerParameter(specificTestCase);
            jsap.registerParameter(testCases);
            jsap.registerParameter(output);
            jsap.registerParameter(cleanOutput);
            jsap.registerParameter(mutantScore);
            jsap.registerParameter(automaticBuilder);
            jsap.registerParameter(mavenHome);
            jsap.registerParameter(seed);
            jsap.registerParameter(timeOut);
            jsap.registerParameter(verbose);
            jsap.registerParameter(example);
            jsap.registerParameter(help);
        } catch (JSAPException e) {
            throw new RuntimeException(e);
        }

        return jsap;
    }

}