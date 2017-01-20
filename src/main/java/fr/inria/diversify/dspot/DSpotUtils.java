package fr.inria.diversify.dspot;

import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.processor.ProcessorUtil;
import fr.inria.diversify.processor.main.AddBlockEverywhereProcessor;
import fr.inria.diversify.processor.main.BranchCoverageProcessor;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import spoon.compiler.Environment;
import spoon.processing.Processor;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;
import spoon.support.QueueProcessingManager;

import java.io.File;

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
            AbstractLoggingInstrumenter.reset();

            applyProcessor(factory, branchCoverageProcessor);

            File fileFrom = new File(inputProgram.getAbsoluteSourceCodeDir());
            printAllClasses(factory, fileFrom);

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

    public static void printJavaFileWithComment(CtType type, File directory) {
        Factory factory = type.getFactory();
        Environment env = factory.getEnvironment();
        env.setCommentEnabled(true);
        JavaOutputProcessor processor = new JavaOutputProcessor(directory, new DefaultJavaPrettyPrinter(env));
        processor.setFactory(factory);
        processor.createJavaFile(type);
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

    private static void applyProcessor(Factory factory, Processor processor) {
        QueueProcessingManager pm = new QueueProcessingManager(factory);
        pm.addProcessor(processor);
        pm.process(factory.Package().getRootPackage());
    }
}
