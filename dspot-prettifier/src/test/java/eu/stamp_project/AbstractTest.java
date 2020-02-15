package eu.stamp_project;

import eu.stamp_project.dspot.amplifier.amplifiers.value.ValueCreator;
import eu.stamp_project.dspot.amplifier.amplifiers.utils.RandomHelper;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.configuration.options.AutomaticBuilderEnum;
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

    protected UserInput configuration;

    @Before
    public void setUp() throws Exception {
        RandomHelper.setSeedRandom(72L);
        ValueCreator.count = 0;
        this.configuration = new UserInput();
        this.configuration.setAbsolutePathToProjectRoot(this.getPathToPropertiesFile());
        this.configuration.setVerbose(true);
        this.configuration.setBuilderEnum(AutomaticBuilderEnum.Maven);
        this.configuration.setGregorMode(true);
        DSpotState.verbose = true;
        Utils.init(this.configuration);
    }
}
