package eu.stamp_project.program;

import java.util.Properties;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/06/18
 */
public class InputConfigurationProperty {

    private final String name;
    private final String description;
    private final String defaultValue;

    public InputConfigurationProperty(String name, String description, String defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    private boolean hasADefaultValue() {
        return this.defaultValue != null;
    }

    public String get(Properties properties) {
        if (hasADefaultValue()) {
            return properties.getProperty(this.getName(), this.getDefaultValue());
        } else {
            return properties.getProperty(this.getName());
        }
    }

    @Override
    public String toString() {
        if (hasADefaultValue()) {
            return this.name + "[optional]: " + this.description + "(" + this.defaultValue + ")";
        } else {
            return this.name + "[mandatory]: " + this.description ;
        }
    }
}
