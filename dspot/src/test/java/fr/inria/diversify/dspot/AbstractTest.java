package fr.inria.diversify.dspot;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.amplifier.value.ValueCreator;
import fr.inria.diversify.utils.AmplificationHelper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/31/17
 */
public abstract class AbstractTest {

    public static final String nl = System.getProperty("line.separator");

    public String getPathToPropertiesFile() {
        return "src/test/resources/sample/sample.properties";
    }

    @Before
    public void setUp() throws Exception {
        Utils.init(getPathToPropertiesFile());
        AmplificationHelper.setSeedRandom(72L);
        ValueCreator.count = 0;
    }

    @After
    public void tearDown() throws Exception {
        Utils.reset();
    }
}
