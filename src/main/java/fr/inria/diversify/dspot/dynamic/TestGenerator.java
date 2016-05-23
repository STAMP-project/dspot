package fr.inria.diversify.dspot.dynamic;

import fr.inria.diversify.coverage.branch.Coverage;
import fr.inria.diversify.coverage.branch.CoverageReader;
import fr.inria.diversify.dspot.AssertGenerator;
import fr.inria.diversify.dspot.ClassWithLoggerBuilder;
import fr.inria.diversify.testRunner.TestRunner;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.util.Log;
import org.apache.commons.io.FileUtils;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User: Simon
 * Date: 23/03/16
 * Time: 15:36
 */
public class TestGenerator {
    protected TestMethodGenerator testMethodGenerator;
    protected TestRunner testRunner;
    protected TestRunner testRunnerWithBranchLogger;
    protected ClassWithLoggerBuilder classWithLoggerBuilder;
    protected AssertGenerator assertGenerator;
    protected File branchDir;

    protected Factory factory;
    protected Map<CtType, CtType> testClasses;

    public TestGenerator(Factory factory, TestRunner testRunner, TestRunner testRunnerWithBranchLogger, AssertGenerator assertGenerator, String branchDir) {
        this.testRunner = testRunner;
        this.testRunnerWithBranchLogger = testRunnerWithBranchLogger;
        this.assertGenerator = assertGenerator;
        this.factory = factory;
        this.branchDir = new File(branchDir + "/log");
        this.testClasses = new HashMap<>();
        this.testMethodGenerator = new TestMethodGenerator(factory);
        this.classWithLoggerBuilder = new ClassWithLoggerBuilder(factory);
    }

    public Collection<CtType> generateTestClasses(String logDir) throws IOException {
        File dir = new File(logDir);

        for(File file : dir.listFiles()) {
            if (!file.isDirectory() && file.getName().startsWith("log")) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                List<String> typesAndParameters = new ArrayList<>();
                String line = br.readLine();
                while (line != null) {
                    if (!line.contains(":")) {
                        String method = line;
                        String targetType = br.readLine();
                        try {
                            generateTest(method, targetType.substring(12, targetType.length()), typesAndParameters);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.debug("");
                        }
                        typesAndParameters.clear();
                    } else {
                        typesAndParameters.add(line);
                    }
                    line = br.readLine();
                }
            }
        }
        List<CtType> tests = getTestClasses();
        return tests.stream()
                .map(test -> minimiseTests(test))
                .map(test -> {
                    try {
                        return assertGenerator.generateAsserts(test);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(test -> test != null)
                .collect(Collectors.toList());
    }


    protected CtType minimiseTests(CtType classTest) {
        CtType cl = classWithLoggerBuilder.buildClassWithLogger(classTest, classTest.getMethods());
        try {
            fr.inria.diversify.profiling.logger.Logger.reset();
            fr.inria.diversify.profiling.logger.Logger.setLogDir(branchDir);
            testRunnerWithBranchLogger.runTests(cl, cl.getMethods());
            List<Coverage> coverage = loadBranchCoverage(branchDir.getAbsolutePath());
            Set<String> mthsSubSet = coverage.stream()
                    .collect(Collectors.groupingBy(c -> c.getCoverageBranch()))
                    .values().stream()
                    .map(value -> value.stream().findAny().get())
                    .map(c -> c.getName())
                    .collect(Collectors.toSet());

            Set<CtMethod> mths = new HashSet<>(classTest.getMethods());
            mths.stream()
                    .filter(mth -> !mthsSubSet.contains(classTest.getQualifiedName() + "." + mth.getSimpleName()))
                    .forEach(mth -> classTest.removeMethod(mth));

        } catch (Exception e) {
            e.printStackTrace();
            Log.debug("");
        }

        return classTest;
    }

    protected List<CtType>  getTestClasses() {
        return testClasses.values().stream()
                .filter(testClass -> !testClass.getMethods().isEmpty())
                .map(testClass -> {
                    CtType cl = factory.Core().clone(testClass);
                    cl.setParent(testClass.getParent());
                    return cl;
                })
                .peek(testClass -> {
                    try {
                        JunitResult result = testRunner.runTests(testClass, testClass.getMethods());
                        Set<CtMethod> tests = new HashSet<CtMethod>(testClass.getMethods());
                                tests.stream()
                                .filter(test -> result.compileOrTimeOutTestName().contains(test.getSimpleName()))
                                .forEach(test -> testClass.removeMethod((CtMethod) test));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .filter(testClass -> !testClass.getMethods().isEmpty())
                .collect(Collectors.toList());
    }

    protected void generateTest(String methodString, String targetType, List<String> typesAndParameters) {
        CtMethod method = findMethod(methodString, typesAndParameters.size());
        CtType declaringClass = method.getDeclaringType();

        if(!testClasses.containsKey(declaringClass)) {
            testClasses.put(declaringClass, generateNewTestClass(declaringClass));
        }
        CtType testClass = testClasses.get(declaringClass);
        try {
            if(!containsThis(method)) {
                testMethodGenerator.generateTestFromBody(method, testClass, typesAndParameters);
            }
            if(!isPrivate(method)) {
                testMethodGenerator.generateTestFromInvocation(method, testClass, targetType, typesAndParameters);
            }
        } catch (Exception e) {}
    }

    protected CtType generateNewTestClass(CtType classToTest) {
        CtType test = factory.Class().create(classToTest.getPackage(), classToTest.getSimpleName()+"Test");
        Set<ModifierKind> modifierKinds = new HashSet<>(test.getModifiers());
        modifierKinds.add(ModifierKind.PUBLIC);
        test.setModifiers(modifierKinds);
        return test;
    }

    protected CtMethod findMethod(String methodString, int nbParameters) {
        int index = methodString.lastIndexOf(".");
        String className = methodString.substring(0, index);
        String methodName = methodString.substring(index + 1, methodString.length());

        CtClass cl = factory.Class().get(className);

        Set<CtMethod> methods = cl.getMethods();
        return methods.stream()
                .filter(mth -> mth.getSimpleName().equals(methodName))
                .filter(mth -> mth.getParameters().size() == nbParameters)
                .findFirst()
                .orElse(null);
    }

    protected boolean isPrivate(CtMethod method) {
        return method.getModifiers().contains(ModifierKind.PRIVATE);
    }

    protected boolean containsThis(CtMethod method) {
        return !Query.getElements(method, new TypeFilter(CtThisAccess.class)).isEmpty();
    }

    protected List<Coverage> loadBranchCoverage(String logDir) throws IOException {
        List<Coverage> branchCoverage = null;
        try {
            CoverageReader branchReader = new CoverageReader(logDir);
            branchCoverage = branchReader.loadTest();

        } catch (Throwable e) {}

        deleteLogFile(logDir);

        return branchCoverage;
    }

    protected void deleteLogFile(String logDir) throws IOException {
        File dir = new File(logDir);
        for(File file : dir.listFiles()) {
            if(!file.getName().equals("info")) {
                FileUtils.forceDelete(file);
            }
        }
    }
}
