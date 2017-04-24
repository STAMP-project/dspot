package fr.inria.diversify.dspot.support;

import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.InitUtils;
import org.junit.Test;

import java.net.URL;

import static fr.inria.diversify.dspot.DSpotUtils.buildMavenHome;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 21/04/17
 */
public class MavenDependenciesResolverTest {

    @Test
    public void name() throws Exception {
        final InputConfiguration configuration = new InputConfiguration("src/test/resources/sample/sample.properties");
        InputProgram program = InitUtils.initInputProgram(configuration);
        final URL[] urls = MavenDependenciesResolver.resolveDependencies(configuration, program, buildMavenHome(configuration));
        assertTrue(urls[0].getPath().endsWith("org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"));
        assertTrue(urls[1].getPath().endsWith("junit/junit/4.11/junit-4.11.jar"));
    }

    @Test
    public void testWrongMaven() throws Exception {
        final InputConfiguration configuration = new InputConfiguration("src/test/resources/sample/sample.properties");
        InputProgram program = new InputProgram();
        program.setProgramDir("target/");//is not a maven project
        try {
            MavenDependenciesResolver.resolveDependencies(configuration, program, buildMavenHome(configuration));
            fail("should have thrown FileNotFoundException");
        } catch (Exception expected) {

        }
    }
}
