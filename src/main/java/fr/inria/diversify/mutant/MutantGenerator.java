package fr.inria.diversify.mutant;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.dspot.AmplificationChecker;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.mutant.transformation.MutationQuery;
import fr.inria.diversify.mutant.transformation.MutationTransformation;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.testRunner.JunitRunner;
import fr.inria.diversify.testRunner.TestRunner;
import fr.inria.diversify.transformation.Transformation;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.runner.notification.Failure;
import spoon.compiler.Environment;
import spoon.reflect.declaration.*;
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
    protected DSpotCompiler compiler;
    protected Set<String> filter;
    protected CtClass original;

    private Map<String, CtClass> mutantsNotKilled;

    private String currentQualifiedName;

    public MutantGenerator(InputProgram program, InputConfiguration configuration) {
        this.inputProgram = program;
        this.inputConfiguration = configuration;
        InitUtils.initLogLevel(inputConfiguration);
        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();
        try {
            initFilter();
            initCompiler(outputDirectory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MutantGenerator(String propertiesFile) throws Exception, InvalidSdkException {
        this.inputConfiguration = new InputConfiguration(propertiesFile);
        this.inputProgram = InitUtils.initInputProgram(inputConfiguration);
        InitUtils.initLogLevel(inputConfiguration);
        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();

        try {
            FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
            inputProgram.setProgramDir(outputDirectory);

            InitUtils.initDependency(inputConfiguration);
            initFilter();
            initCompiler(outputDirectory);
            this.mutantsNotKilled = new HashMap<>();
        } catch (Exception | InvalidSdkException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateForAllClasses() throws Exception {
        inputProgram.getFactory().Class().getAll().stream()
                .filter(ctClass ->
                        ctClass.getMethods().stream()
                                .filter(method ->
                                        AmplificationChecker.isTest(method, inputProgram.getRelativeTestSourceCodeDir()))
                                .count() == 0)
                .map(CtType::getQualifiedName)
                .forEach(this::generateMutant);
    }

    public void generateMutant(String fullQualifiedNameClass) {

        Log.debug("Mutating : " + fullQualifiedNameClass);

        List<CtClass> classes = inputProgram.getAllElement(CtClass.class);
        CtClass cl = classes.stream()
                .filter(c -> c.getQualifiedName().equals(fullQualifiedNameClass))
                .findFirst()
                .orElse(null);
        if (cl == null) {
            Log.warn("Could not find " + fullQualifiedNameClass);
            return ;
        }

        currentQualifiedName = fullQualifiedNameClass;
        original = cl.clone();

        Map<String, CtClass> mutants = generateAllMutant(cl);
        this.mutantsNotKilled.putAll(runMutants(mutants));
    }

    public List<CtClass>[] runTestsOnAliveMutant(InputConfiguration configuration) {
        List<CtClass>[] results = new List[2];
        results[0] = new ArrayList<>();
        results[1] = new ArrayList<>();
        List<String> classpath = Arrays.asList(compiler.getBinaryOutputDirectory().getAbsolutePath(), inputProgram.getProgramDir() + "/" +  inputProgram.getTestClassesDir());
        try {
            final InputProgram amplifiedProgram = InitUtils.initInputProgram(configuration);
            amplifiedProgram.setFactory(this.inputProgram.getFactory());
            this.mutantsNotKilled.keySet().forEach(id -> {
                CtClass mutant = this.mutantsNotKilled.get(id);
                Log.debug("run amplified test suite on {}", id);
                boolean status = writeAndCompile(mutant);
                if (status) {
                    DiversifyClassLoader classLoader = new DiversifyClassLoader(Thread.currentThread().getContextClassLoader(), classpath);
                    CtClass<Object> original = amplifiedProgram.getFactory().Class().get(mutant.getQualifiedName()).clone();
                    amplifiedProgram.getFactory().Class().get(mutant.getQualifiedName()).replace(mutant);
                    JunitResult result = new JunitRunner(classLoader).runAllTestClasses(amplifiedProgram);
                    if (!result.failureTests().isEmpty()) {
                        Log.debug("{} has been killed", id);
                        results[0].add(mutant);
                    } else {
                        Log.debug("{} still alive", id);
                        results[1].add(mutant);
                    }
                    amplifiedProgram.getFactory().Class().get(mutant.getQualifiedName()).replace(original);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    private Map<String, CtClass> runMutants(Map<String, CtClass> mutants) {
        Map<String, CtClass> mutantsNotKilled = new HashMap<>();
        List<String> classpath = Arrays.asList(compiler.getBinaryOutputDirectory().getAbsolutePath(), inputProgram.getProgramDir() + "/" +  inputProgram.getTestClassesDir());
        for (String mutantId : mutants.keySet()) {
            CtClass mutant = mutants.get(mutantId);
            Log.debug("run mutant: {}", mutantId);
            boolean status = writeAndCompile(mutant);
            if (status) {
                inputProgram.getFactory().Class().get(currentQualifiedName).replace(mutant);
                DiversifyClassLoader classLoader = new DiversifyClassLoader(Thread.currentThread().getContextClassLoader(), classpath);
                JunitResult result = new JunitRunner(classLoader).runTestForMutant(inputProgram, mutant.getQualifiedName());
                List<String> failures = result.failureTests();
                if (failures.isEmpty()) {
                    Log.debug("{} is not killed by the current test suite", mutantId);
                    mutantsNotKilled.put(mutantId, mutant);
                }
                Log.debug("number of test failure: {}", failures.size());
            } else {
                Log.debug("mutant {} can not be compiled", mutantId);
            }
        }
        inputProgram.getFactory().Class().get(currentQualifiedName).replace(original);
        return mutantsNotKilled;
    }

    private DiversifyClassLoader buildClassLoader() {
        List<String> classPaths = new ArrayList<>(2);
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir());
        return new DiversifyClassLoader(Thread.currentThread().getContextClassLoader(), classPaths);
    }

    private Map<String, CtClass> generateAllMutant(CtClass cl) {
        Map<String, CtClass> mutants = new HashMap<>();
        MutationQuery query = new MutationQuery(inputProgram);
        Map<String, MutationTransformation> transformations = query.getAllTransformationFor(cl);

        String tmpDir = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();
        File tmpDirFile = new File(tmpDir);
        tmpDirFile.mkdirs();

        for (String id : transformations.keySet()) {
            try {
                Transformation trans = transformations.get(id);
                trans.apply(tmpDir);

                CtClass mutant = cl.clone();
                mutant.setParent(cl.getParent());
                mutants.put(id, mutant);

                trans.restore(tmpDir);

                FileUtils.forceDelete(tmpDirFile);
            } catch (Exception e) {
                Log.warn("Something went wrong during a transformations: " + id + " skipping...");
            }
        }
        return mutants;
    }

    private void initFilter() {
        filter = new HashSet<>();
        Collections.addAll(filter, inputConfiguration.getProperty("filter").split(";"));
    }

    private void initCompiler(String tmpDir) throws IOException, InterruptedException {
        compiler = DSpotCompiler.buildCompiler(inputProgram, true);
        compileClasses();

        if (compiler.getBinaryOutputDirectory() == null) {
            File classOutputDir = new File(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
            if (!classOutputDir.exists()) {
                classOutputDir.mkdirs();
            }
            compiler.setBinaryOutputDirectory(classOutputDir);
        }
        if (compiler.getSourceOutputDirectory().toString().equals("spooned")) {
            File sourceOutputDir = new File(tmpDir + "/tmpSrc");
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

    private void compileClasses() throws InterruptedException, IOException {
        String[] phases = new String[]{"clean", "test-compile"};
        MavenBuilder builder = new MavenBuilder(inputProgram.getProgramDir());
        builder.setGoals(phases);
        builder.initTimeOut();
    }

    private boolean writeAndCompile(CtClass classInstru) {
        try {
            FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
            PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), classInstru);
            return compiler.compileFileIn(compiler.getSourceOutputDirectory(), true);
        } catch (Exception e) {
            Log.warn("error during compilation", e);
            return false;
        }
    }

    public static void main(String[] args) throws InvalidSdkException, Exception {
        fr.inria.diversify.mutant.MutantGenerator mutantGenerator = new fr.inria.diversify.mutant.MutantGenerator(args[0]);
        mutantGenerator.generateForAllClasses();
    }
}