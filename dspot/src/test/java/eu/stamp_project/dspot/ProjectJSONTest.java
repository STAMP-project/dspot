package eu.stamp_project.dspot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.amplifier.TestDataMutator;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.json.ProjectTimeJSON;
import eu.stamp_project.utils.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtPackage;

import java.io.*;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public class ProjectJSONTest {

    @Before
    public void setUp() throws Exception {
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
        InputConfiguration.initialize("src/test/resources/sample/sample.properties");
        DSpot dspot = new DSpot(1,
                Collections.singletonList(new TestDataMutator()),
                new JacocoCoverageSelector()
        );

        final CtClass<?> clone = InputConfiguration.get().getFactory().Class().get("fr.inria.amp.TestJavaPoet").clone();

        dspot.amplifyTestClass("fr.inria.amp.TestJavaPoet");
        ProjectTimeJSON projectJson = getProjectJson(file);
        assertTrue(projectJson.classTimes.
                stream()
                .anyMatch(classTimeJSON ->
                        "fr.inria.amp.TestJavaPoet".equals(classTimeJSON.fullQualifiedName)
                )
        );
        assertEquals(1, projectJson.classTimes.size());
        assertEquals("sample", projectJson.projectName);

        dspot.amplifyTestClass("fr.inria.mutation.ClassUnderTestTest");
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

        dspot.amplifyTestClass("fr.inria.amp.TestJavaPoet");
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
