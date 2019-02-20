package eu.stamp_project.prettifier.code2vec;

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

    @Test
    public void test() {
        final String pathToRootOfCode2Vec = "src/test/resources/code2vec/code2vec";
        final Code2VecExecutor code2VecExecutor = new Code2VecExecutor(
                pathToRootOfCode2Vec,
                "../model/saved_model_iter20"
        );
        code2VecExecutor.run();
        final String output = code2VecExecutor.getOutput();
        System.out.println(output);
        assertTrue(output.startsWith(STARTS_WITH));
        assertTrue(output.contains(CONTAINS));
    }

    private static final String STARTS_WITH = "Original name:\tf";
    private static final String CONTAINS = "\t(1.000000) predicted: ['test']";
}
