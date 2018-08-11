package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.utils.RandomHelper;
import org.junit.Test;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringLiteralAmplifierTest extends AbstractTest {

    @Test
    public void testOneLitExisting() throws Exception {
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.OneLiteralTest");
        Amplifier amplifier = new StringLiteralAmplifier();
        amplifier.reset(literalMutationClass);
        final Stream<CtMethod<?>> test = amplifier.amplify(Utils.findMethod(literalMutationClass, "test"), 0);
        System.out.println(test.collect(Collectors.toList()));
    }


    @Test
    public void testFlatString() throws Exception {
        final CtClass testClass = Utils.findClass("fr.inria.ampl.ToBeAmplifiedLiteralTest");
        CtMethod<?> method = Utils.findMethod(testClass, "testInt");

        StringLiteralAmplifier.flatStringLiterals(method);
        System.out.println(method);

        // TODO spoon adds parenthesis and flat literals does not work in specific case, e.g.
        // TODO java.lang.String s3 = (s1 + "hey") + "ho"; while the source is written as
        // TODO String s3 = s1 + "hey" + "ho";
    }

    @Test
    public void testAmplify() throws Exception {

        /*
            test the StringLiteral
            The first iteration is complete, i.e. apply random operations plus the specific strings
         */

        final String nameMethod = "methodString";
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        Amplifier amplifier = new StringLiteralAmplifier();
        amplifier.reset(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(28, mutantMethods.size());
    }

    @Test
    public void testDoesNotAmplifyChar() throws Exception {
        final String nameMethod = "methodCharacter";
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.LiteralMutation");
        RandomHelper.setSeedRandom(42L);
        Amplifier mutator = new StringLiteralAmplifier();
        mutator.reset(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(nameMethod);
        List<CtMethod> mutantMethods = mutator.amplify(method, 0).collect(Collectors.toList());
        assertTrue(mutantMethods.isEmpty());
    }

    @Test
    public void testFlattenString() throws Exception {

        /*
            test the method to flat string literals.
            After the method call, all the (concatenated) string literals has been merged into one.

         */
        CtClass<Object> literalMutationClass = Utils.getFactory().Class().get("fr.inria.amp.JavaPoet");
        final CtMethod<?> withConcat = Utils.findMethod(literalMutationClass, "method");
        // there is a lot of literal string
        assertEquals(20,
                withConcat.getElements(new TypeFilter<CtLiteral>(CtLiteral.class) {
                    @Override
                    public boolean matches(CtLiteral element) {
                        return element.getValue() instanceof String;
                    }
                }).size());
        StringLiteralAmplifier.flatStringLiterals(withConcat);
        // there is only one literal string
        assertEquals(1,
                withConcat.getElements(new TypeFilter<CtLiteral>(CtLiteral.class) {
                    @Override
                    public boolean matches(CtLiteral element) {
                        return element.getValue() instanceof String;
                    }
                }).size());
    }
}
