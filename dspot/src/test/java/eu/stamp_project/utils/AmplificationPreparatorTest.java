package eu.stamp_project.utils;

import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/08/18
 */
public class AmplificationPreparatorTest {

    @Test
    public void testConvertWithSuperClassIsJUnit3() throws Exception {

        /*
            Converting a test class that inherit from a JUnit3 test class should
            convert also this super class
         */

        final CtClass secondTestClassJUnit3 = eu.stamp_project.Utils.findClass("fr.inria.helper.SecondClassJUnit3");
        final CtClass testClassJUnit3 = eu.stamp_project.Utils.findClass("fr.inria.helper.SubClassOfJUnit3");
        final CtType<?> converted = AmplificationPreparator.convertToJUnit4(testClassJUnit3,
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
        final CtType<?> converted =  AmplificationPreparator.convertToJUnit4(testClassJUnit3,
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
        final CtType<?> secondConverted = AmplificationPreparator.convertToJUnit4(secondTestClassJUnit3,
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
