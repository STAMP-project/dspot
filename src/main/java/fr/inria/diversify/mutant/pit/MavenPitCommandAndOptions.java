package fr.inria.diversify.mutant.pit;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public class MavenPitCommandAndOptions {

    public static boolean descartesMode = false;

    public static boolean evosuiteMode = false;

    public static final String PRE_GOAL_PIT = "clean test -DskipTests";

    public static final String OPT_WITH_HISTORY = "-DwithHistory";

    public static final String OPT_VALUE_REPORT_DIR = "-DreportsDirectory=target/pit-reports";

    public static final String OPT_TARGET_CLASSES = "-DtargetClasses=";

    public static final String OPT_VALUE_FORMAT = "-DoutputFormats=CSV,HTML";

    public static final String OPT_TARGET_TESTS = "-DtargetTests=";

    public static final String PROPERTY_ADDITIONAL_CP_ELEMENTS = "additionalClasspathElements";

    public static final String PROPERTY_EXCLUDED_CLASSES = "excludedClasses";

    public static final String OPT_ADDITIONAL_CP_ELEMENTS = "-D" + PROPERTY_ADDITIONAL_CP_ELEMENTS + "=";

    public static final String OPT_EXCLUDED_CLASSES = "-D" + PROPERTY_EXCLUDED_CLASSES + "=";

    public static final String OPT_MUTATION_ENGINE = "-DmutationEngines=descartes";

    public static final String CMD_PIT_MUTATION_COVERAGE = "org.pitest:pitest-maven:mutationCoverage";

    public static final String OPT_VALUE_TIMEOUT =  "-DtimeoutConst=10000";

    public static final String OPT_VALUE_MEMORY = "-DjvmArgs=16G";

    public static final String OPT_MUTATORS = "-Dmutators=";

    public static final String VALUE_MUTATORS_ALL = "ALL";

    public static final String[] VALUE_MUTATORS_EVOSUITE = new String[]{"VOID_METHOD_CALLS",
            "NON_VOID_METHOD_CALLS",
            "EXPERIMENTAL_MEMBER_VARIABLE",
            "INCREMENTS",
            "INVERT_NEGS",
            "MATH",
            "NEGATE_CONDITIONALS",
            "CONDITIONALS_BOUNDARY",
            "INLINE_CONSTS"};
}
