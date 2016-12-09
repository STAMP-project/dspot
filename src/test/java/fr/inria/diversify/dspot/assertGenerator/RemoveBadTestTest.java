package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.Utils;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.Log;

import org.junit.AfterClass;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/9/16
 */
public class RemoveBadTestTest {

    @Test
    public void testFilterTests() throws Exception, InvalidSdkException {

        /*
            Test RemoveBadTest: the input class test got 4 tests:
                3 of them failed (on purpose), only one has to be keep.
         */

        String mavenHome = System.getenv().get("MAVEN_HOME");
        if (mavenHome == null) {
            mavenHome = "/usr/share/maven/";
            Log.warn("Using default installation of Maven : "  + mavenHome);
        }

        InputProgram inputProgram = Utils.getInputProgram();
        RemoveBadTest removeBadTest = new RemoveBadTest(inputProgram, mavenHome);

        CtClass<?> testClass = Utils.findClass("fr.inria.removebadtest.TestClassToBeTested");

        assertEquals(4, testClass.getMethods().size());

        removeBadTest.init("target");
        List<CtType> ctTypes = removeBadTest.filterTest(Collections.singleton(testClass));

        assertEquals(1, ctTypes.size());
        assertEquals(1, ctTypes.get(0).getMethods().size());
        assertEquals("testKeep", ((CtMethod)(ctTypes.get(0).getMethods().stream().findFirst().get())).getSimpleName());
    }

    @AfterClass
    public static void tearDown() throws InvalidSdkException, Exception {
        FileUtils.forceDelete(Utils.getCompiler().getBinaryOutputDirectory());
        FileUtils.forceDelete(Utils.getCompiler().getSourceOutputDirectory());
        Utils.reset();
    }

}
