package fr.inria.diversify.mutant.pit;

import fr.inria.diversify.buildSystem.maven.MavenBuilder;
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

    private static final String PRE_GOAL_PIT = "clean test -DskipTests";

    private static final String OPT_WITH_HISTORY = "-DwithHistory";

    private static final String OPT_VALUE_REPORT_DIR = "-DreportsDirectory=target/pit-reports";

    private static final String OPT_VALUE_MUTATORS = "-Dmutators=ALL";

    private static final String OPT_VALUE_FORMAT = "-DoutputFormats=CSV";

    private static final String OPT_TARGET_TESTS = "-DtargetTests=";

    private static final String CMD_PIT_MUTATION_COVERAGE = "org.pitest:pitest-maven:mutationCoverage";

    public static List<PitResult> run(InputProgram program, InputConfiguration configuration, CtType testClass) {
        try {
            long time = System.currentTimeMillis();
            String mavenHome = configuration.getProperty("maven.home", null);
            MavenBuilder builder = new MavenBuilder(program.getProgramDir());
            builder.setBuilderPath(mavenHome);
            String[] phases = new String[]{PRE_GOAL_PIT, //
                    OPT_WITH_HISTORY, //
                    OPT_VALUE_MUTATORS, //
                    OPT_VALUE_REPORT_DIR, //
                    OPT_VALUE_FORMAT, //
                    OPT_TARGET_TESTS + testClass.getQualifiedName(), //
                    CMD_PIT_MUTATION_COVERAGE};
            builder.runGoals(phases, false);
            File directoryReportPit = new File(program.getProgramDir() + "/target/pit-reports").listFiles()[0];
            List<PitResult> results = PitResultParser.parse(new File(directoryReportPit.getPath() + "/mutations.csv"), testClass);
            Log.debug("Time to run pit mutation coverage {} ms", System.currentTimeMillis() - time);
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
