package fr.inria.diversify.testRunner;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.declaration.CtType;

import java.io.IOException;

/**
 * User: Simon
 * Date: 05/04/16
 * Time: 10:28
 */
public class TestRunner {

    private DiversifyClassLoader applicationClassLoader;
    private DSpotCompiler compiler;

    public TestRunner(DiversifyClassLoader applicationClassLoader, DSpotCompiler compiler) {
        this.applicationClassLoader = applicationClassLoader;
        this.compiler = compiler;
    }

    public boolean writeAndCompile(CtType classInstru) {
        try {
            FileUtils.cleanDirectory(compiler.getBinaryOutputDirectory());
            FileUtils.cleanDirectory(compiler.getSourceOutputDirectory());
        } catch (IOException | IllegalArgumentException ignored) {
            Log.warn("error during cleaning output directories");
            //ignored
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
}
