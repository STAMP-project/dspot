package fr.inria.diversify.mutant.descartes;

import fr.inria.diversify.automaticbuilder.MavenAutomaticBuilder;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.amplifier.StatementAdderOnAssert;
import fr.inria.diversify.dspot.amplifier.TestDataMutator;
import fr.inria.diversify.dspot.amplifier.TestMethodCallAdder;
import fr.inria.diversify.dspot.selector.PitMutantScoreSelector;
import fr.inria.diversify.mutant.pit.MavenPitCommandAndOptions;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import org.junit.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.util.Arrays;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 24/03/17
 */
public class PitDescartesTest {

    private static final String nl = System.getProperty("line.separator");

    //TODO The generation is not deterministic
    @Test
    public void testPitDescartesMode() throws Exception, InvalidSdkException {
        assertFalse(MavenPitCommandAndOptions.descartesMode);
        FileUtils.deleteDirectory(new File("dspot-out"));

        AmplificationHelper.setSeedRandom(23L);
        MavenPitCommandAndOptions.descartesMode = true;
        InputConfiguration configuration = new InputConfiguration("src/test/resources/descartes/descartes.properties");
        InputProgram program = new InputProgram();
        configuration.setInputProgram(program);
        DSpot dspot = new DSpot(configuration, 3,
                Arrays.asList(new TestMethodCallAdder(), new TestDataMutator(), new StatementAdderOnAssert()),
                new PitMutantScoreSelector());

        final CtClass<Object> originalTestClass = dspot.getInputProgram().getFactory().Class().get("fr.inria.stamp.mutationtest.test.TestCalculator");
        assertEquals(2, originalTestClass.getMethods().size());

        final CtType ctType = dspot.amplifyTest("fr.inria.stamp.mutationtest.test.TestCalculator");
//        assertEquals(8, ctType.getMethods().size()); TODO
        assertTrue(7 <= ctType.getMethods().size() && ctType.getMethods().size() <= 8);

        Predicate<CtMethod> isAmplifiedTest = (ctMethod) ->
                ctMethod.getSimpleName().contains("_failAssert") ||
                        ctMethod.getSimpleName().contains("_cf") ||
                        ctMethod.getSimpleName().contains("_add");
        assertEquals(ctType.getMethods().size() - 2,
                ctType.getMethods().stream().filter(isAmplifiedTest).count());

        //TODO
//        final CtMethod<?> integraltypestest_cf1237 = (CtMethod<?>) ctType.getMethodsByName("Floatingpointtypestest_cf27_failAssert13").get(0);
//        assertEquals(expectedBody, integraltypestest_cf1237.getBody().toString());

        FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));

        assertTrue(MavenPitCommandAndOptions.descartesMode);
    }

    private static final String expectedBody = "{" + nl +
            "    // AssertGenerator generate try/catch block with fail statement" + nl +
            "    try {" + nl +
            "        final fr.inria.stamp.mutationtest.test.Calculator calculator = new fr.inria.stamp.mutationtest.test.Calculator();" + nl +
            "        // MethodAssertGenerator build local variable" + nl +
            "        Object o_3_0 = calculator.getSomething();" + nl +
            "        // MethodAssertGenerator build local variable" + nl +
            "        Object o_5_0 = calculator.add(23.0F);" + nl +
            "        // StatementAdderOnAssert create random local variable" + nl +
            "        int vc_17 = -1780117107;" + nl +
            "        // StatementAdderMethod cloned existing statement" + nl +
            "        calculator.getScreen(vc_17);" + nl +
            "        // MethodAssertGenerator build local variable" + nl +
            "        Object o_11_0 = calculator.getSomething();" + nl +
            "        org.junit.Assert.fail(\"Floatingpointtypestest_cf27 should have thrown UnknownFormatConversionException\");" + nl +
            "    } catch (java.util.UnknownFormatConversionException eee) {" + nl +
            "    }" + nl +
            "}";
}

