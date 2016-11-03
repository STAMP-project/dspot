package fr.inria.diversify.mutant;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.mutant.transformation.MutationQuery;
import fr.inria.diversify.mutant.transformation.MutationTransformation;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.JunitRunner;
import fr.inria.diversify.transformation.Transformation;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.runner.notification.Failure;
import spoon.compiler.Environment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 08/01/16
 * Time: 10:04
 */
public class MutantGenerator {
    protected final InputProgram inputProgram;
    protected InputConfiguration inputConfiguration;
    protected DiversityCompiler compiler;
    protected Set<String> filter;
    protected CtClass original;

    public MutantGenerator(String propertiesFile) throws Exception, InvalidSdkException {
        inputConfiguration = new InputConfiguration(propertiesFile);
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);

        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();

        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
        inputProgram.setProgramDir(outputDirectory);

        InitUtils.initDependency(inputConfiguration);
        initFilter();
        initCompiler(outputDirectory);

    }


    public void generateMutant(String className) throws Exception {
        List<CtClass> classes = inputProgram.getAllElement(CtClass.class);
        CtClass cl = classes.stream()
                .filter(c -> c.getQualifiedName().equals(className))
                .findFirst()
                .orElse(null);

        original = cl;

        Map<String, CtClass> mutants = generateAllMutant(cl);
        Map<String, List<String>> mutantsFailures = runMutants(mutants);

        createMutantTestSuite(mutants, mutantsFailures);
        writeReport(mutantsFailures);
    }

    protected void createMutantTestSuite(Map<String, CtClass> mutants, Map<String, List<String>> failures) throws IOException, GitAPIException {
        String repo = inputConfiguration.getProperty("result") + "/mutant/" + original.getQualifiedName() + "/";
        MutantTestSuiteBuilder mutantTestSuiteBuilder = new MutantTestSuiteBuilder(inputProgram, original, repo);

        List<String> keys = new ArrayList<>(failures.keySet());
        Collections.shuffle(keys);
        List<String> keySorted = keys.stream()
                .filter(key -> !failures.get(key).isEmpty())
                .sorted((key1, key2) -> failures.get(key1).size() - failures.get(key2).size())
                .collect(Collectors.toList());


        List<String> keySelected = new ArrayList<>(10);
        if(keySorted.size() > 10) {
            keySelected.addAll(keySorted.subList(0, 5));
            keySelected.addAll(keySorted.subList(keySorted.size() - 6, keySorted.size() - 1));
        } else {
            keySelected.addAll(keySorted);
        }

        for(String mutantId : keySelected) {
            try {
                List<String> failureSet = failures.get(mutantId);
                if(!failureSet.isEmpty()) {
                    mutantTestSuiteBuilder.addMutant(mutants.get(mutantId), failureSet, mutantId);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected Map<String, List<String>> runMutants(Map<String, CtClass> mutants) throws Exception {
        Map<String, List<String>> mutantsFailures = new HashMap<>();
//        Log.debug("init failure filter");
//        Set<String> failureFilter = initFailureFilter();

        for(String mutantId : mutants.keySet()) {
            try {
                CtClass mutant = mutants.get(mutantId);
                Log.debug("run mutant: {}", mutantId);
                boolean status = writeAndCompile(mutant);
                if (status) {
//                    Result result = runTests(buildClassLoader());
//                    Set<Failure> failures = getFailures(result, failureFilter);
                    PrintClassUtils.printJavaFile(new File(inputProgram.getAbsoluteSourceCodeDir()), mutant);
                    List<String> failures = runTest();
                    mutantsFailures.put(mutantId, failures);
                    Log.debug("number of test failure: {}", failures.size());
                }else{
                    Log.debug("mutant not compile");
                }
                PrintClassUtils.printJavaFile(new File(inputProgram.getAbsoluteSourceCodeDir()), original);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return mutantsFailures;
    }

    protected List<String> runTest() throws InterruptedException, IOException {
        String[] phases  = new String[]{"clean", "test"};
        MavenBuilder builder = new MavenBuilder(inputProgram.getProgramDir());
        builder.setGoals(phases);
        builder.setTimeOut(100);
        builder.runBuilder();

        return builder.getFailedTests();
    }

    protected DiversifyClassLoader buildClassLoader() {
        List<String> classPaths = new ArrayList<>(2);
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir());
        return new DiversifyClassLoader(Thread.currentThread().getContextClassLoader(), classPaths);
    }

    protected Set<Failure> getFailures(JunitResult result, Set<String> failureFilter) {
        return result.getFailures().stream()
                .filter(failure -> !failureFilter.contains(failure.getDescription().getMethodName()))
                .collect(Collectors.toSet());
    }

    protected Set<String> initFailureFilter() throws IOException, ClassNotFoundException {
        JunitResult result = runTests(buildClassLoader());

       return result.getFailures().stream()
               .map(failure -> failure.getDescription().getMethodName())
               .collect(Collectors.toSet());
    }

    protected JunitResult runTests(DiversifyClassLoader classLoader) throws ClassNotFoundException {
        JunitRunner junitRunner = new JunitRunner(inputProgram, classLoader);

        List<String> testsName = getAllTest().stream()
                .map(test -> test.getQualifiedName())
                .collect(Collectors.toList());
        return junitRunner.runTestClasses(testsName);
    }

    protected Map<String, CtClass> generateAllMutant(CtClass cl) throws Exception {
        Map<String, CtClass> mutants = new HashMap<>();
        MutationQuery query = new MutationQuery(inputProgram);
        Map<String, MutationTransformation> transformations = query.getAllTransformationFor(cl);

        String tmpDir = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();
        File tmpDirFile = new File(tmpDir);
        tmpDirFile.mkdirs();

        for(String id : transformations.keySet()) {
            try {
                Transformation trans = transformations.get(id);
                trans.apply(tmpDir);

                CtClass mutant = inputProgram.getFactory().Core().clone(cl);
                mutant.setParent(cl.getParent());
                mutants.put(id, mutant);

                trans.restore(tmpDir);
            } catch (Exception e) {}
        }

        FileUtils.forceDelete(tmpDirFile);
        return mutants;
    }

    protected void writeReport(Map<String, List<String>> mutantsFailures) throws IOException {
        String dir = inputConfiguration.getProperty("result") + "/mutant/" + original.getQualifiedName() + "/report";
        FileWriter writer = new FileWriter(dir);

        for(String id : mutantsFailures.keySet()) {
            writer.write(id + ": " + mutantsFailures.get(id) + "\n");
        }
        writer.close();
    }

    protected void initFilter() {
        filter = new HashSet<>();
        for(String s : inputConfiguration.getProperty("filter").split(";") ) {
            filter.add(s);
        }
    }

    protected void initCompiler(String tmpDir) throws IOException, InterruptedException {
        compiler = InitUtils.initSpoonCompiler(inputProgram, true);
        compileClasses();

        if(compiler.getBinaryOutputDirectory() == null) {
            File classOutputDir = new File(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
            if (!classOutputDir.exists()) {
                classOutputDir.mkdirs();
            }
            compiler.setBinaryOutputDirectory(classOutputDir);
        }
        if(compiler.getSourceOutputDirectory().toString().equals("spooned")) {
            File sourceOutputDir = new File(tmpDir + "/tmpSrc" );
            if (!sourceOutputDir.exists()) {
                sourceOutputDir.mkdirs();
            }
            compiler.setSourceOutputDirectory(sourceOutputDir);
        }
        compiler.setCustomClassLoader(buildClassLoader());

        Environment env = compiler.getFactory().getEnvironment();
        env.setDefaultFileGenerator(new JavaOutputProcessor(compiler.getSourceOutputDirectory(),
                new DefaultJavaPrettyPrinter(env)));
    }

    protected void compileClasses() throws InterruptedException, IOException {
        String[] phases  = new String[]{"clean", "test-compile"};
        MavenBuilder builder = new MavenBuilder(inputProgram.getProgramDir());

        builder.setGoals(phases);
        builder.initTimeOut();
    }

    protected boolean writeAndCompile(CtClass classInstru) throws IOException {
        FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
        try {
            PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), classInstru);
            return compiler.compileFileIn(compiler.getSourceOutputDirectory(), true);
        } catch (Exception e) {
            Log.warn("error during compilation",e);
            return false;
        }
    }

    protected List<CtClass> getAllTest() {
        List<CtClass> classes = inputProgram.getAllElement(CtClass.class);

        return classes.stream()
                .filter(cl -> !cl.getModifiers().contains(ModifierKind.ABSTRACT))
                .filter(cl -> cl.getSimpleName().startsWith("Test") || cl.getSimpleName().endsWith("Test"))
                .filter(cl -> cl.getPosition().getFile().toString().contains(inputProgram.getRelativeTestSourceCodeDir()))
                .collect(Collectors.toList());
    }

    public static void main(String[] args) throws InvalidSdkException, Exception {
        MutantGenerator mutantGenerator = new MutantGenerator(args[0]);
        mutantGenerator.generateMutant(args[1]);
    }
}