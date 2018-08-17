package eu.stamp_project.dspot.amplifier.value;

import eu.stamp_project.Utils;
import eu.stamp_project.AbstractTest;
import org.junit.Test;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/10/17
 */
public class TestConstructorCreator extends AbstractTest {

    @Test
    public void testGenerateAllConstructorOfListOfSpecificObject() throws Exception {

        /*
            Test the good behavior when developers use specific implementation of List.
         */

        final Factory factory = Utils.getFactory();

        final CtTypeReference<ArrayList> listReference = factory.Type().createReference(ArrayList.class);
        listReference.addActualTypeArgument(factory.Type().get("fr.inria.statementadd.ClassParameterAmplify").getReference());

        final List<CtExpression> constructionOf =
                ConstructorCreator.generateAllConstructionOf(listReference);

        assertEquals(1, constructionOf.size());
    }

    @Test
    public void testGenerateAllConstructorOf() throws Exception {

        final Factory factory = Utils.getFactory();

        final List<CtExpression> constructionOf =
                ConstructorCreator.generateAllConstructionOf(factory.Type()
                        .get("fr.inria.statementadd.ClassParameterAmplify")
                        .getReference()
                );
        assertEquals("new fr.inria.statementadd.ClassParameterAmplify(-1183186497)",
                constructionOf.get(0).toString());

        assertEquals("new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(-1432984701), -538589801), 562520686)), 1085381857))",
                constructionOf.get(1).toString());

        assertEquals("new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(164558003), -154269953), -1088520020), 485420728)",
                constructionOf.get(2).toString());

        //TODO this null value is probably not useful
        assertEquals("null",
                constructionOf.get(3).toString());
    }

    @Test
    public void testGenerateConstructorOfAInterface() throws Exception {

        /*
            the constructor creator is able to create an object when we give an interface
         */

        final Factory factory = Utils.getFactory();
        Utils.findMethod("fr.inria.factory.FactoryTest", "test");
        final CtExpression constructorOfInterface = ConstructorCreator.generateConstructionOf(
                factory.Class().get("fr.inria.factory.FactoryTest").getNestedType("aInterface").getReference(), 0
        );
        assertEquals("fr.inria.factory.FactoryTest.createAClass()", constructorOfInterface.toString());

    }

    @Test
    public void testGenerateConstructionOf() throws Exception {
        final Factory factory = Utils.getFactory();

        CtExpression constructionOf = ConstructorCreator.generateConstructionOf(factory.Type()
                .get("fr.inria.statementadd.ClassParameterAmplify")
                .getReference(), 0
        );

        assertEquals("new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(new fr.inria.statementadd.ClassParameterAmplify(-1293507656), -1432984701)), -538589801))",
                constructionOf.toString());

        constructionOf = ConstructorCreator.generateConstructionOf(factory.Type()
                .get("fr.inria.statementadd.ClassParameterAmplify")
                .getReference(), 0
        );

        assertEquals("new fr.inria.statementadd.ClassParameterAmplify(1085381857)",
                constructionOf.toString());
    }

    @Test
    public void testGeneratorConstructionOfReturnNull() throws Exception {
        final Factory factory = Utils.getFactory();
        final CtClass<?> createdClass = factory.Class().create("fr.inria.created.NewTestCreated");
        CtExpression constructionOf = ConstructorCreator.generateConstructionOf(createdClass.getReference(), 0);
        assertTrue(constructionOf instanceof CtLiteral);
        assertNull(((CtLiteral)constructionOf).getValue());
    }

    @Test
    public void testGenerateConstructorUsingFactory() throws Exception {

        /*
            The ConstructorCreator can generate a call on factory.
            We consider has factory methods that:
                - are static
                - return the type expected
                - contains specific keyword in their names: build create //TODO we may need to update this list of names
         */

        final Factory factory = Utils.getFactory();
        Utils.findMethod("fr.inria.factory.FactoryTest", "test");
        final List<CtExpression<?>> aClass = ConstructorCreator.generateConstructorUsingFactory(
                factory.Class().get("fr.inria.factory.FactoryTest").getNestedType("aClass").getReference()
        );
        assertEquals(2, aClass.size());
    }
}
