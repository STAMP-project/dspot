package eu.stamp_project.mutant.pit;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenPitCommandAndOptions {

    public static final String OPT_WITH_HISTORY = "-DwithHistory";

    public static final String OPT_VALUE_REPORT_DIR = "-DreportsDirectory=target/pit-reports";

    public static final String OPT_TARGET_CLASSES = "-DtargetClasses=";

    public static final String OPT_VALUE_FORMAT = "-DoutputFormats=CSV,HTML";

    public static final String OPT_TARGET_TESTS = "-DtargetTests=";

    public static final String PROPERTY_ADDITIONAL_CP_ELEMENTS = "additionalClasspathElements";

    public static final String PROPERTY_EXCLUDED_CLASSES = "excludedTestClasses";

    public static final String OPT_ADDITIONAL_CP_ELEMENTS = "-D" + PROPERTY_ADDITIONAL_CP_ELEMENTS + "=";

    public static final String OPT_EXCLUDED_CLASSES = "-D" + PROPERTY_EXCLUDED_CLASSES + "=";

    public static final String OPT_MUTATION_ENGINE_DESCARTES = "-DmutationEngines=descartes";

    public static final String OPT_MUTATION_ENGINE_DEFAULT = "-DmutationEngines=gregor";

    public static final String CMD_PIT_MUTATION_COVERAGE = "org.pitest:pitest-maven";

    public static final String GOAL_PIT_MUTATION_COVERAGE = "mutationCoverage";

    public static final String OPT_VALUE_TIMEOUT = "-DtimeoutConst=10000";

    public static final String OPT_VALUE_MEMORY = "-DjvmArgs=16G";

    public static final String OPT_MUTATORS = "-Dmutators=";

    public static final String VALUE_MUTATORS_ALL = "ALL";

    /**
     * Mutant operator copied from dhell
     */
    public static final String[] VALUE_MUTATORS_DESCARTES = new String[]{
            "void",
            "null",
            "true",
            "false",
            "empty",
            "0",
            "1",
            "(byte)0",
            "(byte)1",
            "(short)1",
            "(short)2",
            "0L",
            "1L",
            "0.0",
            "1.0",
            "0.0f",
            "1.0f",
            "\'\40\'",
            "\'A\'",
            "\"\"",
            "\"A\"",
    };

    public static final String OUTPUT_DIRECTORY_PIT = "target/pit-reports/";
}
