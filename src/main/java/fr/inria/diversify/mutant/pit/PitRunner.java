package fr.inria.diversify.mutant.pit;

import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.Log;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/3/17
 */
public class PitRunner {

    public static boolean descartesMode = false;

    private static final String PRE_GOAL_PIT = "clean test -DskipTests";

    private static final String OPT_WITH_HISTORY = "-DwithHistory";

    private static final String OPT_VALUE_REPORT_DIR = "-DreportsDirectory=target/pit-reports";

    private static final String OPT_VALUE_MUTATORS = "-Dmutators=ALL";

    private static final String OPT_TARGET_CLASSES = "-DtargetClasses=";

    private static final String OPT_VALUE_FORMAT = "-DoutputFormats=CSV,HTML";

    private static final String OPT_TARGET_TESTS = "-DtargetTests=";

    public static final String PROPERTY_ADDITIONAL_CP_ELEMENTS = "additionalClasspathElements";

    private static final String PROPERTY_EXCLUDED_CLASSES = "excludedClasses";

    private static final String OPT_ADDITIONAL_CP_ELEMENTS = "-D" + PROPERTY_ADDITIONAL_CP_ELEMENTS + "=";

    private static final String OPT_EXCLUDED_CLASSES = "-D" + PROPERTY_EXCLUDED_CLASSES + "=";

    private static final String OPT_MUTATION_ENGINE = "-DmutationEngines=descartes";

    private static final String CMD_PIT_MUTATION_COVERAGE = "org.pitest:pitest-maven:mutationCoverage";

    public static List<PitResult> run(InputProgram program, InputConfiguration configuration, CtType testClass) {
        try {
            long time = System.currentTimeMillis();
            String mavenHome = DSpotUtils.buildMavenHome(configuration);
            MavenBuilder builder = new MavenBuilder(program.getProgramDir());
            builder.setBuilderPath(mavenHome);
            String[] phases = new String[]{PRE_GOAL_PIT, //
                    CMD_PIT_MUTATION_COVERAGE, //
                    OPT_WITH_HISTORY, //
                    OPT_TARGET_CLASSES + configuration.getProperty("filter"), //
                    OPT_VALUE_REPORT_DIR, //
                    OPT_VALUE_FORMAT, //
                    OPT_TARGET_TESTS + testClass.getQualifiedName(), //
                    configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) != null ?
                    OPT_ADDITIONAL_CP_ELEMENTS + configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) :
                    "", //
                    descartesMode ? OPT_MUTATION_ENGINE : OPT_VALUE_MUTATORS, //
                    configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) != null ?
                    OPT_EXCLUDED_CLASSES + configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) :
                            ""//
            };
            builder.runGoals(phases, true);
            if (!new File(program.getProgramDir() + "/target/pit-reports").exists()) {
                return null;
            }
            File directoryReportPit = new File(program.getProgramDir() + "/target/pit-reports").listFiles()[0];
            File fileResults = new File(directoryReportPit.getPath() + "/mutations.csv");
            List<PitResult> results = PitResultParser.parse(fileResults);
            Log.debug("Time to run pit mutation coverage {} ms", System.currentTimeMillis() - time);
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<PitResult> runAll(InputProgram program, InputConfiguration configuration) {
        try {
            long time = System.currentTimeMillis();
            String mavenHome = DSpotUtils.buildMavenHome(configuration);
            MavenBuilder builder = new MavenBuilder(program.getProgramDir());
            builder.setBuilderPath(mavenHome);
            String[] phases = new String[]{PRE_GOAL_PIT, //
                    CMD_PIT_MUTATION_COVERAGE, //
                    OPT_WITH_HISTORY, //
                    OPT_TARGET_CLASSES + configuration.getProperty("filter"), //
                    OPT_VALUE_REPORT_DIR, //
                    OPT_VALUE_FORMAT, //
                    descartesMode ? OPT_MUTATION_ENGINE : OPT_VALUE_MUTATORS, //
                    configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) != null ?
                            OPT_ADDITIONAL_CP_ELEMENTS + configuration.getProperty(PROPERTY_ADDITIONAL_CP_ELEMENTS) :
                            "", //
                    configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) != null ?
                            OPT_EXCLUDED_CLASSES + configuration.getProperty(PROPERTY_EXCLUDED_CLASSES) :
                            ""//
            };
            builder.runGoals(phases, true);
            File directoryReportPit = new File(program.getProgramDir() + "/target/pit-reports").listFiles()[0];
            List<PitResult> results = PitResultParser.parse(new File(directoryReportPit.getPath() + "/mutations.csv"));
            Log.debug("Time to run pit mutation coverage {} ms", System.currentTimeMillis() - time);
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
