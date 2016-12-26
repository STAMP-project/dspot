package fr.inria.diversify;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.dspot.DSpotUtils;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.InitUtils;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.Set;


/**
 * User: Simon
 * Date: 25/11/16
 * Time: 11:16
 */
public class Utils {
    private static String confFile = "src/test/resources/sample.properties";
    private static InputProgram inputProgram;
    private static DSpotCompiler compiler;
    private static DiversifyClassLoader applicationClassLoader;

    public static DiversifyClassLoader getApplicationClassLoader() {
        return applicationClassLoader;
    }

    public static DSpotCompiler getCompiler() throws InvalidSdkException, Exception {
        lazyInit();
        return compiler;
    }

    public static InputProgram getInputProgram() throws InvalidSdkException, Exception {
        lazyInit();
        return inputProgram;
    }

    public static void reset() throws InvalidSdkException, Exception {
        applicationClassLoader = null;
        inputProgram = null;
        compiler = null;
    }

    private static void lazyInit() throws InvalidSdkException, Exception {
        if(inputProgram == null) {
            System.out.println("init");
            loadSampleProject();
        }
    }

    private static void loadSampleProject() throws Exception, InvalidSdkException {
        InputConfiguration inputConfiguration = new InputConfiguration(confFile);

        inputProgram = InitUtils.initInputProgram(inputConfiguration);
        InitUtils.initDependency(inputConfiguration);

        compiler = DSpotUtils.initDiversityCompiler(inputProgram, true);

        InitUtils.addApplicationClassesToClassPath(inputProgram);
        applicationClassLoader = DSpotUtils.initClassLoader(inputProgram, inputConfiguration);
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
