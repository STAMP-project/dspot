package fr.inria.diversify.dspot;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import org.junit.After;
import org.junit.Before;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inriAVa.fr
 * on 1/5/17
 */
public abstract class MavenAbstractTest {

    public final String pathToPropertiesFile = getPathToPropertiesFile();

    public static final String nl = System.getProperty("line.separator");

    @Before
    public void setUp() throws Exception {
        AutomaticBuilderFactory.reset();
    }

    @After
    public void tearDown() throws Exception {
        AutomaticBuilderFactory.reset();
    }

    public abstract String getPathToPropertiesFile();

}
