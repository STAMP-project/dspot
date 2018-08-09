package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.dspot.amplifier.value.ValueCreator;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.RandomHelper;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 16/03/18
 */
public abstract class AbstractSelectorTest {

    protected String getPathToProperties() {
        return "src/test/resources/regression/test-projects_0/test-projects.properties";
    }

    protected abstract TestSelector getTestSelector();

    protected CtMethod<?> getTest() {
        return Utils.findMethod("example.TestSuiteExample", "test2");
    }


    protected abstract CtMethod<?> getAmplifiedTest();

    protected abstract String getPathToReportFile();

    protected abstract String getContentReportFile();

    protected TestSelector testSelectorUnderTest;

    protected abstract Class<?> getClassMinimizer();

    @Before
    public void setUp() throws Exception {
        final String configurationPath = getPathToProperties();
        Utils.init(configurationPath);
        RandomHelper.setSeedRandom(72L);
        ValueCreator.count = 0;
        this.testSelectorUnderTest = this.getTestSelector();
    }

    @Test
    public void testSelector() throws Exception {
        this.testSelectorUnderTest.init(Utils.getInputConfiguration());
        this.testSelectorUnderTest.selectToKeep(
                this.testSelectorUnderTest.selectToAmplify(
                        Collections.singletonList(getTest())
                )
        );
        assertTrue(this.testSelectorUnderTest.getAmplifiedTestCases().isEmpty());

        this.testSelectorUnderTest.selectToKeep(
                this.testSelectorUnderTest.selectToAmplify(
                        Collections.singletonList(getAmplifiedTest())
                )
        );
        assertFalse(this.testSelectorUnderTest.getAmplifiedTestCases().isEmpty());

        this.testSelectorUnderTest.report();
        try (BufferedReader buffer = new BufferedReader(new FileReader(getPathToReportFile()))) {
            assertEquals(getContentReportFile(),
                    buffer.lines()
                            .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertTrue(this.testSelectorUnderTest.getMinimizer().getClass() == getClassMinimizer());
    }
}
