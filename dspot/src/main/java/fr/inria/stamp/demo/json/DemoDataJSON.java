package fr.inria.stamp.demo.json;

@Deprecated
public class DemoDataJSON {

    public final String project_name;
    public final int random_seed;
    public final ConfigurationJSON input_configuration;
    public final String github_link_to_original_code;
    public final TestMethodJSON original_test_method;
    public final TestMethodJSON before_test_method;
    public final TestMethodJSON after_test_method;
    public final String[] used_amplifiers;
    public final String[] covered_jacoco_instructions;
    public final int absolute_coverage_before;
    public final String[] covered_mutant_identifiers_after;
    public final int absolute_coverage_after;
    public final int absolute_killed_mutants_before;
    public final String[] killed_mutant_identifiers_after;
    public final int absolute_killed_mutants_after;
    public final String[] live_mutant_identifiers_after;

    public DemoDataJSON(String project_name,
                        int random_seed,
                        ConfigurationJSON input_configuration,
                        String github_link_to_original_code,
                        TestMethodJSON original_test_method,
                        TestMethodJSON before_test_method,
                        TestMethodJSON after_test_method,
                        String[] used_amplifiers,
                        String[] covered_jacoco_instructions,
                        int absolute_coverage_before,
                        String[] covered_mutant_identifiers_after,
                        int absolute_coverage_after,
                        int absolute_killed_mutants_before,
                        String[] killed_mutant_identifiers_after,
                        int absolute_killed_mutants_after,
                        String[] live_mutant_identifiers_after) {
        this.project_name = project_name;
        this.random_seed = random_seed;
        this.input_configuration = input_configuration;
        this.github_link_to_original_code = github_link_to_original_code;
        this.original_test_method = original_test_method;
        this.before_test_method = before_test_method;
        this.after_test_method = after_test_method;
        this.used_amplifiers = used_amplifiers;
        this.covered_jacoco_instructions = covered_jacoco_instructions;
        this.absolute_coverage_before = absolute_coverage_before;
        this.covered_mutant_identifiers_after = covered_mutant_identifiers_after;
        this.absolute_coverage_after = absolute_coverage_after;
        this.absolute_killed_mutants_before = absolute_killed_mutants_before;
        this.killed_mutant_identifiers_after = killed_mutant_identifiers_after;
        this.absolute_killed_mutants_after = absolute_killed_mutants_after;
        this.live_mutant_identifiers_after = live_mutant_identifiers_after;
    }
}
