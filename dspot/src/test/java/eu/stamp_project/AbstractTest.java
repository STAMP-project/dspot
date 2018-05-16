package eu.stamp_project;

import eu.stamp_project.dspot.amplifier.value.ValueCreator;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Before;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/31/17
 */
public abstract class AbstractTest {

    public String getPathToPropertiesFile() {
        return "src/test/resources/sample/sample.properties";
    }

    @Before
    public void setUp() throws Exception {
        Utils.init(getPathToPropertiesFile());
        AmplificationHelper.setSeedRandom(72L);
        ValueCreator.count = 0;
    }
}
