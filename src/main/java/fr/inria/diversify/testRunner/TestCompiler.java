package fr.inria.diversify.testRunner;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.compare.ObjectLog;
import fr.inria.diversify.dspot.TypeUtils;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;

/**
 * User: Simon
 * Date: 05/04/16
 * Time: 10:28
 */
public class TestCompiler {

    public static boolean writeAndCompile(DiversifyClassLoader applicationClassLoader, DSpotCompiler compiler, CtType classInstru, boolean withLogger) {
        try {
            FileUtils.cleanDirectory(compiler.getBinaryOutputDirectory());
            FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
        } catch (IOException | IllegalArgumentException ignored) {
            Log.warn("error during cleaning output directories");
            //ignored
        }
        if (withLogger) {
            copyLoggerFile(compiler);
        }
        try {
            PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), classInstru);
            compiler.setCustomClassLoader(applicationClassLoader);
            compiler.compileFileIn(compiler.getSourceOutputDirectory(), true);
        } catch (Exception e) {
            Log.warn("error during compilation", e);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
