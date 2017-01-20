package fr.inria.diversify;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.AmplificationHelper;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.util.Set;


/**
 * User: Simon
 * Date: 25/11/16
 * Time: 11:16
 */
public class Utils {

    private static String confFile = "src/test/resources/sample.properties";
    private static InputProgram inputProgram;
    private static InputConfiguration inputConfiguration;
    private static Launcher spoonModel;
    private static DSpotCompiler compiler;

    public static DSpotCompiler getCompiler() {
        return compiler;
    }

    public static InputConfiguration getInputConfiguration() throws InvalidSdkException, Exception {
        lazyInit();
        return inputConfiguration;
    }

    private static void init() {
        try {
            inputConfiguration = new InputConfiguration(confFile);
            inputProgram = InitUtils.initInputProgram(inputConfiguration);
            InitUtils.initLogLevel(inputConfiguration);
            inputConfiguration.setInputProgram(inputProgram);
            String outputDirectory = inputConfiguration.getProperty("tmpDir") + "/tmp";
            FileUtils.cleanDirectory(new File("tmpDir"));
            FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(outputDirectory));
            inputProgram.setProgramDir(outputDirectory);
            String dependencies = AmplificationHelper.getDependenciesOf(inputConfiguration, inputProgram);
            File output = new File(inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir());
            FileUtils.cleanDirectory(output);
            DSpotCompiler.compile(inputProgram.getAbsoluteSourceCodeDir(), dependencies, output);
            compiler = new DSpotCompiler(inputProgram, dependencies);
            inputProgram.setFactory(compiler.getLauncher().getFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static InputProgram getInputProgram() throws InvalidSdkException, Exception {
        lazyInit();
        return inputProgram;
    }

    public static void reset() {
        inputProgram = null;
    }

    private static void lazyInit() throws InvalidSdkException, Exception {
        if (inputProgram == null) {
            init();
        }
    }

    public static CtClass findClass(String fullQualifiedName) throws InvalidSdkException, Exception {
        return getInputProgram().getFactory().Class().get(fullQualifiedName);
    }

    public static CtMethod findMethod(CtClass<?> ctClass, String methodName) throws InvalidSdkException, Exception {
        Set<CtMethod<?>> mths = ctClass.getMethods();
        return mths.stream()
                .filter(mth -> mth.getSimpleName().endsWith(methodName))
                .findFirst()
                .orElse(null);
    }

    public static CtMethod findMethod(String className, String methodName) throws InvalidSdkException, Exception {
        Set<CtMethod> mths = findClass(className).getMethods();
        return mths.stream()
                .filter(mth -> mth.getSimpleName().endsWith(methodName))
                .findFirst()
                .orElse(null);
    }

    public static Factory getFactory() throws InvalidSdkException, Exception {
        return getInputProgram().getFactory();
    }

    public static String buildMavenHome() {
        return System.getenv().get("MAVEN_HOME") != null ? System.getenv().get("MAVEN_HOME") :
                System.getenv().get("M2_HOME") != null ? System.getenv().get("M2_HOME") : "/usr/share/maven/";
    }
}
