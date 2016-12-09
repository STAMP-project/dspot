package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 22/11/16
 * Time: 09:36
 */
public class RemoveBadTest {

    protected InputProgram inputProgram;
    protected String mvnHome;

    public RemoveBadTest(InputProgram inputProgram, String mvnHome) {
        this.inputProgram = inputProgram;
        this.mvnHome = mvnHome;
    }

    public void init(String tmpDir) throws IOException {
        String outputDirectory = tmpDir + "/tmp_" + System.currentTimeMillis();

        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
        inputProgram.setProgramDir(outputDirectory);

        FileUtils.deleteDirectory(new File(inputProgram.getAbsoluteTestSourceCodeDir()));
    }

    public List<CtType> filterTest(Collection<CtType> tests) throws IOException, InterruptedException {
        List<String> failedTests = findFailedTest(tests);

        return tests.stream()
                .peek(cl -> {
                    CtType clone = cl.getFactory().Core().clone(cl);
                    clone.getMethods().stream()
                            .filter(mth -> {
                                CtMethod test = (CtMethod) mth;
                                return failedTests.stream()
                                    .anyMatch(string -> string.contains(test.getSimpleName()));
                            })
                            .forEach(test -> cl.removeMethod((CtMethod) test));
                })
                .filter(cl -> cl.getMethods().size() != 0)
                .collect(Collectors.toList());
    }

    private List<String> findFailedTest(Collection<CtType> tests) throws IOException, InterruptedException {
        File testDir = new File(inputProgram.getAbsoluteTestSourceCodeDir());
        if(!testDir.exists()) {
            testDir.mkdirs();
        }

        for(CtType test : tests) {
            PrintClassUtils.printJavaFile(testDir, test);
        }

        MavenBuilder builder = new MavenBuilder(inputProgram.getProgramDir());
        builder.setBuilderPath(mvnHome);

        String[] phases  = new String[]{"clean", "test"};
        builder.setGoals(phases);
        builder.initTimeOut();

        return builder.getFailedTests();
    }
}
