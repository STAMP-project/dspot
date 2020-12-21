package eu.stamp_project.diff_test_selection.diff;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DiffComputerTest {

    @Test
    public void test() {
        final File file = new File("src/test/resources/tavernpatch.diff");
        file.delete();
        assertFalse(file.exists());
        new DiffComputer()
                .computeDiffWithDiffCommand(new File("src/test/resources/tavern"), new File("src/test/resources/tavern-refactor"));
        assertTrue(file.exists());
    }
}
