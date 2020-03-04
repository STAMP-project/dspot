package eu.stamp_project.dspot.common.miscellaneous;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
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
public class AmplificationHelperTest extends AbstractTestOnSample {

    @Test
    public void testCreateAmplifiedTestWithReferenceInString() throws Exception {

        /*
            test that literals are also replaced if they contain the original test class name when
                using --generate-new-test-class command line option
         */
        AmplificationHelper.init(10000, true, false);
        final CtClass<?> testClass = findClass("fr.inria.amplified.AmplifiedTestClassWithReferenceToName");
        final CtType amplifiedTest = AmplificationHelper.renameTestClassUnderAmplification(testClass);
        assertEquals("AmplAmplifiedTestClassWithReferenceToName", amplifiedTest.getElements(new TypeFilter<>(CtLiteral.class)).get(0).getValue()); // must be updated if the resource change
        assertEquals("AmplAmplifiedTestClassWithReferenceToName",
                amplifiedTest.getElements(new TypeFilter<>(CtTypeAccess.class))
                        .get(0)
                        .getAccessedType()
                        .getSimpleName()
        );
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

        AmplificationHelper.init(10000, true, false);
        CtClass<?> classTest = findClass("fr.inria.helper.ClassWithInnerClass");
        List<CtMethod<?>> fakeAmplifiedMethod = classTest.getMethods()
                .stream()
                .filter(TestFramework.get()::isTest)
                .map(CtMethod::clone)
                .peek(ctMethod -> ctMethod.setSimpleName("ampl" + ctMethod.getSimpleName()))
                .collect(Collectors.toList());
        CtType<?> amplifiedTest = AmplificationHelper.createAmplifiedTest(fakeAmplifiedMethod, classTest);
        CtType<?> renamedAmplifiedTest = AmplificationHelper.renameTestClassUnderAmplification(amplifiedTest);

        assertTrue(renamedAmplifiedTest.getSimpleName().contains("Ampl")); // (1)

        assertEquals(11, renamedAmplifiedTest.getMethods().size()); // (2)
        assertEquals(11, classTest.getMethods().size());// (2)

        // (3)
        assertTrue(classTest.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
            @Override
            public boolean matches(CtTypeReference element) {
                return element.getDeclaration() != null &&
                        renamedAmplifiedTest.toString().equals(element.getDeclaration().toString()) &&
                        super.matches(element);
            }
        }).isEmpty());
        // (3)
        assertFalse(renamedAmplifiedTest.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
            @Override
            public boolean matches(CtTypeReference element) {
                return renamedAmplifiedTest.equals(element.getDeclaration()) &&
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
        AmplificationHelper.init(10000, false, true);
        CtClass<?> classTest = findClass("fr.inria.helper.ClassWithInnerClass");
        List<CtMethod<?>> fakeAmplifiedMethod = classTest.getMethods()
                .stream()
                .filter(TestFramework.get()::isTest)
                .map(CtMethod::clone)
                .collect(Collectors.toList());
        fakeAmplifiedMethod.forEach(ctMethod -> ctMethod.setSimpleName("ampl" + ctMethod.getSimpleName()));
        CtType<?> amplifiedTest = AmplificationHelper.createAmplifiedTest(fakeAmplifiedMethod, classTest);

        // (1)
        assertFalse(amplifiedTest.getSimpleName().contains("Ampl"));
        // (2)
        assertEquals(15, amplifiedTest.getMethods().size());
        // (3)
        assertFalse(classTest.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
            @Override
            public boolean matches(CtTypeReference element) {
                return element.getDeclaration() != null &&
                        classTest.getQualifiedName().equals(element.getDeclaration().getQualifiedName()) &&
                        super.matches(element);
            }
        }).isEmpty());
    }
}
