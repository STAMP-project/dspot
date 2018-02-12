package fr.inria.diversify.utils;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AbstractTest;
import org.junit.Test;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/30/17
 */
public class AmplificationHelperTest extends AbstractTest {

    @Test
    public void testCreateAmplifiedTestClass() throws Exception {

        CtClass<?> classTest = Utils.getFactory().Class().get("fr.inria.helper.ClassWithInnerClass");
        List<CtMethod<?>> fakeAmplifiedMethod = classTest.getMethods()
                .stream()
                .map(CtMethod::clone)
                .collect(Collectors.toList());
        fakeAmplifiedMethod.forEach(ctMethod -> ctMethod.setSimpleName("ampl" + ctMethod.getSimpleName()));

        CtType<?> amplifiedTest = AmplificationHelper.createAmplifiedTest(fakeAmplifiedMethod, classTest);
        assertEquals(16, amplifiedTest.getMethods().size());

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

    @Test
    public void testReduction() throws Exception {

        /*
            test that the reduction, using hashcode is correct.
            The method should return a list with different test
         */

        AmplificationHelper.MAX_NUMBER_OF_TESTS = 2;

        final CtMethod methodString = Utils.findMethod("fr.inria.amp.LiteralMutation", "methodString");
        // very different
        final CtMethod methodInteger = Utils.findMethod("fr.inria.amp.LiteralMutation", "methodInteger");

        List<CtMethod<?>> methods = new ArrayList<>();
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        methods.add(methodString);
        final CtMethod clone = methodString.clone();
        final CtLiteral originalLiteral = clone.getElements(new TypeFilter<>(CtLiteral.class)).get(0);
        originalLiteral.replace(Utils.getFactory().createLiteral(originalLiteral.getValue() + "a"));
        methods.add(clone);
        methods.add(clone);
        methods.add(clone);
        methods.add(methodInteger);

        final List<CtMethod<?>> reduce = AmplificationHelper.reduce(methods);
        assertEquals(2, reduce.size());

        AmplificationHelper.MAX_NUMBER_OF_TESTS = 200;
    }
}
