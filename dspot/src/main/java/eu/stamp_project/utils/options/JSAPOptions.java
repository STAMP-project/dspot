package eu.stamp_project.utils.options;

import com.martiansoftware.jsap.Flagged;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;
import eu.stamp_project.utils.collector.CollectorConfig;
import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.utils.options.check.Checker;
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

    static final Logger LOGGER = LoggerFactory.getLogger(JSAPOptions.class);

    private static final String SEPARATOR = AmplificationHelper.LINE_SEPARATOR + "\t\t - ";

    public static final JSAP options = initJSAP();

    /**
     * parse the command line argument
     * @param args command line arguments. Refer to the README on the github page or use --help command line option to display all the accepted arguments.
     *             Otherwise, it returns false and DSpot will run normally, using the properties and the command line options.
     */
    public static void parse(String[] args) {
        JSAPResult jsapConfig = options.parse(args);
        if (!jsapConfig.success() || jsapConfig.getBoolean("help")) {
            System.err.println();
            for (Iterator<?> errs = jsapConfig.getErrorMessageIterator(); errs.hasNext(); ) {
                System.err.println("Error: " + errs.next());
            }
            showUsage();
        } else if (jsapConfig.getBoolean("example")) {
            Configuration.configureExample();
            return;
        }

        // getting first all the values of the command line.
        final String pathToProperties = jsapConfig.getString("path-to-properties");
        final List<String> amplifiers = new ArrayList<>(Arrays.asList(jsapConfig.getStringArray("amplifiers")));
        final String testCriterion = jsapConfig.getString("test-criterion");
        final String inputAmplDistributor = jsapConfig.getString("input-ampl-distributor");
        final String pitOutputFormat = jsapConfig.getString("pit-output-format");
        final String pathPitResult = jsapConfig.getString("path-pit-result");
        final String automaticBuilder = jsapConfig.getString("automatic-builder");
        final String outputPath = jsapConfig.getString("output-path");
        final int iteration = jsapConfig.getInt("iteration");
        final long randomSeed = jsapConfig.getLong("random-seed");
        final int timeOut = jsapConfig.getInt("time-out");
        final int maxTestAmplified = jsapConfig.getInt("max-test-amplified");
        final boolean clean = jsapConfig.getBoolean("clean");
        final boolean verbose = jsapConfig.getBoolean("verbose");
        final boolean workingDirectory = jsapConfig.getBoolean("working-directory");
        final boolean withComment = jsapConfig.getBoolean("with-comment");
        final boolean generateNewTestClass = jsapConfig.getBoolean("generate-new-test-class");
        final boolean keepOriginalTestMethods = jsapConfig.getBoolean("keep-original-test-methods");
        final boolean gregor = jsapConfig.getBoolean("gregor");
        final boolean descartes = jsapConfig.getBoolean("descartes");
        final boolean useMavenToExeTest = jsapConfig.getBoolean("use-maven-to-exe-test");
        final boolean targetOneTestClass = jsapConfig.getBoolean("target-one-test-class");
        final boolean allowPathInAssertions = jsapConfig.getBoolean("allow-path-in-assertions");
        final int executeTestParallelWithNumberProcessors =
                jsapConfig.getInt("execute-test-parallel-with-number-processors") != 0 ?
                        jsapConfig.getInt("execute-test-parallel-with-number-processors") : Runtime.getRuntime().availableProcessors();
        final boolean executeTestsInParallel = jsapConfig.userSpecified("execute-test-parallel-with-number-processors");
        final String fullClasspath = jsapConfig.getString("full-classpath");
        final String collector = jsapConfig.getString("collector");
        final String mongoUrl = jsapConfig.getString("mongo-url");
        final String mongoDbname = jsapConfig.getString("mongo-dbname");
        final String mongoColname = jsapConfig.getString("mongo-colname");
        final String repoSlug = jsapConfig.getString("repo-slug");
        final String repoBranch = jsapConfig.getString("repo-branch");
        final boolean restful = jsapConfig.getBoolean("restful");
        final String smtpUsername = jsapConfig.getString("smtp-username");
        final String smtpPassword = jsapConfig.getString("smtp-password");
        final String smtpHost = jsapConfig.getString("smtp-host");
        final String smtpPort = jsapConfig.getString("smtp-port");
        final boolean smtpAuth = jsapConfig.getBoolean("smtp-auth");
        final boolean smtpTls = jsapConfig.getBoolean("smtp-tls");

        // these values need to be checked when the factory is available
        // We check them in DSpot class since we have the codes that allow to check them easily
        // and thus, the Factory will be created.
        // Anyway, the verification in DSpot is not yet too late nor deep in the amplification's process.
        final List<String> test = Arrays.asList(jsapConfig.getStringArray("test"));
        final List<String> testCases = Arrays.asList(jsapConfig.getStringArray("test-cases"));

        Configuration.configure(
                pathToProperties,
                amplifiers,
                testCriterion,
                inputAmplDistributor,
                pitOutputFormat,
                pathPitResult,
                automaticBuilder,
                outputPath,
                iteration,
                randomSeed,
                timeOut,
                maxTestAmplified,
                clean,
                verbose,
                workingDirectory,
                withComment,
                generateNewTestClass,
                keepOriginalTestMethods,
                gregor,
                descartes,
                useMavenToExeTest,
                targetOneTestClass,
                allowPathInAssertions,
                executeTestsInParallel,
                executeTestParallelWithNumberProcessors,
                test,
                testCases,
                fullClasspath,
                collector,
                mongoUrl,
                mongoDbname,
                mongoColname,
                repoSlug,
                repoBranch,
                restful,
                smtpUsername,
                smtpPassword,
                smtpHost,
                smtpPort,
                smtpAuth,
                smtpTls
        );

        CollectorConfig.getInformationCollector().reportInitInformation(jsapConfig);
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

        FlaggedOption collector = new FlaggedOption("collector");
        collector.setStringParser(JSAP.STRING_PARSER);
        collector.setLongFlag("collector");
        collector.setUsageName("NullCollector | MongodbCollector");
        collector.setHelp("[optional] set a collector: MongodbCollector to send info to Mongodb at end process, NullCollector which does nothing.");
        collector.setDefault("NullCollector");

        FlaggedOption mongoUrl = new FlaggedOption("mongo-url");
        mongoUrl.setLongFlag("mongo-url");
        mongoUrl.setDefault("mongodb://localhost:27017");
        mongoUrl.setRequired(false);
        mongoUrl.setStringParser(JSAP.STRING_PARSER);
        mongoUrl.setAllowMultipleDeclarations(false);
        mongoUrl.setHelp("[optional] If valid url, DSpot will submit to Mongodb database. For default use mongodb://localhost:27017");

        FlaggedOption mongoDbname = new FlaggedOption("mongo-dbname");
        mongoDbname.setLongFlag("mongo-dbname");
        mongoDbname.setDefault("Dspot");
        mongoDbname.setRequired(false);
        mongoDbname.setStringParser(JSAP.STRING_PARSER);
        mongoDbname.setAllowMultipleDeclarations(false);
        mongoDbname.setHelp("[optional] If valid mongo-url provided, DSpot will submit to the provided database name.");

        FlaggedOption mongoColname = new FlaggedOption("mongo-colname");
        mongoColname.setLongFlag("mongo-colname");
        mongoColname.setDefault("AmpRecords");
        mongoColname.setRequired(false);
        mongoColname.setStringParser(JSAP.STRING_PARSER);
        mongoColname.setAllowMultipleDeclarations(false);
        mongoColname.setHelp("[optional] If valid mongo-url and mongo-dbname provided, DSpot will submit to the provided collection name.");

        FlaggedOption repoSlug = new FlaggedOption("repo-slug");
        repoSlug.setLongFlag("repo-slug");
        repoSlug.setDefault("UnknownSlug");
        repoSlug.setRequired(false);
        repoSlug.setStringParser(JSAP.STRING_PARSER);
        repoSlug.setAllowMultipleDeclarations(false);
        repoSlug.setHelp("[optional] slug of the repo for instance Stamp/Dspot,this is used by mongodb as a identifier for analyzed repo's submitted data ");

        FlaggedOption repoBranch = new FlaggedOption("repo-branch");
        repoBranch.setLongFlag("repo-branch");
        repoBranch.setDefault("UnknownBranch");
        repoBranch.setRequired(false);
        repoBranch.setStringParser(JSAP.STRING_PARSER);
        repoBranch.setAllowMultipleDeclarations(false);
        repoBranch.setHelp("[optional] branch name of the submitted repo,this is used by mongodb as a identifier for analyzed repo's submitted data");

        Switch restful = new Switch("restful");
        restful.setLongFlag("restful");
        restful.setDefault("false");
        restful.setHelp("If 1 or true will enable restful mode for web Interface. It will look for a pending document in Mongodb with the corresponding slug and branch provided instead of creating a completely new one.");

        FlaggedOption smtpUsername = new FlaggedOption("smtp-username");
        smtpUsername.setLongFlag("smtp-username");
        smtpUsername.setDefault("Unknown@gmail.com");
        smtpUsername.setRequired(false);
        smtpUsername.setStringParser(JSAP.STRING_PARSER);
        smtpUsername.setAllowMultipleDeclarations(false);
        smtpUsername.setHelp("Username for Gmail, used for submit email at end-process");

        FlaggedOption smtpPassword = new FlaggedOption("smtp-password");
        smtpPassword.setLongFlag("smtp-password");
        smtpPassword.setDefault("Unknown");
        smtpPassword.setRequired(false);
        smtpPassword.setStringParser(JSAP.STRING_PARSER);
        smtpPassword.setAllowMultipleDeclarations(false);
        smtpPassword.setHelp("password for Gmail, used for submit email at end-process");

        FlaggedOption smtpHost = new FlaggedOption("smtp-host");
        smtpHost.setLongFlag("smtp-host");
        smtpHost.setDefault("smtp.gmail.com");
        smtpHost.setRequired(false);
        smtpHost.setStringParser(JSAP.STRING_PARSER);
        smtpHost.setAllowMultipleDeclarations(false);
        smtpHost.setHelp("host server name , default: smtp.gmail.com");

        FlaggedOption smtpPort = new FlaggedOption("smtp-port");
        smtpPort.setLongFlag("smtp-port");
        smtpPort.setDefault("587");
        smtpPort.setRequired(false);
        smtpPort.setStringParser(JSAP.STRING_PARSER);
        smtpPort.setAllowMultipleDeclarations(false);
        smtpPort.setHelp("host server port , default : 587");

        Switch smtpAuth = new Switch("smtp-auth");
        smtpAuth.setLongFlag("smtp-auth");
        smtpAuth.setDefault("false");
        smtpAuth.setHelp("true , if the smtp host server require auth, which is usually the case");

        Switch smtpTls = new Switch("smtp-tls");
        smtpTls.setLongFlag("smtp-tls");
        smtpTls.setDefault("false");
        smtpTls.setHelp("true , if need secure tls transport.");

        try {
            jsap.registerParameter(pathToConfigFile);
            jsap.registerParameter(amplifiers);
            jsap.registerParameter(iteration);
            jsap.registerParameter(selector);
            jsap.registerParameter(pitOutputFormat);
            jsap.registerParameter(inputAmplDistributor);
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
            jsap.registerParameter(executeTestParallel);
            jsap.registerParameter(fullClasspath);
            jsap.registerParameter(collector);
            jsap.registerParameter(mongoUrl);
            jsap.registerParameter(mongoDbname);
            jsap.registerParameter(mongoColname);
            jsap.registerParameter(repoSlug);
            jsap.registerParameter(repoBranch);
            jsap.registerParameter(restful);
            jsap.registerParameter(smtpUsername);
            jsap.registerParameter(smtpPassword);
            jsap.registerParameter(smtpHost);
            jsap.registerParameter(smtpPort);
            jsap.registerParameter(smtpAuth);
            jsap.registerParameter(smtpTls);
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
     * @param args not used
     */
    public static void main(String[] args) {
        final Iterator iterator = options.getIDMap().idIterator();
        while (iterator.hasNext()) {
            final Object id = iterator.next();
            System.out.println(jsapParameterToMojoParameter(options.getByID((String) id)));
        }
    }

}
