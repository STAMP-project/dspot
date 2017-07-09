package fr.inria.diversify.dspot;

import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.processor.ProcessorUtil;
import fr.inria.diversify.processor.main.AddBlockEverywhereProcessor;
import fr.inria.diversify.processor.main.BranchCoverageProcessor;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.processing.Processor;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;

import java.io.File;
import java.io.IOException;


/**
 * User: Simon
 * Date: 18/05/16
 * Time: 16:10
 */
public class DSpotUtils {

    public static void addBranchLogger(InputProgram inputProgram, Factory factory) {
        try {
            applyProcessor(factory, new AddBlockEverywhereProcessor(inputProgram));

            BranchCoverageProcessor branchCoverageProcessor = new BranchCoverageProcessor(inputProgram, inputProgram.getProgramDir(), true);
            branchCoverageProcessor.setLogger(Logger.class.getCanonicalName());

            applyProcessor(factory, branchCoverageProcessor);

            File fileFrom = new File(inputProgram.getAbsoluteSourceCodeDir());
            printAllClasses(factory, fileFrom);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyLoggerPackage(InputProgram inputProgram) throws IOException {
        String loggerPackage = Logger.class.getPackage().getName().replace(".", "/");
        File destDir = new File(inputProgram.getAbsoluteSourceCodeDir() + "/" + loggerPackage);
        File srcDir = new File(System.getProperty("user.dir") + "/src/main/java/" + loggerPackage);
        FileUtils.forceMkdir(destDir);

        FileUtils.copyDirectory(srcDir, destDir);

        ProcessorUtil.writeInfoFile(inputProgram.getProgramDir());
    }

    public static void printJavaFileWithComment(CtType<?> type, File directory) {
        Factory factory = type.getFactory();
        Environment env = factory.getEnvironment();
        env.setCommentEnabled(true);
        JavaOutputProcessor processor = new JavaOutputProcessor(directory, new DefaultJavaPrettyPrinter(env));
        processor.setFactory(factory);
        processor.createJavaFile(type);
    }

    public static void printAmplifiedTestClass(CtType<?> type, File directory) {
        final String pathname = directory.getAbsolutePath() + "/" + type.getQualifiedName().replaceAll("\\.", "/") + ".java";
        if (new File(pathname).exists()) {
            printJavaFileWithComment(addGeneratedTestToExistingClass(type, pathname), directory);
        } else {
            printJavaFileWithComment(type, directory);
        }
    }

    private static CtClass<?> addGeneratedTestToExistingClass(CtType<?> type, String pathname) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource(pathname);
        launcher.buildModel();
        final CtClass<?> existingAmplifiedTest = launcher.getFactory().Class().get(type.getQualifiedName());
        type.getMethods().stream()
                .filter(testCase -> !existingAmplifiedTest.getMethods().contains(testCase))
                .forEach(existingAmplifiedTest::addMethod);
        return existingAmplifiedTest;
    }

    public static void printAllClasses(Factory factory, File out) {
        factory.Class().getAll().forEach(type -> printJavaFileWithComment(type, out));
    }

    public static void addComment(CtElement element, String content, CtComment.CommentType type) {
        CtComment comment = element.getFactory().createComment(content, type);
        if (!element.getComments().contains(comment)) {
            element.addComment(comment);
        }
    }

    public static String mavenHome;

    public static String buildMavenHome(InputConfiguration inputConfiguration) {
        if (mavenHome == null) {
            mavenHome = inputConfiguration != null && inputConfiguration.getProperty("maven.home") != null ? inputConfiguration.getProperty("maven.home") :
                    System.getenv().get("MAVEN_HOME") != null ? System.getenv().get("MAVEN_HOME") :
                            System.getenv().get("M2_HOME") != null ? System.getenv().get("M2_HOME") :
                                    new File("/usr/share/maven/").exists() ? "/usr/share/maven/" : "/usr/share/maven3/";
        }
        return mavenHome;
    }

    private static void applyProcessor(Factory factory, Processor processor) {
        QueueProcessingManager pm = new QueueProcessingManager(factory);
        pm.addProcessor(processor);
        pm.process(factory.Package().getRootPackage());
    }
}
