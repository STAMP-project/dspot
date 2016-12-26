package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.processor.ProcessorUtil;
import fr.inria.diversify.processor.main.AddBlockEverywhereProcessor;
import fr.inria.diversify.processor.main.BranchCoverageProcessor;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.*;
import spoon.compiler.Environment;
import spoon.processing.Processor;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * User: Simon
 * Date: 18/05/16
 * Time: 16:10
 */
public class DSpotUtils {

    public static void addBranchLogger(InputProgram inputProgram) {
        try {
            Factory factory = DSpotCompiler.buildCompiler(inputProgram, false).getFactory();

            applyProcessor(factory, new AddBlockEverywhereProcessor(inputProgram));

            BranchCoverageProcessor branchCoverageProcessor = new BranchCoverageProcessor(inputProgram, inputProgram.getProgramDir(), true);
            branchCoverageProcessor.setLogger(Logger.class.getCanonicalName());
            AbstractLoggingInstrumenter.reset();

            applyProcessor(factory, branchCoverageProcessor);

            File fileFrom = new File(inputProgram.getAbsoluteSourceCodeDir());
            printAllClasses(factory, fileFrom, fileFrom);

            String loggerPackage = Logger.class.getPackage().getName().replace(".", "/");
            File destDir = new File(inputProgram.getAbsoluteSourceCodeDir() + "/" + loggerPackage);
            File srcDir = new File(System.getProperty("user.dir") + "/src/main/java/" + loggerPackage);
            FileUtils.forceMkdir(destDir);

            FileUtils.copyDirectory(srcDir, destDir);

            ProcessorUtil.writeInfoFile(inputProgram.getProgramDir());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void printAllClasses(Factory factory, File out, File fileFrom) {
        //Environment env = factory.getEnvironment();
        //JavaOutputProcessorWithFilter processor = new JavaOutputProcessorWithFilter(out, new DefaultJavaPrettyPrinter(env), allClassesName(fileFrom));

        factory.Class().getAll().forEach(type -> {
            try {
                PrintClassUtils.printJavaFile(out, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Deprecated
    private static List<String> allClassesName(File dir) {
        ArrayList list = new ArrayList();
        File[] var2 = dir.listFiles();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            File file = var2[var4];
            if(file.isDirectory()) {
                list.addAll(allClassesName(file));
            } else {
                String name = file.getName();
                if(name.endsWith(".java")) {
                    String[] tmp = name.substring(0, name.length() - 5).split("/");
                    list.add(tmp[tmp.length - 1]);
                }
            }
        }

        return list;
    }

    public static DSpotCompiler initDiversityCompiler(InputProgram inputProgram, boolean withTest) throws IOException, InterruptedException {
        DSpotCompiler compiler = DSpotCompiler.buildCompiler(inputProgram, withTest);
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
        Collections.addAll(filter, inputConfiguration.getProperty("filter").split(";"));

        List<String> classPaths = new ArrayList<>();
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
        classPaths.add(inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir());

        classPaths.add(System.getProperty("user.dir") + "/target/classes/");

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
