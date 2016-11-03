package fr.inria.diversify.crossCheckingOracle;

import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 24/09/15
 * Time: 10:30
 */
public class CrossCheckingOracle {
    protected InputProgram inputProgram;
    protected String outputDirectory;


    public CrossCheckingOracle(InputProgram inputProgram, String outputDirectory) {
        this.inputProgram = inputProgram;
        this.outputDirectory = outputDirectory;
    }

    public String generateTest() throws IOException {
        init();
        CrossCheckingOracleBuilder builder = new CrossCheckingOracleBuilder();

        addSwitch(inputProgram.getRelativeSourceCodeDir(), outputDirectory);
        addCompareFile(inputProgram.getRelativeTestSourceCodeDir(), outputDirectory);
        File output = new File(outputDirectory + "/" + inputProgram.getRelativeTestSourceCodeDir());

        for (CtClass cl : getAllTestClasses()) {
            Set<CtMethod> methods = new HashSet<>(cl.getMethods());
            for(CtMethod test : methods) {
                if(isTestMethod(test)) {
                    builder.builder(test);
                }
            }
            PrintClassUtils.printJavaFile(output, cl);
        }

        return outputDirectory;
    }

    protected boolean isTestMethod(CtMethod method) {
        String type = method.getType().getSimpleName();
        return method.getSimpleName().contains("test") && (type.equals("Void") || type.equals("void")) && method.getParameters().isEmpty();
    }

    protected Collection<CtClass> getAllTestClasses() {
        String testDir = inputProgram.getRelativeTestSourceCodeDir();
        List<CtClass> allClasses =  inputProgram.getAllElement(CtClass.class);
        return allClasses.stream()
                .filter(cl -> cl.getSimpleName().contains("Test"))
                .filter(cl -> cl.getPosition().getFile().toString().contains(testDir))
                .collect(Collectors.toSet());
    }

    protected void init() throws IOException {
        File dir = new File(outputDirectory);
        dir.mkdirs();
        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), dir);
    }

    protected void addSwitch(String mainSrc, String outputDirectory) throws IOException {
        File srcFile = new File(System.getProperty("user.dir") + "/generator/src/main/java/fr/inria/diversify/transformation/switchsosie/Switch.java");
        File destFile = new File(outputDirectory + "/" + mainSrc + "/fr/inria/diversify/transformation/switchsosie/Switch.java");

        FileUtils.copyFile(srcFile, destFile);
    }

    public void addCompareFile(String mainSrc, String outputDirectory) throws IOException {
        File srcDir = new File(System.getProperty("user.dir") + "/testAmplification/src/main/java/fr/inria/diversify/compare/");

        File destDir = new File(outputDirectory + "/" + mainSrc + "/fr/inria/diversify/compare/");
        FileUtils.forceMkdir(destDir);
        FileUtils.copyDirectory(srcDir, destDir);
    }
}
