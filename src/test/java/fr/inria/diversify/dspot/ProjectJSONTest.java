package fr.inria.diversify.dspot;

import edu.emory.mathcs.backport.java.util.Collections;
import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.dspot.selector.BranchCoverageTestSelector;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public class ProjectJSONTest extends AbstractTest {

    @Test
    public void test() throws Exception, InvalidSdkException {

        final File file = new File("output/sample.json");
        if (file.exists()) {
            file.delete();
        }

        DSpot dspot = new DSpot(Utils.getInputConfiguration(),
                1,
                Collections.singletonList(new TestDataMutator()),
                new BranchCoverageTestSelector(200)
        );

        dspot.amplifyTest(Utils.getFactory().Class().get("fr.inria.amp.TestJavaPoet"));
        try (BufferedReader buffer = new BufferedReader(new FileReader(file))) {
            final String jsonAsString = buffer.lines().collect(Collectors.joining("\n"));
            assertTrue(jsonAsString.startsWith(expectedFirstProjectJSON[0]));
            assertTrue(jsonAsString.endsWith(expectedFirstProjectJSON[1]));
        }
        dspot.amplifyTest(Utils.getFactory().Class().get("fr.inria.mutation.ClassUnderTestTest"));
        try (BufferedReader buffer = new BufferedReader(new FileReader(file))) {
            final String jsonAsString = buffer.lines().collect(Collectors.joining("\n"));
            assertTrue(jsonAsString.startsWith(expectedFirstProjectJSON[0]));
            assertTrue(jsonAsString.contains(expectedFirstProjectJSON[2]));
            assertTrue(jsonAsString.endsWith(expectedFirstProjectJSON[3]));
        }
    }

    //we cannot predict the time of the computation of dspot, we split the output string into two parts: head and tail.

    private static final String[] expectedFirstProjectJSON = new String[]{
            "{" + nl +
                    "  \"classTimes\": [" + nl +
                    "    {" + nl +
                    "      \"fullQualifiedName\": \"fr.inria.amp.TestJavaPoet\"," + nl +
                    "      \"timeInMs\": ",
            nl +
                    "    }" + nl +
                    "  ]," + nl +
                    "  \"projectName\": \"sample\"" + nl +
                    "}",
            "    }," + nl +
                    "    {" + nl +
                    "      \"fullQualifiedName\": \"fr.inria.mutation.ClassUnderTestTest\"," + nl +
                    "      \"timeInMs\": ",
            "    }" + nl +
                    "  ]," + nl +
                    "  \"projectName\": \"sample\"" + nl +
                    "}"
    };

}
