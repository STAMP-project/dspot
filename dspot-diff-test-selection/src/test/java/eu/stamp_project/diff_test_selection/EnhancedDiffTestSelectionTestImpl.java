package eu.stamp_project.diff_test_selection;

import eu.stamp_project.diff_test_selection.coverage.Coverage;
import eu.stamp_project.diff_test_selection.coverage.DiffCoverage;
import eu.stamp_project.diff_test_selection.diff.DiffComputer;
import eu.stamp_project.diff_test_selection.selector.EnhancedDiffTestSelection;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class EnhancedDiffTestSelectionTestImpl {

    @Test
    public void testOnlyAddition() {

        /*
            Testing the selection when the diff is only addition
                 Here, we have T3 = T1  U T2
                    T1 = empty
                    T2 = test executing addition on v2
             NO TEST MODIFICATIONS
         */

        final Coverage coverageV1 = new Coverage();
        for (int line: new Integer[]{12, 16, 17, 18, 19, 21, 22, 23, 25}) {
            coverageV1.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "test",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
        }
        for (int line: new Integer[]{12, 13, 14}) {
            coverageV1.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "hittingTest",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
        }
        final Coverage coverageV2 = new Coverage();
        for (int line: new Integer[]{12, 15, 16, 17, 18, 20, 21, 22, 24}) {
            coverageV2.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "test",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
        }
        for (int line: new Integer[]{12, 13}) {
            coverageV2.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "hittingTest",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
        }
        final File p1Directory = new File("src/test/resources/only-addition-p1");
        final File p1SrcDirectory = new File("src/test/resources/only-addition-p1/src/main/java");
        final File p2Directory = new File("src/test/resources/only-addition-p2");
        final File p2SrcDirectory = new File("src/test/resources/only-addition-p2/src/main/java");
        final String diff = new DiffComputer().computeDiffWithDiffCommand(p1SrcDirectory, p2SrcDirectory);

        EnhancedDiffTestSelection diffTestSelection = new EnhancedDiffTestSelection(
                p1Directory.getAbsolutePath(),
                p2Directory.getAbsolutePath(),
                coverageV1,
                diff,
                new DiffCoverage(),
                coverageV2
        );
        Map<String, Set<String>> selectedTests = diffTestSelection.selectTests();
        assertTrue(selectedTests.containsKey("fr.inria.stamp.only_modification.FibonacciTest"));
        assertTrue(selectedTests.get("fr.inria.stamp.only_modification.FibonacciTest").contains("hittingTest"));
    }

    @Test
    public void testOnlyDeletion() {

        /*
            Testing the selection when the diff is only deletion
                 Here, we have T3 = T1  U T2
                    T1 = test executing deletion on v1
                    T2 = empty
             NO TEST MODIFICATIONS
         */

        final Coverage coverageV1 = new Coverage();
        for (int line: new Integer[]{12, 15, 16, 17, 18, 20, 21, 22, 24}) {
            coverageV1.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "test",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
        }
        for (int line: new Integer[]{12, 13}) {
            coverageV1.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "hittingTest",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
        }
        final Coverage coverageV2 = new Coverage();
        for (int line: new Integer[]{12, 16, 17, 18, 19, 21, 22, 23, 25}) {
            coverageV2.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "test",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
        }
        for (int line: new Integer[]{12, 13, 14}) {
            coverageV2.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "hittingTest",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
        }

        final File p1Directory = new File("src/test/resources/only-deletion-p1");
        final File p1SrcDirectory = new File("src/test/resources/only-deletion-p1/src/main/java");
        final File p2Directory = new File("src/test/resources/only-deletion-p2");
        final File p2SrcDirectory = new File("src/test/resources/only-deletion-p2/src/main/java");
        final String diff = new DiffComputer().computeDiffWithDiffCommand(p1SrcDirectory, p2SrcDirectory);

        EnhancedDiffTestSelection diffTestSelection = new EnhancedDiffTestSelection(
                p1Directory.getAbsolutePath(),
                p2Directory.getAbsolutePath(),
                coverageV1,
                diff,
                new DiffCoverage(),
                coverageV2
        );
        Map<String, Set<String>> selectedTests = diffTestSelection.selectTests();
        assertTrue(selectedTests.containsKey("fr.inria.stamp.only_modification.FibonacciTest"));
        assertTrue(selectedTests.get("fr.inria.stamp.only_modification.FibonacciTest").contains("hittingTest"));
    }

    @Test
    public void testModification() {

        /*
            NO TEST MODIFICATIONS
            Testing the selection when the diff is only modification
                Here, we have T3 = T1  U T2
                    T1 = test executing deletion on v1
                    T2 = test executing addition on v2

         */

        final Coverage coverageV1 = new Coverage();
        final Coverage coverageV2 = new Coverage();
        for (int line: new Integer[]{12, 15, 16, 17, 18, 20, 21, 22, 24}) {
            coverageV1.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "test",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
            coverageV2.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "test",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
        }
        for (int line: new Integer[]{12, 13}) {
            coverageV1.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "hittingTest",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
            coverageV2.addCoverage(
                    "fr.inria.stamp.only_modification.FibonacciTest",
                    "hittingTest",
                    "fr.inria.stamp.only_modification.Fibonacci",
                    line,
                    1
            );
        }

        final File p1Directory = new File("src/test/resources/only-modification-p1");
        final File p1SrcDirectory = new File("src/test/resources/only-modification-p1/src/main/java");
        final File p2Directory = new File("src/test/resources/only-modification-p2");
        final File p2SrcDirectory = new File("src/test/resources/only-modification-p2/src/main/java");
        final String diff = new DiffComputer().computeDiffWithDiffCommand(p1SrcDirectory, p2SrcDirectory);

        EnhancedDiffTestSelection diffTestSelection = new EnhancedDiffTestSelection(
                p1Directory.getAbsolutePath(),
                p2Directory.getAbsolutePath(),
                coverageV1,
                diff,
                new DiffCoverage(),
                new Coverage()
        );
        Map<String, Set<String>> selectedTests = diffTestSelection.selectTests();
        assertTrue(selectedTests.containsKey("fr.inria.stamp.only_modification.FibonacciTest"));
        assertTrue(selectedTests.get("fr.inria.stamp.only_modification.FibonacciTest").contains("hittingTest"));

        diffTestSelection = new EnhancedDiffTestSelection(
                p1Directory.getAbsolutePath(),
                p2Directory.getAbsolutePath(),
                new Coverage(),
                diff,
                new DiffCoverage(),
                coverageV2
        );
        selectedTests = diffTestSelection.selectTests();
        assertTrue(selectedTests.containsKey("fr.inria.stamp.only_modification.FibonacciTest"));
        assertTrue(selectedTests.get("fr.inria.stamp.only_modification.FibonacciTest").contains("hittingTest"));

        diffTestSelection = new EnhancedDiffTestSelection(
                p1Directory.getAbsolutePath(),
                p2Directory.getAbsolutePath(),
                coverageV1,
                diff,
                new DiffCoverage(),
                coverageV2
        );
        selectedTests = diffTestSelection.selectTests();
        assertTrue(selectedTests.containsKey("fr.inria.stamp.only_modification.FibonacciTest"));
        assertTrue(selectedTests.get("fr.inria.stamp.only_modification.FibonacciTest").contains("hittingTest"));
    }
}
