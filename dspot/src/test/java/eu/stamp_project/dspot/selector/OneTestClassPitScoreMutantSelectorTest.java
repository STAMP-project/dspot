package eu.stamp_project.dspot.selector;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.utils.pit.AbstractPitResult;
import eu.stamp_project.utils.program.InputConfiguration;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/01/19
 */
public class OneTestClassPitScoreMutantSelectorTest extends AbstractTest {

    private String FULL_QUALIFIED_NAME_TEST_CLASS = "example.TestSuiteExample";

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/test-projects/test-projects.properties";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        InputConfiguration.get().getBuilder().reset();
        InputConfiguration.get().setTestClasses(Collections.singletonList(FULL_QUALIFIED_NAME_TEST_CLASS));
        InputConfiguration.get().setTargetOneTestClass(true);
    }

    @After
    public void tearDown() throws Exception {
        InputConfiguration.get().setTargetOneTestClass(false);
        InputConfiguration.get().setTestClasses(Collections.emptyList());
    }

    @Test
    public void test() throws NoSuchFieldException, IllegalAccessException {
        final PitMutantScoreSelector pitMutantScoreSelector = new PitMutantScoreSelector();
        pitMutantScoreSelector.init();
        final Field field = pitMutantScoreSelector.getClass().getDeclaredField("originalKilledMutants");
        field.setAccessible(true);
        List<AbstractPitResult> originalResult = (List<AbstractPitResult>) field.get(pitMutantScoreSelector);
        assertTrue(originalResult.stream().allMatch(abstractPitResult -> abstractPitResult.getFullQualifiedNameOfKiller().equals(FULL_QUALIFIED_NAME_TEST_CLASS)));
    }
}
