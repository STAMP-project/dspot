package eu.stamp_project.prettifier.context2name;

import eu.stamp_project.prettifier.options.InputConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class Context2NameExecutorTest {

    /*
        These test methods requires to have context2name in src/main/resources
     */

    @Before
    public void setUp() throws Exception {
        InputConfiguration.get().setTimeToWaitForContext2nameInMillis(3000);
        InputConfiguration.get().setPathToRootOfContext2Name("src/main/resources/context2name/python");
        InputConfiguration.get().setRelativePathToModelForContext2Name("../model");
    }

    @Ignore // DOES NOT WORK ON TRAVIS, CANNOT FIND python3 cmd
    @Test
    public void test() { ;
        final Context2NameExecutor context2NameExecutor = new Context2NameExecutor();
        context2NameExecutor.run();
        final String output = context2NameExecutor.getOutput();
        System.out.println(output);
        assertTrue(output.startsWith(STARTS_WITH));
        assertTrue(output.contains(CONTAINS));
    }

    private static final String STARTS_WITH = "Original name:\tf";
    private static final String CONTAINS = "\t(1.000000) predicted: ['test']";
}
