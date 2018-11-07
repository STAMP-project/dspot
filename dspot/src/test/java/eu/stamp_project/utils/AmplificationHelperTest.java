package eu.stamp_project.utils;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.program.InputConfiguration;
import org.junit.After;
import org.junit.Test;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/30/17
 */
public class AmplificationHelperTest extends AbstractTest {

    @After
    public void tearDown() throws Exception {
        InputConfiguration.get().setGenerateAmplifiedTestClass(false);
        InputConfiguration.get().setKeepOriginalTestMethods(false);
    }

    @Test
    public void testCreateAmplifiedTestWithReferenceInString() throws Exception {

        /*
            test that literals are also replaced if they contain the original test class name when
                using --generate-new-test-class command line option
         */

        InputConfiguration.get().setGenerateAmplifiedTestClass(true);
        final CtClass<?> testClass = Utils.findClass("fr.inria.amplified.AmplifiedTestClassWithReferenceToName");
        final List<CtMethod<?>> fakeAmplification =
                testClass.getMethods()
                        .stream()
                        .map(CtMethod::clone)
                        .peek(method -> method.setSimpleName("ampl" + method.getSimpleName()))
                .collect(Collectors.toList());
        final CtType amplifiedTest = AmplificationHelper.createAmplifiedTest(fakeAmplification, testClass);
        assertEquals("AmplAmplifiedTestClassWithReferenceToName", amplifiedTest.getElements(new TypeFilter<>(CtLiteral.class)).get(0).getValue()); // must be updated if the resource change
        assertEquals("AmplAmplifiedTestClassWithReferenceToName",
                amplifiedTest.getElements(new TypeFilter<>(CtTypeAccess.class))
                        .get(0)
                        .getAccessedType()
                        .getSimpleName()
        );
    }

    @Test
    public void testConvertWithSuperClassIsJUnit3() throws Exception {

        /*
            Converting a test class that inherit from a JUnit3 test class should
            convert also this super class
         */

        final CtClass secondTestClassJUnit3 = Utils.findClass("fr.inria.helper.SecondClassJUnit3");
        final CtClass testClassJUnit3 = Utils.findClass("fr.inria.helper.SubClassOfJUnit3");
        final CtType<?> converted = JUnit3Support.convertToJUnit4(testClassJUnit3,
                Utils.getInputConfiguration()
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
        final CtClass testClassJUnit3 = Utils.findClass("fr.inria.helper.ClassJunit3");
        final CtType<?> converted = JUnit3Support.convertToJUnit4(testClassJUnit3,
                Utils.getInputConfiguration()
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

        final CtClass secondTestClassJUnit3 = Utils.findClass("fr.inria.helper.SecondClassJUnit3");
        final CtType<?> secondConverted = JUnit3Support.convertToJUnit4(secondTestClassJUnit3,
                Utils.getInputConfiguration()
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

    @Test
    public void testCreateAmplifiedTestClass() throws Exception {

        /*
            The resulting amplifies test class should:
                 (1) be renamed with Ampl in its name
                 (2) should contains only 11 methods,
                    such as the original since we faked an amplified test methods per original test methods
                 (3) all the references are replaced with the new one (i.e. the one with Ampl)
         */

        InputConfiguration.get().setGenerateAmplifiedTestClass(true);
        InputConfiguration.get().setKeepOriginalTestMethods(false);
        CtClass<?> classTest = Utils.getFactory().Class().get("fr.inria.helper.ClassWithInnerClass");
        List<CtMethod<?>> fakeAmplifiedMethod = classTest.getMethods()
                .stream()
                .filter(AmplificationChecker::isTest)
                .map(CtMethod::clone)
                .peek(ctMethod -> ctMethod.setSimpleName("ampl" + ctMethod.getSimpleName()))
                .collect(Collectors.toList());
        CtType<?> amplifiedTest = AmplificationHelper.createAmplifiedTest(fakeAmplifiedMethod, classTest);

        assertTrue(amplifiedTest.getSimpleName().contains("Ampl")); // (1)

        assertEquals(11, amplifiedTest.getMethods().size()); // (2)
        assertEquals(11, classTest.getMethods().size());// (2)

        // (3)
        assertFalse(classTest.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
            @Override
            public boolean matches(CtTypeReference element) {
                return classTest.equals(element.getDeclaration()) &&
                        super.matches(element);
            }
        }).isEmpty());
        // (3)
        assertTrue(amplifiedTest.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
            @Override
            public boolean matches(CtTypeReference element) {
                return classTest.equals(element.getDeclaration()) &&
                        super.matches(element);
            }
        }).isEmpty());
    }

    @Test
    public void testCreateAmplifiedTestClassWithOriginalTestMethod() {

          /*
            The resulting amplifies test class should:
                 (1) not be renamed with Ampl in its name
                 (2) should contains 20 methods,
                    4 amplified and 4 original
                 (3) all the references are the same than original
         */
        InputConfiguration.get().setGenerateAmplifiedTestClass(false);
        InputConfiguration.get().setKeepOriginalTestMethods(true);
        CtClass<?> classTest = Utils.getFactory().Class().get("fr.inria.helper.ClassWithInnerClass");
        List<CtMethod<?>> fakeAmplifiedMethod = classTest.getMethods()
                .stream()
                .filter(AmplificationChecker::isTest)
                .map(CtMethod::clone)
                .collect(Collectors.toList());
        fakeAmplifiedMethod.forEach(ctMethod -> ctMethod.setSimpleName("ampl" + ctMethod.getSimpleName()));
        CtType<?> amplifiedTest = AmplificationHelper.createAmplifiedTest(fakeAmplifiedMethod, classTest);

        // (1)
        assertFalse(amplifiedTest.getSimpleName().contains("Ampl"));
        // (2)
        assertEquals(16, amplifiedTest.getMethods().size());
        // (3)
        assertFalse(classTest.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
            @Override
            public boolean matches(CtTypeReference element) {
                return classTest.equals(element.getDeclaration()) &&
                        super.matches(element);
            }
        }).isEmpty());
    }
}