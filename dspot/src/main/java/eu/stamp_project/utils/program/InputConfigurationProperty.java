package eu.stamp_project.utils.program;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/06/18
 */
public class InputConfigurationProperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputConfigurationProperty.class);

    private final String name;
    private final String description;
    private final String defaultValue;
    private final String naturalLanguageDesignation;
    private final String oldName;

    public InputConfigurationProperty(String name, String description, String defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.naturalLanguageDesignation = "";
        this.oldName = "";
    }

    public InputConfigurationProperty(String name,
                                      String description,
                                      String defaultValue,
                                      String naturalLanguageDesignation) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.naturalLanguageDesignation = naturalLanguageDesignation;
        this.oldName = "";
    }

    public InputConfigurationProperty(String name, String description, String defaultValue, String naturalLanguageDesignation, String oldName) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.naturalLanguageDesignation = naturalLanguageDesignation;
        this.oldName = oldName;
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

    public String getNaturalLanguageDesignation() {
        return naturalLanguageDesignation;
    }

    public boolean isRequired() {
        return this.defaultValue == null;
    }

    private boolean hasDefaultValue() {
        return !isRequired() && !this.defaultValue.isEmpty();
    }

    public String get(Properties properties) {
        if (properties.containsKey(this.oldName)) {
            LOGGER.warn("You used the old name ({}) for {}.", this.oldName, this.getName());
            LOGGER.warn("The old name will be removed in future versions.");
            LOGGER.warn("Please, update your properties file.");
            return this.get(properties, this.oldName);
        } else {
            return this.get(properties, this.getName());
        }
    }

    private String get(Properties properties, String keyToUse) {
        if (!isRequired()) {
            return properties.getProperty(keyToUse, this.getDefaultValue());
        } else {
            return properties.getProperty(keyToUse);
        }
    }

    /**
     * @return This method return in markdown format a description of the Property
     */
    @Override
    public String toString() {
        return "\t* `" + this.name + "`: " + this.description + (this.hasDefaultValue() ? "(default: " + this.defaultValue + ")" : "");
    }
}
