package eu.stamp_project;

import eu.stamp_project.dspot.amplifier.value.ValueCreator;
import eu.stamp_project.utils.RandomHelper;
import eu.stamp_project.utils.options.AutomaticBuilderEnum;
import eu.stamp_project.utils.program.InputConfiguration;
import org.junit.Before;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/31/17
 */
public abstract class AbstractTest {

    public String getPathToPropertiesFile() {
        return "src/test/resources/sample/";
    }

    protected InputConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        RandomHelper.setSeedRandom(72L);
        ValueCreator.count = 0;
        this.configuration = new InputConfiguration();
        this.configuration.setAbsolutePathToProjectRoot(this.getPathToPropertiesFile());
        this.configuration.setVerbose(true);
        this.configuration.setBuilderEnum(AutomaticBuilderEnum.Maven);
        this.configuration.setGregorMode(true);
        Main.verbose = true;
        Utils.init(this.configuration);
    }
}
