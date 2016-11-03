package fr.inria.diversify.mutant;

import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * User: Simon
 * Date: 13/01/16
 * Time: 15:03
 */
public class Mutant {
    InputProgram inputProgram;
    String mutantRepo;


    public Mutant(InputProgram inputProgram, String mutantRepo) {
        this.inputProgram = inputProgram;
        this.mutantRepo = mutantRepo;
    }

    public String checkout(String destinationDirName, int id, boolean applicationClass, boolean testClasses) throws Exception {
        if(applicationClass || testClasses) {
            File classesDir = new File(mutantRepo + "/" + id);
            if (!classesDir.exists() || !classesDir.isDirectory()) {
                throw new Exception("");
            }

            File destinationDir = new File(destinationDirName);
            if(destinationDir.exists()) {
                FileUtils.forceDelete(destinationDir);
             }
            FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), destinationDir);

            if(applicationClass) {
                checkoutApplicationClass(destinationDir, classesDir);
            }
            if(testClasses) {
                checkoutTestClasses(destinationDir, classesDir);
            }
        }
        return destinationDirName;
    }

    protected void checkoutTestClasses(File destinationDir, File classesDir) throws IOException {
        File testClassesDir = new File(classesDir.getAbsolutePath()+ "/test");
        File destination = new File(destinationDir.getAbsolutePath() + "/" + inputProgram.getRelativeTestSourceCodeDir());
        FileUtils.copyDirectory(testClassesDir, destination);
    }

    protected void checkoutApplicationClass(File destinationDir, File classesDir) throws IOException {
        File applicationClassesDir = new File(classesDir.getAbsolutePath()+ "/src/");
        File destination = new File(destinationDir.getAbsolutePath() + "/" + inputProgram.getRelativeSourceCodeDir());
        FileUtils.copyDirectory(applicationClassesDir, destination);
    }

    public List<String> triggerTests(int id) throws Exception {
        File classesDir = new File(mutantRepo + "/" + id);
        if (!classesDir.exists() || !classesDir.isDirectory()) {
            throw new Exception("");
        }

        Path path = FileSystems.getDefault().getPath(mutantRepo, id+"", "failures");
        return Files.readAllLines(path);
    }
}
