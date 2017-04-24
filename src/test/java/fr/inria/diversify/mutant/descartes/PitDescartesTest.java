package fr.inria.diversify.mutant.descartes;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.StatementAdderOnAssert;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.dspot.amplifier.TestMethodCallAdder;
import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.mutant.pit.PitRunner;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.stamp.JSAPOptions;
import fr.inria.stamp.Main;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/03/17
 */
public class PitDescartesTest {

    private static final String nl = System.getProperty("line.separator");

    @Test
    public void testPitDescartesMode() throws Exception, InvalidSdkException {
        assertFalse(PitRunner.descartesMode);
        FileUtils.deleteDirectory(new File("dspot-out"));

        AmplificationHelper.setSeedRandom(23L);
        PitRunner.descartesMode = true;
        InputConfiguration configuration = new InputConfiguration("src/test/resources/descartes/descartes.properties");
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration, 3,
                Arrays.asList(new TestMethodCallAdder(), new TestDataMutator(), new StatementAdderOnAssert()),
                new PitMutantScoreSelector());

        final CtClass<Object> originalTestClass = dspot.getInputProgram().getFactory().Class().get("fr.inria.stamp.mutationtest.test.TestCalculator");
        assertEquals(2, originalTestClass.getMethods().size());

        final CtType ctType = dspot.amplifyTest("fr.inria.stamp.mutationtest.test.TestCalculator");
        assertEquals(8, ctType.getMethods().size());

        final CtMethod<?> integraltypestest_cf1237 = (CtMethod<?>) ctType.getMethodsByName("Integraltypestest_cf1237").get(0);
        assertEquals(expectedBody, integraltypestest_cf1237.getBody().toString());

        FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));

        assertTrue(PitRunner.descartesMode);
    }

    private static final String expectedBody = "{" + nl +
            "    final fr.inria.stamp.mutationtest.test.Calculator calculator = new fr.inria.stamp.mutationtest.test.Calculator();" + nl +
            "    org.junit.Assert.assertEquals(((byte) (0)), calculator.getByte());" + nl +
            "    org.junit.Assert.assertEquals(((short) (0)), calculator.getShort());" + nl +
            "    org.junit.Assert.assertEquals(0, calculator.getCeiling());" + nl +
            "    org.junit.Assert.assertEquals(0L, calculator.getSquare());" + nl +
            "    // AssertGenerator replace invocation" + nl +
            "    boolean o_Integraltypestest_cf1237__11 = // StatementAdderMethod cloned existing statement" + nl +
            "calculator.isOdd();" + nl +
            "    // AssertGenerator add assertion" + nl +
            "    org.junit.Assert.assertTrue(o_Integraltypestest_cf1237__11);" + nl +
            "    org.junit.Assert.assertEquals(0, calculator.getLastOperatorSymbol());" + nl +
            "}";
}

