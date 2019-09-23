package eu.stamp_project.dspot;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.amplifier.MethodAdderOnExistingObjectsAmplifier;
import eu.stamp_project.dspot.amplifier.ReturnValueAmplifier;
import eu.stamp_project.dspot.amplifier.value.ValueCreator;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.options.InputConfiguration;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.RandomHelper;
import eu.stamp_project.utils.report.output.Output;
import eu.stamp_project.utils.test_finder.TestFinder;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.Arrays;
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

        InputConfiguration.get().setKeepOriginalTestMethods(true);

        ValueCreator.count = 0;
        RandomHelper.setSeedRandom(23L);
        final InputConfiguration configuration = InputConfiguration.get();
        configuration.setAmplifiers(Arrays.asList(new MethodAdderOnExistingObjectsAmplifier(), new ReturnValueAmplifier()));
        final JacocoCoverageSelector jacocoCoverageSelector = new JacocoCoverageSelector();
        DSpot dspot = new DSpot(
                TestFinder.get(),
                Utils.getCompiler(),
                jacocoCoverageSelector,
                InputConfiguration.get().getBudgetizer().getInputAmplDistributor(),
                Output.get(InputConfiguration.get()),
                1,
                InputConfiguration.get().shouldGenerateAmplifiedTestClass());
        try {
            FileUtils.cleanDirectory(new File(configuration.getOutputDirectory()));
        } catch (Exception ignored) {

        }
        final CtClass<Object> testClass = InputConfiguration.get().getFactory().Class().get("info.sanaulla.dal.BookDALTest");
        final List<CtMethod<?>> originalTestMethods = TestFramework.getAllTest(testClass);
        assertEquals(5, originalTestMethods.size());
        assertEquals(28, originalTestMethods.stream().mapToLong(
                ctMethod -> ctMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                    @Override
                    public boolean matches(CtInvocation<?> element) {
                        return TestFramework.get().isAssert(element);
                    }
                }).size()).sum());

        EntryPoint.verbose = true;


        CtType<?> amplifiedTest = dspot.amplify(
                Utils.findType("info.sanaulla.dal.BookDALTest"),
                "testGetBook"
        );

        final List<CtMethod<?>> amplifiedTestMethods = TestFramework.getAllTest(amplifiedTest);
        assertEquals(6, amplifiedTestMethods.size());
        //assertEquals(89, amplifiedTestMethods.stream().mapToLong(
        assertEquals(63, amplifiedTestMethods.stream().mapToLong(
                ctMethod -> ctMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                    @Override
                    public boolean matches(CtInvocation<?> element) {
                        return TestFramework.get().isAssert(element);
                    }
                }).size()).sum());
    }

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/mockito/mockito.properties";
    }
}
