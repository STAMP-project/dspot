package eu.stamp_project;

import eu.stamp_project.dspot.amplifier.value.ValueCreator;
import eu.stamp_project.utils.options.InputConfiguration;
import eu.stamp_project.utils.RandomHelper;
import org.junit.Before;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/31/17
 */
public abstract class AbstractTest {

    public String getPathToRootProject() {
        return "src/test/resources/sample/";
    }

    @Before
    public void setUp() throws Exception {
        Utils.init(getPathToRootProject());
        RandomHelper.setSeedRandom(72L);
        ValueCreator.count = 0;
        InputConfiguration.get().setVerbose(true);
        InputConfiguration.get().setGenerateAmplifiedTestClass(false);
        InputConfiguration.get().setKeepOriginalTestMethods(false);
        InputConfiguration.get().setAllowPathInAssertion(false);
    }
}
