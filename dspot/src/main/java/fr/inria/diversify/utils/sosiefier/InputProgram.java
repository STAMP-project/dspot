package fr.inria.diversify.utils.sosiefier;

import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The InputProgram class encapsulates all the known information of the program being sosiefiecated
 * <p/>
 * Created by marcel on 6/06/14.
 */
//TODO @Deprecated
public class InputProgram {

    /**
     * Path to the root directory of the input program
     */
    private String programDir;

    /**
     * Path to the source code of the input program
     */
    private String sourceCodeDir;

    private String externalSourceCodeDir = "";

    /**
     * Path to the test source code of the input program
     */
    private String testSourceCodeDir;

    /**
     * java version of this program
     */
    private int javaVersion;

    /**
     * Path to the built classes
     */
    private String classesDir;


    /**
     * Path to the test built classes
     */
    private String testClassesDir = "target/test-classes";

    /**
     * Path to the coverage information
     */
    private String coverageDir;

    /**
     * Path to previous transformations made in this input program
     */
    private String previousTransformationsPath;

    /**
     * Number of transformations that we are going to attempt in every run of the diversificator
     */
    private int transformationPerRun;

    /**
     * Minimum number of transformations that we are going to attempt in every run of the diversificator
     */
    private int minTransformationsPerRun;

    /**
     * Root spoon element for an input program, mostly upper level packages
     */
    private Set<CtElement> roots;

    /**
     * List Spoon return statements that can be found in this program
     */
    protected List<CtReturn> returns;


    protected Map<Class, List<? extends CtElement>> typeToObject = new HashMap<Class, List<? extends CtElement>>();

    /**
     * List of inline constants that can be found in this program
     */
    protected List<CtLocalVariable> inlineConstant;


    /**
     * Spoon factory to process all AST elements
     */
    private Factory factory;


    /**
     * Copies properties from the configuration
     *
     * @param configuration
     */
    public void configure(InputConfiguration configuration) {
        setRelativeSourceCodeDir(configuration.getRelativeSourceCodeDir());
        setProgramDir(configuration.getProjectPath());
        setRelativeSourceCodeDir(configuration.getRelativeSourceCodeDir());
        setPreviousTransformationsPath(configuration.getPreviousTransformationPath());
        setClassesDir(configuration.getClassesDir());
        setCoverageDir(configuration.getCoverageDir());
    }

    /**
     * Spoon factory to process all AST elements
     */
    public Factory getFactory() {
        return factory;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
    }

    /**
     * Path to the test source code of the input program
     */
    public String getRelativeTestSourceCodeDir() {
        return testSourceCodeDir;
    }

    /**
     * Path to the test source code of the input program
     */
    public String getAbsoluteTestSourceCodeDir() {
        return  programDir + "/" + testSourceCodeDir;
    }

    public void setRelativeTestSourceCodeDir(String testSourceCodeDir) {
        this.testSourceCodeDir = testSourceCodeDir;
    }

    /**
     * Path to the  source of the input program
     */
    public String getAbsoluteSourceCodeDir() {
        return programDir + "/" + sourceCodeDir;
    }

    /**
     * Path to the  source of the input program
     */
    public String getRelativeSourceCodeDir() {
        return sourceCodeDir;
    }

    public void setRelativeSourceCodeDir(String sourceCodeDir) {
        this.sourceCodeDir = sourceCodeDir;
    }

    public void setExternalSourceCodeDir(String externalSourceCodeDir) {
        this.externalSourceCodeDir = externalSourceCodeDir;
    }

    public String getExternalSourceCodeDir() {
        return externalSourceCodeDir;
    }

    /**
     * Path to the know sosie information stored in file
     */
    public String getPreviousTransformationsPath() {
        return previousTransformationsPath;
    }

    public void setPreviousTransformationsPath(String path) {
        this.previousTransformationsPath = path;
    }

    /**
     * Number of transformations that we are going to attempt in every run of the diversificator
     */
    public int getTransformationPerRun() {
        return transformationPerRun;
    }

    public void setTransformationPerRun(int transformationPerRun) {
        this.transformationPerRun = transformationPerRun;
    }

    /**
     * Path to the root directory of the input program
     */
    public String getProgramDir() {
        return programDir;
    }

    public void setProgramDir(String programDir) {
        this.programDir = programDir;
    }

    public void setJavaVersion(int javaVersion) {
        this.javaVersion = javaVersion;
    }

    public int getJavaVersion() {
        return javaVersion;
    }

    /**
     * Path to the built classes
     */
    public String getClassesDir() {
        return classesDir;
    }


    /**
     * Path to the built test classes
     */
    public String getTestClassesDir() {
        return testClassesDir;
    }

    public void setClassesDir(String classesDir) {
        this.classesDir = classesDir;
    }


    /**
     * Path to the coverage information
     */
    public String getCoverageDir() {
        return coverageDir;
    }

    public void setCoverageDir(String coverageDir) {
        this.coverageDir = coverageDir;
    }

    /**
     * Minimum number of transformations that we are going to attempt in every run of the diversificator
     */
    public int getMinTransformationsPerRun() {
        return minTransformationsPerRun;
    }

    public void setMinTransformationsPerRun(int minTransformationsPerRun) {
        this.minTransformationsPerRun = minTransformationsPerRun;
    }

    public InputProgram clone() {
        InputProgram clone = new InputProgram();
        clone.programDir = programDir;
        clone.sourceCodeDir = sourceCodeDir;
        clone.externalSourceCodeDir = externalSourceCodeDir;
        clone.testSourceCodeDir = testSourceCodeDir;
        clone.javaVersion = javaVersion;
        clone.coverageDir = coverageDir;
        clone.classesDir = classesDir;

        return clone;
    }
}
