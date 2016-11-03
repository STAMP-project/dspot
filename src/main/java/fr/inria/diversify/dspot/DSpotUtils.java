package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.processor.ProcessorUtil;
import fr.inria.diversify.processor.main.AddBlockEverywhereProcessor;
import fr.inria.diversify.processor.main.BranchCoverageProcessor;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.compiler.Environment;
import spoon.processing.Processor;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Simon
 * Date: 18/05/16
 * Time: 16:10
 */
public class DSpotUtils {

    public static void addBranchLogger(InputProgram inputProgram) throws IOException {
        Factory factory = InitUtils.initSpoon(inputProgram, false);

        applyProcessor(factory, new AddBlockEverywhereProcessor(inputProgram));

        BranchCoverageProcessor m = new BranchCoverageProcessor(inputProgram, inputProgram.getProgramDir(), true);
        m.setLogger(Logger.class.getCanonicalName());
        AbstractLoggingInstrumenter.reset();
        applyProcessor(factory, m);

        File fileFrom = new File(inputProgram.getAbsoluteSourceCodeDir());
        PrintClassUtils.printAllClasses(factory, fileFrom, fileFrom);

        String loggerPackage = Logger.class.getPackage().getName().replace(".", "/");
        File destDir = new File(inputProgram.getAbsoluteSourceCodeDir() + "/" + loggerPackage);
        File srcDir = new File(System.getProperty("user.dir") + "/src/main/java/" + loggerPackage);
        FileUtils.forceMkdir(destDir);

        FileUtils.copyDirectory(srcDir, destDir);

        ProcessorUtil.writeInfoFile(inputProgram.getProgramDir());
    }

    public static DiversityCompiler initDiversityCompiler(InputProgram inputProgram, boolean withTest) throws IOException, InterruptedException {
        DiversityCompiler compiler = InitUtils.initSpoonCompiler(inputProgram, withTest);
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

        return compiler;
    }

    public static void compileTests(InputProgram inputProgram, String mavenHome, String mavenLocalRepository) throws InterruptedException, IOException {
        String[] phases  = new String[]{"clean", "test"};
        pCompile(inputProgram, phases, mavenHome, mavenLocalRepository);
    }

    public static void compile(InputProgram inputProgram, String mavenHome, String mavenLocalRepository) throws InterruptedException, IOException {
        String[] phases  = new String[]{"clean", "compile"};
        pCompile(inputProgram, phases, mavenHome, mavenLocalRepository);
    }

    protected static void pCompile(InputProgram inputProgram,  String[] phases, String mavenHome, String mavenLocalRepository) throws InterruptedException, IOException {
        MavenBuilder builder = new MavenBuilder(inputProgram.getProgramDir());

        builder.setBuilderPath(mavenHome);
        if(mavenLocalRepository != null) {
            builder.setSetting(new File(mavenLocalRepository));
        }

        builder.setGoals(phases);
        builder.initTimeOut();
        InitUtils.addApplicationClassesToClassPath(inputProgram);
    }

    public static DiversifyClassLoader initClassLoader(InputProgram inputProgram, InputConfiguration inputConfiguration) {
        Set<String> filter = new HashSet<>();
        for(String s : inputConfiguration.getProperty("filter").split(";") ) {
            filter.add(s);
        }

        List<String> classPaths = new ArrayList<>();
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir());

        DiversifyClassLoader applicationClassLoader = new DiversifyClassLoader(Thread.currentThread().getContextClassLoader(), classPaths);
        applicationClassLoader.setClassFilter(filter);

        return applicationClassLoader;
    }

    protected static void applyProcessor(Factory factory, Processor processor) {
        QueueProcessingManager pm = new QueueProcessingManager(factory);
        pm.addProcessor(processor);
        pm.process(factory.Package().getRootPackage());
    }
}
