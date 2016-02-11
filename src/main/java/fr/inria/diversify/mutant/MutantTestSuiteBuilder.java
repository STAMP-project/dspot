package fr.inria.diversify.mutant;

import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 08/01/16
 * Time: 18:36
 */
public class MutantTestSuiteBuilder {
    protected InputProgram inputProgram;
    protected CtClass originalClass;
    protected int count;
    protected String repo;

    public MutantTestSuiteBuilder(InputProgram inputProgram , CtClass originalClass, String repo) throws IOException {
        this.inputProgram = inputProgram;
        this.originalClass = originalClass;
        this.repo = repo;
    }

    public void addMutant(CtClass mutantClass, Collection<String> failures, String mutationId) throws IOException {
        try {
            Set<CtClass> mutantTestClasses = createMutantTestClass(mutantClass, failures);

            if (!mutantTestClasses.isEmpty()) {
                createMutantRepo(mutantClass, mutantTestClasses, failures, mutationId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createMutantRepo(CtClass mutantClass, Set<CtClass> mutantTestClasses, Collection<String> failures, String mutationId) throws IOException {
        File dir = new File(repo + "/" + count++);
        dir.mkdirs();

        FileWriter failuresSummary = new FileWriter(dir.getAbsoluteFile() + "/failures");
        for(String failure : failures) {
            failuresSummary.write(failure + "\n");
        }
        failuresSummary.close();

        FileWriter mutation = new FileWriter(dir.getAbsoluteFile() + "/mutation");
        mutation.write(mutationId);
        mutation.close();

        File sourceDir = new File(dir.getAbsolutePath() + "/src/");
        sourceDir.mkdirs();

        File testSourceDir = new File(dir.getAbsolutePath() + "/test/");
        testSourceDir.mkdirs();

        PrintClassUtils.printJavaFile(sourceDir, mutantClass);

        for (CtClass testClass : mutantTestClasses) {
            PrintClassUtils.printJavaFile(testSourceDir, testClass);
        }
    }

    protected List<String> runTest() throws InterruptedException, IOException {
        MavenBuilder builder = new MavenBuilder(inputProgram.getProgramDir());
//        String[] phases  = new String[]{"-Dmaven.compiler.useIncrementalCompilation=false", "-Dmaven.test.useIncrementalCompilation=false", "test"};
        String[] phases  = new String[]{"clean", "test"};

        builder.setGoals(phases);
        builder.initTimeOut();

        if(builder.getCompileError()) {
            return null;
        } else {
            return builder.getFailedTests();
        }
    }

    protected Set<CtClass> createMutantTestClass(CtClass mutantClass, Collection<String> failures) throws IOException, InterruptedException {
        File sourceDir = new File(inputProgram.getAbsoluteSourceCodeDir());
        File testSourceDir = new File(inputProgram.getAbsoluteTestSourceCodeDir());

        PrintClassUtils.printJavaFile(sourceDir, mutantClass);

        Map<CtClass, List<String>> failureByTestClass = failures.stream()
                .collect(Collectors.groupingBy(failure -> findTestClass(getClass(failure))));

        cloneTestClass = new HashMap<>();
        Set<CtClass> mutantTestClasses = failureByTestClass.entrySet().stream()
                .flatMap(entry -> removeFailTest(entry.getKey(), entry.getValue()).stream())
                .collect(Collectors.toSet());

        for (CtClass testClass : mutantTestClasses) {
            PrintClassUtils.printJavaFile(testSourceDir, testClass);
        }

        List<String> result = runTest();
        if(result == null || !result.isEmpty()) {
                mutantTestClasses.clear();
        }
        restore(mutantTestClasses);
        return mutantTestClasses;
    }

    Map<String, CtClass> cloneTestClass;
    protected Collection<CtClass> removeFailTest(CtClass testClass, List<String> failures) {
        Factory factory = testClass.getFactory();


        Set<String> testMthNames = failures.stream()
                .map(failure -> getMethod(failure))
                .collect(Collectors.toSet());
        Set<CtMethod> allTestMths = testClass.getAllMethods();

        for(String testMthName : testMthNames) {
            allTestMths.stream()
                    .filter(mth -> mth.getSimpleName().equals(testMthName))
                    .map(mth -> (CtClass)mth.getDeclaringType())
                    .peek(cl -> {
                        if(!cloneTestClass.containsKey(cl.getQualifiedName())) {
                            CtClass clone = factory.Core().clone(cl);
                            clone.setParent(cl.getParent());
                            cloneTestClass.put(cl.getQualifiedName(), clone);
                        }
                    })
                    .map(cl -> cloneTestClass.get(cl.getQualifiedName()))
                    .forEach(cl -> cl.getMethod(testMthName).setBody(factory.Core().createBlock()));
        }
        return cloneTestClass.values();
    }

    protected void restore(Collection<CtClass> testClasses) throws IOException {
        File sourceDir = new File(inputProgram.getAbsoluteSourceCodeDir());
        File testSourceDir = new File(inputProgram.getAbsoluteTestSourceCodeDir());

        PrintClassUtils.printJavaFile(sourceDir, originalClass);

        List<CtClass> originalTestClass = testClasses.stream()
                .map(cl -> findTestClass(cl.getQualifiedName()))
                .collect(Collectors.toList());

        for(CtClass testClass : originalTestClass) {
            PrintClassUtils.printJavaFile(testSourceDir, testClass);
        }
    }

    protected CtClass findTestClass(String className) {
        List<CtType> classes = inputProgram.getAllElement(CtType.class);

        return (CtClass) classes.stream()
                .filter(c -> c.getQualifiedName().equals(className))
                .findFirst()
                .orElse(null);
    }

    protected String getMethod(String failure) {
        String[] tmp = failure.split("\\.");

        return tmp[tmp.length -1];
    }

    protected String getClass(String failure) {

        return failure.substring(0,failure.lastIndexOf("."));
    }
}
