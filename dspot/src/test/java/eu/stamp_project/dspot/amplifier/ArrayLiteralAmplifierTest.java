package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.amplifier.amplifiers.ArrayLiteralAmplifier;
import eu.stamp_project.dspot.amplifier.amplifiers.utils.RandomHelper;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Andrew Bwogi
 * abwogi@kth.se
 * on 12/09/19
 */
public class ArrayLiteralAmplifierTest extends AbstractTestOnSample {

    ArrayLiteralAmplifier amplifier;

    CtClass<Object> literalMutationClass;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        literalMutationClass = launcher.getFactory().Class().get("fr.inria.amp.ArrayMutation");
        RandomHelper.setSeedRandom(42L);
        amplifier = new ArrayLiteralAmplifier();
    }

    /*
        Test that correct number of mutants is generated,
        the value of the mutants and the name of the new mutant method.
     */
    @Test
    public void testArrayMutation() {
        final String methodName = "methodArray";
        List<String> expectedValues = Arrays.asList("new int[][]{ new int[]{ 3, 4 }, new int[]{ 1, 2 }, new int[]{ 3, 4 } }",
                "new int[][]{ new int[]{ 1, 2 } }","new int[][]{  }","null");
        callAssertions(methodName,expectedValues);
    }

    @Test
    public void testNullArrayMutation() {
        final String methodName = "methodNullArray";
        List<String> expectedValues = Arrays.asList("new int[][]{ new int[]{ 1 } }","new int[][]{  }");
        callAssertions(methodName,expectedValues);
    }

    @Test
    public void testEmptyArrayMutation1() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList("int","byte","short","long","float","double","char","String","Object"));
        for(String type : list) {
            final String methodName = type.toLowerCase() + "EmptyArray";
            if(type.equals("String") || type.equals("Object")){
                type = "java.lang." + type;
            }
            List<String> expectedValues =
                    Arrays.asList(
                            "new " + type + "[][]{ new " + type +"[]{ " + constructAdditionalElement(type) + " } }",
                            "null"
                    );
            callAssertions(methodName, expectedValues);
        }
    }

    @Test
    public void testEmptyArrayMutation2() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList("Integer","Byte","Short","Long","Float","Double","Character"));
        for(String type : list) {
            final String methodName = type.toLowerCase() + "Reference" + "EmptyArray";
            String fullType = "java.lang." + type;
            List<String> expectedValues = Arrays.asList("new " + fullType + "[][]{ new " + fullType +"[]{ " +
                    constructAdditionalElement(type) + " } }","null");
            callAssertions(methodName,expectedValues);
        }
    }

    private void callAssertions(String methodName, List<String> expectedValues){
        amplifier.reset(literalMutationClass);
        CtMethod method = literalMutationClass.getMethod(methodName);
        List<CtMethod> mutantMethods = amplifier.amplify(method, 0).collect(Collectors.toList());
        assertEquals(expectedValues.size(), mutantMethods.size());
        for (int i = 0; i < mutantMethods.size(); i++) {
            CtMethod mutantMethod = mutantMethods.get(i);
            assertEquals(methodName + "_litArray" + (i + 1), mutantMethod.getSimpleName());
            CtExpression mutantLiteral = mutantMethod.getBody().getElements(new TypeFilter<>(CtExpression.class)).get(0);
            assertTrue(mutantLiteral + " not in expected values",
                    expectedValues.contains(mutantLiteral.toString()));
        }
    }

    private String constructAdditionalElement(String type) {
        type = type.toLowerCase();
        if(type.equals("int") || type.equals("integer") || type.equals("short") || type.equals("byte")){
            return "1";
        } else if(type.equals("long")){
            return "1L";
        } else if(type.equals("float")){
            return "1.1F";
        } else if(type.equals("double")){
            return "1.1";
        } else if(type.equals("byte")){
            return "1";
        } else if(type.equals("boolean")){
            return "true";
        } else if(type.equals("char") || type.equals("character")){
            return "'a'";
        } else if(type.equals("java.lang.string")){
            return "\"a\"";
        } else {
            return "null";
        }
    }
}
