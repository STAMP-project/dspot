package eu.stamp_project.utils;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.DSpot;
import eu.stamp_project.dspot.selector.TakeAllSelector;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.compilation.TestCompiler;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/08/18
 */
public class AmplificationPreparationTest extends AbstractTest {

    @Test
    public void testUsingAmplifiedTestClassFromTheStart() throws Exception {

        /*
            Some project using the name in assertion, and thus we cannot use the original test class name during the
            process of the amplification. That is now, we rename, and use the renaming version from the start of the amplification.
         */

        final CtClass<?> testClass = Utils.findClass("fr.inria.preparation.MustBeRenamedFromStart");
        final DSpot dSpot = new DSpot(
                InputConfiguration.get(),
                new TakeAllSelector()
        );
        final CtType amplifyTest = dSpot.amplifyTest(testClass);
        assertTrue("should be empty", TestCompiler.compileAndRun(amplifyTest,
                Utils.getCompiler(),
                Collections.singletonList(Utils.findMethod("fr.inria.preparation.MustBeRenamedFromStart", "test")),
                InputConfiguration.get()
        ).getFailingTests().isEmpty());
    }

    @Test
    public void testConvertWithSuperClassIsJUnit3() throws Exception {

        /*
            Converting a test class that inherit from a JUnit3 test class should
            convert also this super class
         */

        final CtClass secondTestClassJUnit3 = eu.stamp_project.Utils.findClass("fr.inria.helper.SecondClassJUnit3");
        final CtClass testClassJUnit3 = eu.stamp_project.Utils.findClass("fr.inria.helper.SubClassOfJUnit3");
        final CtType<?> converted = AmplificationPreparation.convertToJUnit4(testClassJUnit3,
                eu.stamp_project.Utils.getInputConfiguration()
        );
        assertEquals("public class SubClassOfJUnit3 extends fr.inria.helper.SecondClassJUnit3 {" + AmplificationHelper.LINE_SEPARATOR +
                "    @java.lang.Override" + AmplificationHelper.LINE_SEPARATOR +
                "    @org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "    public void test() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "        org.junit.Assert.assertEquals(3, 3);" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "" + AmplificationHelper.LINE_SEPARATOR +
                "    @org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "    public void testThatIsATest() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "        org.junit.Assert.assertEquals(3, 3);" + AmplificationHelper.LINE_SEPARATOR +
                "    }" + AmplificationHelper.LINE_SEPARATOR +
                "}", converted.toString());

        assertEquals("public class SecondClassJUnit3 {" + AmplificationHelper.LINE_SEPARATOR +
                        "    @org.junit.After" + AmplificationHelper.LINE_SEPARATOR +
                        "    public void tearDown() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "" + AmplificationHelper.LINE_SEPARATOR +
                        "    @org.junit.Before" + AmplificationHelper.LINE_SEPARATOR +
                        "    public void setUp() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "" + AmplificationHelper.LINE_SEPARATOR +
                        "    @org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                        "    public void test() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                        "        org.junit.Assert.assertEquals(3, 3);" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "" + AmplificationHelper.LINE_SEPARATOR +
                        "    @org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                        "    public void should() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                        "        org.junit.Assert.assertTrue(true);" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "}",
                secondTestClassJUnit3.toString());
    }

    @Test
    public void testConvert() throws Exception {
        final CtClass testClassJUnit3 = eu.stamp_project.Utils.findClass("fr.inria.helper.ClassJunit3");
        final CtType<?> converted = AmplificationPreparation.convertToJUnit4(testClassJUnit3,
                eu.stamp_project.Utils.getInputConfiguration()
        );
        System.out.println(converted);
        assertEquals("public class ClassJunit3 {" + AmplificationHelper.LINE_SEPARATOR +
                        "    class MyInnerClass {" + AmplificationHelper.LINE_SEPARATOR +
                        "        int value;" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "" + AmplificationHelper.LINE_SEPARATOR +
                        "    @org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                        "    public void test() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                        "        fr.inria.helper.ClassJunit3.MyInnerClass innerClass = new fr.inria.helper.ClassJunit3.MyInnerClass();" + AmplificationHelper.LINE_SEPARATOR +
                        "        innerClass.value = 4;" + AmplificationHelper.LINE_SEPARATOR +
                        "        org.junit.Assert.assertEquals(4, innerClass.value);" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "}",
                converted.toString());

        final CtClass secondTestClassJUnit3 = eu.stamp_project.Utils.findClass("fr.inria.helper.SecondClassJUnit3");
        final CtType<?> secondConverted = AmplificationPreparation.convertToJUnit4(secondTestClassJUnit3,
                eu.stamp_project.Utils.getInputConfiguration()
        );
        System.out.println(secondConverted);
        assertEquals("public class SecondClassJUnit3 {" + AmplificationHelper.LINE_SEPARATOR +
                        "    @org.junit.After" + AmplificationHelper.LINE_SEPARATOR +
                        "    public void tearDown() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "" + AmplificationHelper.LINE_SEPARATOR +
                        "    @org.junit.Before" + AmplificationHelper.LINE_SEPARATOR +
                        "    public void setUp() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "" + AmplificationHelper.LINE_SEPARATOR +
                        "    @org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                        "    public void test() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                        "        org.junit.Assert.assertEquals(3, 3);" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "" + AmplificationHelper.LINE_SEPARATOR +
                        "    @org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                        "    public void should() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                        "        org.junit.Assert.assertTrue(true);" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "}",
                secondConverted.toString());
    }

}
