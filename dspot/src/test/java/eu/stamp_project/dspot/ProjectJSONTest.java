package eu.stamp_project.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.amplifier.FastLiteralAmplifier;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.json.ProjectTimeJSON;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.report.output.Output;
import eu.stamp_project.utils.test_finder.TestFinder;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtPackage;

import java.io.*;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public class ProjectJSONTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        try {
            FileUtils.forceDelete(new File("target/dspot/"));
        } catch (Exception ignored) {

        }
    }

    @Test
    public void test() throws Exception {

        final File file = new File("target/trash/sample.json");
        if (file.exists()) {
            file.delete();
        }
        final JacocoCoverageSelector jacocoCoverageSelector = new JacocoCoverageSelector();
        final InputAmplDistributor inputAmplDistributor = InputConfiguration.get().getBudgetizer().getInputAmplDistributor();
        DSpot dspot = new DSpot(
                TestFinder.get(),
                Utils.getCompiler(),
                jacocoCoverageSelector,
                inputAmplDistributor,
                Output.get(InputConfiguration.get()),
                1
        );
        final CtClass<?> clone = InputConfiguration.get().getFactory().Class().get("fr.inria.amp.TestJavaPoet").clone();

        dspot.amplify(Utils.findClass("fr.inria.amp.TestJavaPoet"), Collections.emptyList());
        ProjectTimeJSON projectJson = getProjectJson(file);
        assertTrue(projectJson.classTimes.
                stream()
                .anyMatch(classTimeJSON ->
                        "fr.inria.amp.TestJavaPoet".equals(classTimeJSON.fullQualifiedName)
                )
        );
        assertEquals(1, projectJson.classTimes.size());
        assertEquals("sample", projectJson.projectName);

        dspot.amplify(Utils.findClass("fr.inria.mutation.ClassUnderTestTest"), Collections.emptyList());
        projectJson = getProjectJson(file);
        assertTrue(projectJson.classTimes.stream().anyMatch(classTimeJSON -> classTimeJSON.fullQualifiedName.equals("fr.inria.amp.TestJavaPoet")));
        assertTrue(projectJson.classTimes.stream().anyMatch(classTimeJSON -> classTimeJSON.fullQualifiedName.equals("fr.inria.mutation.ClassUnderTestTest")));
        assertEquals(2, projectJson.classTimes.size());
        assertEquals("sample", projectJson.projectName);

        /* we reinitialize the factory to remove the amplified test class */

        final CtClass<?> amplifiedClassToBeRemoved = InputConfiguration.get().getFactory().Class().get("fr.inria.amp.TestJavaPoet");
        final CtPackage aPackage = amplifiedClassToBeRemoved.getPackage();
        aPackage.removeType(amplifiedClassToBeRemoved);
        aPackage.addType(clone);

        dspot.amplify(Utils.findClass("fr.inria.amp.TestJavaPoet"), Collections.emptyList());
        projectJson = getProjectJson(file);
        assertTrue(projectJson.classTimes.stream().anyMatch(classTimeJSON -> classTimeJSON.fullQualifiedName.equals("fr.inria.amp.TestJavaPoet")));
        assertTrue(projectJson.classTimes.stream().anyMatch(classTimeJSON -> classTimeJSON.fullQualifiedName.equals("fr.inria.mutation.ClassUnderTestTest")));
        assertEquals(2, projectJson.classTimes.size());
        assertEquals("sample", projectJson.projectName);
    }

    private static ProjectTimeJSON getProjectJson(File fileProjectJSON) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            return gson.fromJson(new FileReader(fileProjectJSON), ProjectTimeJSON.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //we cannot predict the time of the computation of dspot, we split the output string into two parts: head and tail.

    private static final String[] expectedFirstProjectJSON = new String[]{
            "{" + AmplificationHelper.LINE_SEPARATOR +
                    "  \"classTimes\": [" + AmplificationHelper.LINE_SEPARATOR +
                    "    {" + AmplificationHelper.LINE_SEPARATOR +
                    "      \"fullQualifiedName\": \"fr.inria.amp.TestJavaPoet\"," + AmplificationHelper.LINE_SEPARATOR +
                    "      \"timeInMs\": ",
            AmplificationHelper.LINE_SEPARATOR +
                    "    }" + AmplificationHelper.LINE_SEPARATOR +
                    "  ]," + AmplificationHelper.LINE_SEPARATOR +
                    "  \"projectName\": \"sample\"" + AmplificationHelper.LINE_SEPARATOR +
                    "}",
            "    }," + AmplificationHelper.LINE_SEPARATOR +
                    "    {" + AmplificationHelper.LINE_SEPARATOR +
                    "      \"fullQualifiedName\": \"fr.inria.mutation.ClassUnderTestTest\"," + AmplificationHelper.LINE_SEPARATOR +
                    "      \"timeInMs\": ",
            "    }" + AmplificationHelper.LINE_SEPARATOR +
                    "  ]," + AmplificationHelper.LINE_SEPARATOR +
                    "  \"projectName\": \"sample\"" + AmplificationHelper.LINE_SEPARATOR +
                    "}"
    };
}
