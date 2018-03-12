package fr.inria.stamp.demo.json;

public class ConfigurationJSON {

    public final int number_generated_tests;
    public final int max_number_assertions;

    public ConfigurationJSON(int number_generated_tests, int max_number_assertions) {
        this.number_generated_tests = number_generated_tests;
        this.max_number_assertions = max_number_assertions;
    }
}
