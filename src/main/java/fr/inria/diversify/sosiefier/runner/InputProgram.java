package fr.inria.diversify.sosiefier.runner;

import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.QueryVisitor;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The InputProgram class encapsulates all the known information of the program being sosiefiecated
 * <p/>
 * Created by marcel on 6/06/14.
 */
@Deprecated
public class InputProgram {

    /**
     * The preferred generator version indicates the preferred version of the generator to modify this
     * program
     */
    private String preferredGeneratorVersion = InputConfiguration.LATEST_GENERATOR_VERSION;


    /**
     * Default tolerance value for the code fragment searching algorithm
     */
    private double searchToleranceThreshold = 0.9999999;


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

    public <T extends CtElement> T findElement(Class type, String position, String searchValue) {
        return findElement(type, position, searchValue, 5, 0.85);
    }

    public <T extends CtElement> T findElement(Class type, String position, String searchValue,
											   int lineThreshold, double valueThreshold) {
        T result = null;

        String[] s = position.split(":");
        String classPositionTmp = s[0];
        if(classPositionTmp.contains("$")) classPositionTmp = classPositionTmp.split("\\$")[0];
        String classPosition = classPositionTmp;
        int lineNumberPosition = Integer.parseInt(s[1]);

        List<T> allElements = getAllElement(type);
        /*List<T> elements = allElements.stream()
                .filter(e -> (e.getPosition().getLine() == lineNumberPosition)
                        && (e.getPosition().getCompilationUnit().getMainType().getQualifiedName().equals(classPosition))
                ).collect(Collectors.toList());*/
        List<T> elements = allElements.stream()
                .filter(e -> (e.getPosition().getLine() == lineNumberPosition))
                .collect(Collectors.toList());
        if(elements.size() == 1) return  elements.get(0);
        List<T> elementsFiltered = elements.stream()
                .filter(e -> e.getPosition().getCompilationUnit().getMainType().getQualifiedName().equals(classPosition))
                .collect(Collectors.toList());
        if(elements.size() == 1) return  elements.get(0);
        for(T elem : elements) {
            if ((searchValue == null || elem.toString().equals(searchValue))) {
                return elem;
            }
        }
        return result;
    }


    /**
     * Root spoon element for an input program, mostly upper level packages
     */
    public synchronized Set<CtPackage> getRoots() {
//        if (roots == null) {
//            roots = new HashSet<>();
//            ProcessingManager pm = new QueueProcessingManager(factory);
//            AbstractProcessor<CtPackage> processor = new AbstractProcessor<CtPackage>() {
//                @Override
//                public void process(CtPackage element) {
//                    CtElement root = element;
//                    while (root.getParent() != null && !root.getParent().toString().equals("")) {
//                        root = root.getParent();
//                    }
//                    roots.add(root);
//                }
//            };
//            pm.addProcessor(processor);
//            pm.process();
//        }
//        return roots;
        return factory.Package().getRootPackage().getPackages();
    }

    public synchronized <T extends CtElement> List<T> getAllElement(Class cl) {
        if (!typeToObject.containsKey(cl)) {
            QueryVisitor<T> query = new QueryVisitor(new TypeFilter(cl));
            List<T> elements = new ArrayList<>();
            for (CtElement e : getRoots()) {
                e.accept(query);
                elements.addAll(query.getResult());
            }
            typeToObject.put(cl, elements);
        }
        return (List<T>)typeToObject.get(cl);
    }

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


    public String getPreferredGeneratorVersion() {
        return preferredGeneratorVersion;
    }

    public void setPreferredGeneratorVersion(String preferredGeneratorVersion) {
        this.preferredGeneratorVersion = preferredGeneratorVersion;
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
