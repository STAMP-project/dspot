package fr.inria.diversify.testRunner;

import fr.inria.diversify.compare.ObjectLog;
import fr.inria.diversify.dspot.TypeUtils;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import static org.codehaus.plexus.util.FileUtils.forceDelete;

/**
 * User: Simon
 * Date: 05/04/16
 * Time: 10:28
 */
public class TestCompiler {

    public static boolean writeAndCompile(DSpotCompiler compiler, CtType classTest, boolean withLogger, String dependencies) {
        if (withLogger) {
            copyLoggerFile(compiler);
        }
        try {
            PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), classTest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            String pathToDotClass = compiler.getBinaryOutputDirectory().getAbsolutePath() + "/" + classTest.getQualifiedName().replaceAll("\\.", "/") + ".class";
            forceDelete(pathToDotClass);
        } catch (IOException ignored) {
            //ignored
        }

        try {
            compiler.compile(dependencies);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static void copyLoggerFile(DSpotCompiler compiler) {
        try {
            String comparePackage = ObjectLog.class.getPackage().getName().replace(".", "/");
            File srcDir = new File(System.getProperty("user.dir") + "/src/main/java/" + comparePackage);

            File destDir = new File(compiler.getSourceOutputDirectory() + "/" + comparePackage);
            FileUtils.forceMkdir(destDir);

            FileUtils.copyDirectory(srcDir, destDir);

            String typeUtilsPackage = TypeUtils.class.getPackage().getName().replace(".", "/");
            File srcFile = new File(System.getProperty("user.dir") + "/src/main/java/" + typeUtilsPackage + "/TypeUtils.java");

            destDir = new File(compiler.getSourceOutputDirectory() + "/" + typeUtilsPackage);
            FileUtils.forceMkdir(destDir);

            File destFile = new File(compiler.getSourceOutputDirectory() + "/" + typeUtilsPackage + "/TypeUtils.java");
            FileUtils.copyFile(srcFile, destFile);
        } catch (FileAlreadyExistsException ignored) {
            //skip
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
