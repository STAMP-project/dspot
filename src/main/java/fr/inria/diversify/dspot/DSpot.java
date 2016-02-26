package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.amp.*;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.profiling.logger.Logger;
import fr.inria.diversify.profiling.processor.ProcessorUtil;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.profiling.processor.main.BranchCoverageProcessor;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.LoggerUtils;
import fr.inria.diversify.util.PrintClassUtils;
import org.apache.commons.io.FileUtils;
import spoon.compiler.Environment;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * User: Simon
 * Date: 08/06/15
 * Time: 17:36
 */
public class DSpot {
    protected DiversityCompiler compiler;
    protected InputProgram inputProgram;
    protected DiversifyClassLoader applicationClassLoader;
    protected AssertGenerator assertGenerator;

    protected static DiversifyClassLoader regressionClassLoader;

    public DSpot(InputConfiguration inputConfiguration) throws InvalidSdkException, Exception {
        InitUtils.initLogLevel(inputConfiguration);
        inputProgram = InitUtils.initInputProgram(inputConfiguration);

        String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();

        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
        inputProgram.setProgramDir(outputDirectory);

        InitUtils.initDependency(inputConfiguration);
        initClassLoader(inputConfiguration);
        initDiversityCompiler();

        assertGenerator = new AssertGenerator(inputProgram, compiler, applicationClassLoader);
        InitUtils.initLogLevel(inputConfiguration);
    }

    public DSpot(InputConfiguration inputConfiguration, DiversifyClassLoader classLoader) throws Exception, InvalidSdkException {
        this(inputConfiguration);
        regressionClassLoader = classLoader;
    }

    public CtType generateTest(String fullName) throws InterruptedException, IOException, ClassNotFoundException {
        return generateTest(inputProgram.getFactory().Type().get(fullName));
    }

    public CtType generateTest(CtType test) throws IOException, InterruptedException, ClassNotFoundException {
        Amplification testAmplification = new Amplification(inputProgram, compiler, applicationClassLoader, initAmplifiers());

        List<CtMethod> ampTests = testAmplification.amplification(test, 3);
        return assertGenerator.makeDSpotClassTest(test, ampTests);
    }

    public CtType generateTest(List<CtMethod> tests, CtType testClass) throws IOException, InterruptedException, ClassNotFoundException {
        Amplification testAmplification = new Amplification(inputProgram, compiler, applicationClassLoader, initAmplifiers());

        List<CtMethod> ampTests = testAmplification.amplification(testClass, tests, 3);
        return assertGenerator.makeDSpotClassTest(testClass, ampTests);
    }

    protected List<AbstractAmp> initAmplifiers() {
        List<AbstractAmp> amplifiers = new ArrayList<>();

        amplifiers.add(new TestDataMutator());
        amplifiers.add(new TestMethodCallAdder());
        amplifiers.add(new TestMethodCallRemover());
        amplifiers.add(new StatementAdder());

        return amplifiers;
    }

    protected void initDiversityCompiler() throws IOException, InterruptedException {
        addBranchLogger();
        compiler = InitUtils.initSpoonCompiler(inputProgram, true);
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

        compileTests();
}

    protected void compileTests() throws InterruptedException, IOException {
        String[] phases  = new String[]{"clean", "test"};
        MavenBuilder builder = new MavenBuilder(inputProgram.getProgramDir());

        builder.setGoals(phases);
        builder.initTimeOut();
        InitUtils.addApplicationClassesToClassPath(inputProgram);
    }

    protected void initClassLoader(InputConfiguration inputConfiguration) {
        Set<String> filter = new HashSet<>();
        for(String s : inputConfiguration.getProperty("filter").split(";") ) {
            filter.add(s);
        }

        List<String> classPaths = new ArrayList<>();
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir());

        applicationClassLoader = new DiversifyClassLoader(Thread.currentThread().getContextClassLoader(), classPaths);
        applicationClassLoader.setClassFilter(filter);
    }


    protected void addBranchLogger() throws IOException {
        Factory factory = InitUtils.initSpoon(inputProgram, false);

        BranchCoverageProcessor m = new BranchCoverageProcessor(inputProgram, inputProgram.getProgramDir(), true);
        m.setLogger(Logger.class.getCanonicalName());
        AbstractLoggingInstrumenter.reset();
        LoggerUtils.applyProcessor(factory, m);

        File fileFrom = new File(inputProgram.getAbsoluteSourceCodeDir());
        PrintClassUtils.printAllClasses(factory, fileFrom, fileFrom);

        String loggerPackage = Logger.class.getPackage().getName().replace(".", "/");
        File destDir = new File(inputProgram.getAbsoluteSourceCodeDir() + "/" + loggerPackage);
        File srcDir = new File(System.getProperty("user.dir") + "/src/main/java/" + loggerPackage);
        FileUtils.forceMkdir(destDir);

        FileUtils.copyDirectory(srcDir, destDir);

        ProcessorUtil.writeInfoFile(inputProgram.getProgramDir());
    }

    public void clean() throws IOException {
        FileUtils.forceDelete(compiler.getSourceOutputDirectory());
        FileUtils.forceDelete(compiler.getBinaryOutputDirectory());
        FileUtils.forceDelete(new File(inputProgram.getProgramDir()));
    }

    public InputProgram getInputProgram() {
        return inputProgram;
    }

    protected static void kill() throws IOException {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        Runtime r = Runtime.getRuntime();
        r.exec("kill "+pid);
    }

    public static void main(String[] args) throws Exception, InvalidSdkException {
        InputConfiguration inputConfiguration = new InputConfiguration(args[0]);
        String testClass = inputConfiguration.getProperty("testClass");

        DSpot dspot = new DSpot(inputConfiguration);
        CtType ampTest = dspot.generateTest(dspot.inputProgram.getFactory().Type().get(testClass));
        PrintClassUtils.printJavaFile(new File(inputConfiguration.getProperty("result")), ampTest);
        dspot.kill();
    }

}
