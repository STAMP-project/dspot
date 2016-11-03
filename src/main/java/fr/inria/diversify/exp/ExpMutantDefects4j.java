package fr.inria.diversify.exp;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.exp.tool.Defect4J;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtMethod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 14/12/15
 * Time: 14:30
 */
public class ExpMutantDefects4j {
    protected String projectId;
    protected int nbVersion;
    protected InputConfiguration inputConfiguration;

    protected Defect4J defect4J;
    protected LogResult log;
    protected File resultDir;

    public ExpMutantDefects4j(String projectId, int nbVersion, String defect4JHome) throws Exception, InvalidSdkException {
        this.projectId = projectId;
        this.nbVersion = nbVersion;

        inputConfiguration = new InputConfiguration();
        InitUtils.initLogLevel(inputConfiguration);

        resultDir = new File(inputConfiguration.getProperty("tmpDir") + "/DSpot_" + System.currentTimeMillis());
        resultDir.mkdirs();

        defect4J = new Defect4J(defect4JHome + "/framework", resultDir.getAbsolutePath());
        initLog();
    }

    public void runExp() throws IOException {
        for(int i = 2; i <= nbVersion; i++) {
            try {
                initRegressionClassLoader(i);
                String dir = checkout(i, false);

                DSpot sbse = new DSpot(inputConfiguration, regressionClassLoader);

                Set<String> testsNameToExclude = defect4J.triggerTests(projectId, i);
                List<CtType> classes = run(sbse, testsNameToExclude);
                printClasses(classes, resultDir.getAbsolutePath() + "/testSource/" + projectId + "/" + i + "/" + inputConfiguration.getRelativeTestSourceCodeDir());

                printClasses(classes, dir + "/" + inputConfiguration.getRelativeTestSourceCodeDir());
                String archive = defect4J.buildArchive(projectId, i, "DSpot",
                        resultDir.getAbsolutePath() + "/testSource/" + projectId + "/" + i + "/" + inputConfiguration.getRelativeTestSourceCodeDir(),
                        resultDir.getAbsolutePath());

                boolean status = defect4J.bugDetection(projectId, i, "DSpot");
                Log.info(projectId + " " + i + ": test status on bug version : " + status);
                dir = defect4J.checkout(projectId, i, false);
                boolean status2 = defect4J.runTest(dir, archive);

                 if (status) {
                     dir = defect4J.checkout(projectId, i, true);
                     status = defect4J.runTest(dir, archive);
                     Log.info("");
//                     LogResult.log(i, testsNameToExclude, failures);
                } else {
//                    log.write("Lang_" + i + ": failing tests on correct version (" +dir +")\n");
                }
            } catch (Throwable e)  {
                e.printStackTrace();
                Log.debug("");
            }
        }
        log.close();

    }

    protected void initLog() throws IOException {
        FileWriter fw = new FileWriter(resultDir + "/resultLog");
        log = new LogResult(resultDir.getAbsolutePath());
    }

    public static DiversifyClassLoader regressionClassLoader;
    protected void initRegressionClassLoader(int i) throws IOException, InterruptedException {
        String dir = checkout(i, true);

        List<String> classPaths = new ArrayList<>();
        classPaths.add(dir + "/" + inputConfiguration.getClassesDir());
        classPaths.add(dir + "/target/test-classes/");
        regressionClassLoader = new DiversifyClassLoader(Thread.currentThread().getContextClassLoader(), classPaths);

        Set<String> filter = new HashSet<>();
        for(String s : inputConfiguration.getProperty("filter").split(";") ) {
            filter.add(s);
        }
        regressionClassLoader.setClassFilter(filter);

        compile(dir);
    }

    protected void compile(String dir) throws InterruptedException, IOException {
        String[] phases = new String[]{"clean", "test"};
        MavenBuilder builder = new MavenBuilder(dir);

        builder.setGoals(phases);
        builder.initTimeOut();
    }

    protected void printClasses(List<CtType> classes, String dir) {
        File dirFile = new File(dir);
        if(!dirFile.exists()) {
            dirFile.mkdirs();
        }
        for(CtType cl : classes) {
            try {
                PrintClassUtils.printJavaFile(new File(dir), cl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected List<CtType> run(DSpot dSpot, Set<String> testsNameToExclude) {
        Map<CtType, List<String[]>> testByClass = testsNameToExclude.stream()
                .map(testName -> testName.split("::"))
                .collect(Collectors.groupingBy(split -> findClass(split[0],  dSpot.getInputProgram())));

        return testByClass.keySet().stream()
                .map(cl -> {
                    Set<CtMethod> testsToExclude = testByClass.get(cl).stream()
                            .map(split -> split[split.length - 1])
                            .map(simpleName -> findMethod(cl, simpleName))
                            .filter(mth -> mth != null)
                            .collect(Collectors.toSet());

                    List<CtMethod> methods = new LinkedList<CtMethod>(cl.getMethods());
                    methods.removeAll(testsToExclude);
                    removeBody(testsToExclude);
                    try {
                        return dSpot.generateTest(methods, cl);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(cl -> cl != null)
                .collect(Collectors.toList());
    }

    protected void removeBody(Set<CtMethod> methods) {
        methods.stream()
                .forEach(mth -> mth.setBody(mth.getFactory().Core().createBlock()));
    }


    protected CtType findClass(String className, InputProgram inputProgram) {
        List<CtType> classes = inputProgram.getAllElement(CtType.class);

        return classes.stream()
                .filter(cl -> cl.getQualifiedName().equals(className))
                .findFirst()
                .get();
    }

    protected CtMethod findMethod(CtType cl, String mthSimpleName) {
        Set<CtMethod> mths = cl.getMethods();
        return mths.stream()
                .filter(mth -> mth.getSimpleName().contains(mthSimpleName))
                .findFirst()
                .orElse(null);
    }

    protected void killAllChildrenProcess() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        Runtime r = Runtime.getRuntime();
        try {
            r.exec("pkill -P " + pid);

            Thread.sleep(1000);
        } catch (Exception e) {
            Log.error("killallchildren ", e);
        }
        Log.debug("all children process kill (pid: {})", pid);
    }

    protected String checkout(int version, boolean bugVersion) throws IOException, InterruptedException {
        String dir = defect4J.checkout(projectId, version, bugVersion);
        inputConfiguration.getProperties().setProperty("project", dir);

        fixProject(version);

        return dir;
    }

    protected void fixProject(int version) {
        switch (projectId) {
            case "Lang":
                fixLang(version);
                break;

            case "Time":
                inputConfiguration.getProperties().setProperty("filter", "org.joda.time");
                break;

            case "Chart":
                break;

            case "Math":
                inputConfiguration.getProperties().setProperty("filter", "org.apache.commons.math");
                break;
        }
    }



    protected void fixLang(int version) {
        inputConfiguration.getProperties().setProperty("filter", "org.apache.commons.lang");
        if (version < 35) {
            inputConfiguration.getProperties().setProperty("src", "src/main/java");
            inputConfiguration.getProperties().setProperty("testSrc", "src/test/java");
        } else {
            inputConfiguration.getProperties().setProperty("src", "src/java");
            inputConfiguration.getProperties().setProperty("testSrc", "src/test");
        }

    }

    public static void main(String[] args) throws Exception, InvalidSdkException {
        ExpMutantDefects4j exp = new ExpMutantDefects4j(args[0], Integer.parseInt(args[1]), args[2]);
        exp.runExp();
    }


}
