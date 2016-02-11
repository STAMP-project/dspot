package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.processor.*;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.profiling.coverage.Coverage;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.JunitRunner;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import org.apache.commons.io.FileUtils;
import spoon.compiler.Environment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.JavaOutputProcessor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 03/12/15
 * Time: 13:52
 */
public class Amplification {
    protected DiversifyClassLoader applicationClassLoader;
    protected InputProgram inputProgram;
    protected List<AbstractAmp> amplifiers;
    protected DiversityCompiler compiler;
    protected TestSelector testSelector;
    protected Map<Boolean, List<CtMethod>> testsStatus;

    public Amplification(InputProgram inputProgram, DiversityCompiler compiler, Set<String> classLoaderFilter, List<AbstractAmp> amplifiers) {
        this.inputProgram = inputProgram;
        this.compiler = compiler;
        testSelector = new TestSelector(inputProgram, 10);

        this.amplifiers = amplifiers;
        initClassLoader(classLoaderFilter);
        initCompiler();
    }

    public CtClass amplification(CtClass classTest, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        return amplification(classTest, getAllTest(classTest), maxIteration);
    }

    public CtClass amplification(CtClass classTest, List<CtMethod> methods, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        List<CtMethod> tests = methods.stream()
                .filter(mth -> isTest(mth))
                .collect(Collectors.toList());

        if(tests.isEmpty()) {
            return null;
        }
        CtClass classWithLogger = testSelector.buildClassWithLogger(classTest, tests);
        boolean status = writeAndCompile(classWithLogger);
        if(!status) {
            Log.info("error whit Logger in class {}", classTest);
            return null;
        }
        runTests(classWithLogger, tests);
        testSelector.updateLogInfo();
        resetAmplifiers(classTest, testSelector.getGlobalCoverage());

        Log.info("amplification of {} ({} test)", classTest.getQualifiedName(), tests.size());

        List<CtMethod> ampTest = new ArrayList<>();
        List<CtMethod> testsToRemove = new ArrayList<>();
        for(int i = 0; i < tests.size(); i++) {
            Log.debug("amp {} ({}/{})", tests.get(i).getSimpleName(), i+1, tests.size());
            testSelector.init();

            classWithLogger = testSelector.buildClassWithLogger(classTest, tests.get(i));
            writeAndCompile(classWithLogger);

            JunitResult result = runTest(classWithLogger, tests.get(i));
            if(result != null
                    && result.getFailures().isEmpty()) {
                testSelector.updateLogInfo();

                amplification(classTest, tests.get(i), maxIteration);

                ampTest.addAll(testSelector.selectedAmplifiedTests(testsStatus.get(false)));
                ampTest.addAll(testSelector.selectedAmplifiedTests(testsStatus.get(true)));

                Log.debug("total amp test: {}", ampTest.size());
            } else {
              testsToRemove.add(tests.get(i));
            }
        }
        Log.debug("assert generation");
        return makeDSpotClassTest(classTest, ampTest, testsToRemove);
    }



    protected void amplification(CtClass originalClass, CtMethod test, int maxIteration) throws IOException, InterruptedException, ClassNotFoundException {
        testsStatus();
        List<CtMethod> newTests = new ArrayList<>();
        Collection<CtMethod> ampTests = new ArrayList<>();
        newTests.add(test);
        ampTests.add(test);

        for (int i = 0; i < maxIteration; i++) {
            Log.debug("iteration {}:", i);

            Collection<CtMethod> testToAmp = testSelector.selectTestToAmp(ampTests, newTests);
            if(testToAmp.isEmpty()) {
                break;
            }
            Log.debug("{} tests selected to be amplified", testToAmp.size());
            newTests = ampTest(testToAmp);
            Log.debug("{} new tests generated", newTests.size());

            newTests = reduce(newTests);

            CtClass classWithLogger = testSelector.buildClassWithLogger(originalClass, newTests);
            boolean status = writeAndCompile(classWithLogger);
            if(!status) {
                break;
            }
            Log.debug("run tests");
            JunitResult result = runTests(classWithLogger, newTests);
            if(result == null) {
                break;
            }
            newTests = filterTest(newTests, result);
            ampTests.addAll(newTests);
            saveTestStatus(newTests, result);
            Log.debug("update coverage info");
            testSelector.updateLogInfo();
        }
    }

    protected List<CtMethod> reduce(List<CtMethod> newTests) {
        Random r = new Random();
        while(newTests.size() > 6000) {
            newTests.remove(r.nextInt(newTests.size()));
        }
        return newTests;
    }

    protected void saveTestStatus(Collection<CtMethod> newTests, JunitResult result) {
        List<String> runTests = result.runTests();
        List<String> failedTests = result.failureTests();
        newTests.stream()
                .filter(test -> runTests.contains(test.getSimpleName()))
                .forEach(test -> {
                    if(failedTests.contains(test.getSimpleName())) {
                        testsStatus.get(false).add(test);
                    } else {
                        testsStatus.get(true).add(test);
                    }
                });
    }

    protected List<CtMethod> filterTest(List<CtMethod> newTests, JunitResult result) {
        List<String> goodTests = result.goodTests();
        return newTests.stream()
                .filter(test -> goodTests.contains(test.getSimpleName()))
                .collect(Collectors.toList());
    }

    protected void testsStatus()  {
        testsStatus = new HashMap<>();
        testsStatus.put(true, new ArrayList<>());
        testsStatus.put(false, new ArrayList<>());
    }

