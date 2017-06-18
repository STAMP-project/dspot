package fr.inria.diversify.dspot;

import fr.inria.diversify.Utils;
import fr.inria.diversify.runner.InputProgram;
import org.junit.Test;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/30/17
 */
public class AmplifierHelperTest {

    @Test
    public void testWrongMaven() throws Exception {
        InputProgram program = new InputProgram();
        program.setProgramDir("target/");//is not a maven project
        try {
            AmplificationHelper.getDependenciesOf(Utils.getInputConfiguration(), program);
            fail("should have thrown FileNotFoundException");
        } catch (Exception expected) {
            //ignored
        }
    }

    @Test
    public void testGetDependenciesOf() throws Exception {
        Utils.init("src/test/resources/sample/sample.properties");
        final String dependenciesOf = AmplificationHelper.getDependenciesOf(Utils.getInputConfiguration(), Utils.getInputProgram());
        final String separator = System.getProperty("path.separator");
        final String[] dependencies = dependenciesOf.split(separator);
        assertTrue(dependencies[0].endsWith("org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar")
                || dependencies[0].endsWith("junit/junit/4.11/junit-4.11.jar"));
        assertTrue(dependencies[1].endsWith("org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar")
                || dependencies[1].endsWith("junit/junit/4.11/junit-4.11.jar"));
    }

    @Test
    public void testCreateAmplifiedTestClass() throws Exception {

        Utils.init("src/test/resources/sample/sample.properties");

        CtClass<Object> classTest = Utils.getFactory().Class().get("fr.inria.helper.ClassWithInnerClass");
        List<CtMethod<?>> fakeAmplifiedMethod = classTest.getMethods()
                .stream()
                .map(CtMethod::clone)
                .collect(Collectors.toList());
        fakeAmplifiedMethod.forEach(ctMethod -> ctMethod.setSimpleName("ampl" + ctMethod.getSimpleName()));

        CtType amplifiedTest = AmplificationHelper.createAmplifiedTest(fakeAmplifiedMethod, classTest);
        assertEquals(classTest.getMethods().size() * 2, amplifiedTest.getMethods().size());

        assertFalse(classTest.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
            @Override
            public boolean matches(CtTypeReference element) {
                return classTest.equals(element.getDeclaration()) &&
                        super.matches(element);
            }
        }).isEmpty());

        assertTrue(amplifiedTest.getElements(new TypeFilter<CtTypeReference>(CtTypeReference.class) {
            @Override
            public boolean matches(CtTypeReference element) {
                return classTest.equals(element.getDeclaration()) &&
                        super.matches(element);
            }
        }).isEmpty());
    }
}
