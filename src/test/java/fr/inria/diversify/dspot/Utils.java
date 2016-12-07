package fr.inria.diversify.dspot;

import fr.inria.diversify.factories.DiversityCompiler;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.io.IOException;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/7/16
 */
public class Utils {

    public static CtMethod getCtMethod(CtClass testClass, final String simpleNameMethod) {
        return (CtMethod) testClass.getMethods().stream().filter(method -> simpleNameMethod.equals(((CtMethod) method).getSimpleName())).findFirst().get();
    }

    public static Factory getFactory(InputProgram inputProgram) throws IOException, InterruptedException {
        DiversityCompiler diversityCompiler = DSpotUtils.initDiversityCompiler(inputProgram, true);
        Factory factory = diversityCompiler.getFactory();
        factory.getEnvironment().setNoClasspath(true);
        return factory;
    }

    public static InputProgram getInputProgram() {
        InputProgram inputProgram = new InputProgram();
        inputProgram.setProgramDir("src/test/resources/");
        inputProgram.setRelativeSourceCodeDir("src");
        inputProgram.setRelativeTestSourceCodeDir("test");
        inputProgram.setTransformationPerRun(1);
        inputProgram.setJavaVersion(8);
        return inputProgram;
    }
}