    protected void initCompiler() {
        if(compiler.getBinaryOutputDirectory() == null) {
            File classOutputDir = new File("tmpDir/tmpClasses_" + System.currentTimeMillis());
            if (!classOutputDir.exists()) {
                classOutputDir.mkdirs();
            }
            compiler.setBinaryOutputDirectory(classOutputDir);
        }
        if(compiler.getSourceOutputDirectory().toString().equals("spooned")) {
            File sourceOutputDir = new File("tmpDir/tmpSrc_" + System.currentTimeMillis());
            if (!sourceOutputDir.exists()) {
                sourceOutputDir.mkdirs();
            }
            compiler.setSourceOutputDirectory(sourceOutputDir);
        }

        Environment env = compiler.getFactory().getEnvironment();
        env.setDefaultFileGenerator(new JavaOutputProcessor(compiler.getSourceOutputDirectory(),
                new DefaultJavaPrettyPrinter(env)));
    }

    protected boolean writeAndCompile(CtClass classInstru) throws IOException {
        FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
        FileUtils.cleanDirectory(compiler.getBinaryOutputDirectory());
        try {
            PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), classInstru);
            compiler.compileFileIn(compiler.getSourceOutputDirectory(), false);
            return true;
        } catch (Exception e) {
            Log.warn("error during compilation",e);
            return false;
        }
    }

    protected JunitResult runTest(CtClass testClass, CtMethod test) throws ClassNotFoundException {
        List<CtMethod> tests = new ArrayList<>(1);
        tests.add(test);
        return runTests(testClass, tests);
    }

    protected JunitResult runTests(CtClass testClass, Collection<CtMethod> tests) throws ClassNotFoundException {
        JunitRunner junitRunner = new JunitRunner(inputProgram, new DiversifyClassLoader(applicationClassLoader, compiler.getBinaryOutputDirectory().getAbsolutePath()));

        return junitRunner.runTestClass(testClass.getQualifiedName(), tests.stream()
                .map(test-> test.getSimpleName())
                .collect(Collectors.toList()));
    }

    protected List<CtMethod> ampTest(Collection<CtMethod> tests) {
        return tests.stream()
                .flatMap(test -> ampTest(test).stream())
                .collect(Collectors.toList());
    }

    protected List<CtMethod> ampTest(CtMethod test) {
        return amplifiers.stream().
                flatMap(amplifier -> amplifier.apply(test).stream())
                .collect(Collectors.toList());
    }

    protected void resetAmplifiers(CtClass parentClass, Coverage coverage) {
        amplifiers.stream()
                .forEach(amp -> amp.reset(inputProgram, coverage, parentClass));
    }

    protected void initClassLoader(Set<String> classLoaderFilter) {
        List<String> classPaths = new ArrayList<>();
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir());
        applicationClassLoader = new DiversifyClassLoader(Thread.currentThread().getContextClassLoader(), classPaths);
        applicationClassLoader.setClassFilter(classLoaderFilter);
    }

    protected List<CtMethod> getAllTest(CtClass classTest) {
        Set<CtMethod> mths = classTest.getMethods();
        return mths.stream()
                .filter(mth -> isTest(mth))
                .distinct()
                .collect(Collectors.toList());
    }

    protected boolean isTest(CtMethod candidate) {
        if(candidate.isImplicit()
                || candidate.getVisibility() == null
                || !candidate.getVisibility().equals(ModifierKind.PUBLIC)
                || candidate.getBody() == null
                || candidate.getBody().getStatements().size() == 0) {
            return false;
        }

        if(!candidate.getPosition().getFile().toString().contains(inputProgram.getRelativeTestSourceCodeDir())) {
            return false;
        }

        return candidate.getSimpleName().contains("test")
                || candidate.getAnnotations().stream()
                    .map(annotation -> annotation.toString())
                    .anyMatch(annotation -> annotation.startsWith("@org.junit.Test"));
    }

    protected CtClass makeDSpotClassTest(CtClass originalClass, Collection<CtMethod> ampTests, Collection<CtMethod> testToRemove) throws IOException, ClassNotFoundException {
        CtClass cloneClass = originalClass.getFactory().Core().clone(originalClass);
        cloneClass.setParent(originalClass.getParent());

        AssertGenerator ag = new AssertGenerator(originalClass, inputProgram, compiler, applicationClassLoader);
        for(CtMethod test : ampTests) {
            CtMethod ampTest = ag.generateAssert(test, findStatementToAssert(test));
            if(ampTest != null) {
                cloneClass.addMethod(ampTest);
            }
        }
        PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), cloneClass);

        return cloneClass;
    }

    protected List<Integer> findStatementToAssert(CtMethod test) {
        CtMethod originalTest = getOriginalTest(test);
        List<CtStatement> originalStmts = Query.getElements(originalTest, new TypeFilter(CtStatement.class));
        List<String> originalStmtStrings = originalStmts.stream()
                .map(stmt -> stmt.toString())
                .collect(Collectors.toList());

        List<CtStatement> ampStmts = Query.getElements(test, new TypeFilter(CtStatement.class));
        List<String> ampStmtStrings = ampStmts.stream()
                .map(stmt -> stmt.toString())
                .collect(Collectors.toList());

        List<Integer> indexs = new ArrayList<>();
        for(int i = 0; i < ampStmtStrings.size(); i++) {
            int index = originalStmtStrings.indexOf(ampStmtStrings.get(i));
            if(index == -1) {
                indexs.add(i);
            } else {
                originalStmtStrings.remove(index);
            }
        }

        return indexs;
    }

    protected CtMethod getOriginalTest(CtMethod test) {
        CtMethod parent = AbstractAmp.getAmpTestToParent().get(test);
        while(AbstractAmp.getAmpTestToParent().get(parent) != null) {
            parent = AbstractAmp.getAmpTestToParent().get(parent);
        }
        return parent;
    }
}
