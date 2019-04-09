package eu.stamp_project.prettifier.code2vec;

import eu.stamp_project.prettifier.options.InputConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/02/19
 */
public class Code2VecExecutorTest {

    /*
        These test methods requires to have code2vec in src/test/resources
     */

    @Before
    public void setUp() throws Exception {
        InputConfiguration.get().setTimeToWaitForCode2vecInMillis(3000);
        InputConfiguration.get().setPathToRootOfCode2Vec("src/test/resources/code2vec/code2vec");
        InputConfiguration.get().setRelativePathToModelForCode2Vec("../model/saved_model_iter20");
    }

    @Ignore // DOES NOT WORK ON TRAVIS, CANNOT FIND python3 cmd
    @Test
    public void test() { ;
        final Code2VecExecutor code2VecExecutor = new Code2VecExecutor();
        code2VecExecutor.run();
        final String output = code2VecExecutor.getOutput();
        System.out.println(output);
        assertTrue(output.startsWith(STARTS_WITH));
        assertTrue(output.contains(CONTAINS));
    }

    private static final String STARTS_WITH = "Original name:\tf";
    private static final String CONTAINS = "\t(1.000000) predicted: ['test']";
}
