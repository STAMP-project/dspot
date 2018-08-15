package eu.stamp_project.dspot;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.dspot.amplifier.MethodGeneratorAmplifier;
import eu.stamp_project.dspot.amplifier.ReturnValueAmplifier;
import eu.stamp_project.dspot.amplifier.value.ValueCreator;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.RandomHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/04/17
 */
public class DSpotMockedTest extends AbstractTest {

    @Test
    public void test() throws Exception {

        /*
            Test the whole dspot procedure.
         */
        ValueCreator.count = 0;
        RandomHelper.setSeedRandom(23L);
        final InputConfiguration configuration = InputConfiguration.get();
        configuration.setAmplifiers(Arrays.asList(new MethodGeneratorAmplifier(), new ReturnValueAmplifier()));
        DSpot dspot = new DSpot(configuration, 1,
                configuration.getAmplifiers()
        );
        try {
            FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
        } catch (Exception ignored) {

        }
        final List<CtMethod<?>> originalTestMethods = AmplificationHelper.getAllTest(dspot.getInputConfiguration().getFactory().Class().get("info.sanaulla.dal.BookDALTest"));
        assertEquals(5, originalTestMethods.size());
        assertEquals(28, originalTestMethods.stream().mapToLong(
                ctMethod -> ctMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                    @Override
                    public boolean matches(CtInvocation<?> element) {
                        return AmplificationChecker.isAssert(element);
                    }
                }).size()).sum());

        EntryPoint.verbose = true;

        CtType<?> amplifiedTest = dspot.amplifyTest("info.sanaulla.dal.BookDALTest", Collections.singletonList("testGetBook"));

        final List<CtMethod<?>> amplifiedTestMethods = AmplificationHelper.getAllTest(amplifiedTest);
        assertEquals(6, amplifiedTestMethods.size());
        assertEquals(53, amplifiedTestMethods.stream().mapToLong(
                ctMethod -> ctMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                    @Override
                    public boolean matches(CtInvocation<?> element) {
                        return AmplificationChecker.isAssert(element);
                    }
                }).size()).sum());
    }

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/mockito/mockito.properties";
    }
}
