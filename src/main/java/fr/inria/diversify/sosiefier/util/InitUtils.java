package fr.inria.diversify.sosiefier.util;

import fr.inria.diversify.sosiefier.runner.InputConfiguration;
import fr.inria.diversify.sosiefier.runner.InputProgram;
import org.apache.log4j.Level;
import spoon.Launcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 18/03/15
 * Time: 10:58
 */
@Deprecated
public class InitUtils {


    public static void initLogLevel(InputConfiguration inputConfiguration) {
        Launcher.LOGGER.setLevel(Level.OFF);
        int level = Integer.parseInt(inputConfiguration.getProperty("logLevel"));
        Log.set(level);
    }

    /**
     * Initializes the InputProgram dataset
     */
    public static InputProgram initInputProgram(InputConfiguration inputConfiguration) throws IOException, InterruptedException {
        InputProgram inputProgram = new InputProgram();
        inputConfiguration.setInputProgram(inputProgram);
        inputProgram.setProgramDir(inputConfiguration.getProperty("project"));
        inputProgram.setRelativeSourceCodeDir(inputConfiguration.getRelativeSourceCodeDir());
        inputProgram.setRelativeTestSourceCodeDir(inputConfiguration.getRelativeTestSourceCodeDir());

        if(inputConfiguration.getProperty("externalSrc") != null) {
            List<String> list = Arrays.asList(inputConfiguration.getProperty("externalSrc").split(System.getProperty("path.separator")));
            String sourcesDir = list.stream()
                    .map(src -> inputProgram.getProgramDir() + "/" + src)
                    .collect(Collectors.joining(System.getProperty("path.separator")));
            inputProgram.setExternalSourceCodeDir(sourcesDir);
        }

        inputProgram.setTransformationPerRun(
                Integer.parseInt(inputConfiguration.getProperty("transformation.size", "1")));

        //Path to pervious transformations made to this input program
        inputProgram.setPreviousTransformationsPath(
                inputConfiguration.getProperty("transformation.directory"));

        inputProgram.setClassesDir(inputConfiguration.getProperty("classes"));

        inputProgram.setCoverageDir(inputConfiguration.getProperty("jacoco"));

        inputProgram.setJavaVersion(Integer.parseInt(inputConfiguration.getProperty("javaVersion", "6")));

        return inputProgram;
    }
}
