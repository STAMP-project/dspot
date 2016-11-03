package fr.inria.diversify.exp;

import fr.inria.diversify.buildSystem.DiversifyClassLoader;
import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.dspot.DSpot;
import fr.inria.diversify.mutant.Mutant;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 14/12/15
 * Time: 14:30
 */
public class ExpMutantDSpot {
    protected int nbVersion;
    protected InputConfiguration inputConfiguration;
    protected InputProgram inputProgram;
    protected Mutant mutant;
    protected String mutantClass;

    protected LogResult log;
    protected File resultDir;

    public ExpMutantDSpot(String propertiesFile, int nbVersion) throws Exception, InvalidSdkException {
        this.nbVersion = nbVersion;

        inputConfiguration = new InputConfiguration(propertiesFile);
        InitUtils.initLogLevel(inputConfiguration);

        inputProgram = InitUtils.initInputProgram(inputConfiguration);
        String tmpDir = inputConfiguration.getProperty("tmpDir") + "/tmp_" + System.currentTimeMillis();
        FileUtils.copyDirectory(new File(inputProgram.getProgramDir()), new File(tmpDir));
        inputProgram.setProgramDir(tmpDir);

        String mutantDir = inputConfiguration.getProperty("mutant.dir");
        mutantClass = inputConfiguration.getProperty("mutant.class");
        mutant = new Mutant(inputProgram, mutantDir);
        resultDir = new File(inputConfiguration.getProperty("tmpDir") + "/DSpot_mutant" + System.currentTimeMillis());
        resultDir.mkdirs();

        initLog();
    }

    public void runExp() throws IOException {
        for(int i = 0; i <= nbVersion; i++)
            try {
                String mutantTestProject = mutant.checkout(inputConfiguration.getProperty("tmpDir") + "/mutantTestFT_ "+ System.currentTimeMillis() + "/", i, false, true);
                String mutantApplicationProject = mutant.checkout(inputConfiguration.getProperty("tmpDir") + "/mutantTestFT_ "+ System.currentTimeMillis() + "/", i, true, true);

                initRegressionClassLoader(mutantApplicationProject);

                inputConfiguration.getProperties().setProperty("project", mutantTestProject);
                DSpot dSpot = new DSpot(inputConfiguration, regressionClassLoader);

                List<String> testsNameToExclude = mutant.triggerTests(i);
                List<CtType> testClasses = run(dSpot, testsNameToExclude);
                printClasses(testClasses, resultDir.getAbsolutePath() + "/DSpotTests/" + i + "/" + inputConfiguration.getRelativeTestSourceCodeDir());
                if(verify(i, testClasses)) {
                    List<String> failures = findBug(i, testClasses);
                    LogResult.log(i, mutant.triggerTests(i), failures);
                } else {
                    LogResult.log(i, mutant.triggerTests(i), null);
                }
                clean(dSpot, mutantTestProject, mutantApplicationProject);
            } catch (Throwable e) {
                e.printStackTrace();
                Log.debug("");
            }
        LogResult.close();
        suicide();
    }

    protected void clean(DSpot dSpot, String mutantTestProject, String mutantApplicationProject) throws IOException {
        dSpot.clean();
        FileUtils.forceDelete(new File(mutantTestProject));
        FileUtils.forceDelete(new File(mutantApplicationProject));
    }

    protected boolean verify(int version, List<CtType> testClasses) throws Exception {
        String  mutantApplicationProject = mutant.checkout(inputConfiguration.getProperty("tmpDir") +"/tmp"+ System.currentTimeMillis(), version, false, true);
        printClasses(testClasses, mutantApplicationProject + "/" + inputConfiguration.getRelativeTestSourceCodeDir());

        List<String> failure = runTest(mutantApplicationProject);

        FileUtils.forceDelete(new File(mutantApplicationProject));

        return failure != null && failure.isEmpty();
    }

    protected List<String> findBug(int version, List<CtType> testClasses) throws Exception {
        String  mutantApplicationProject = mutant.checkout(inputConfiguration.getProperty("tmpDir") +"/tmp"+ System.currentTimeMillis(), version, true, true);
        printClasses(testClasses, mutantApplicationProject + "/" + inputConfiguration.getRelativeTestSourceCodeDir());

        List<String> failure = runTest(mutantApplicationProject);

        FileUtils.forceDelete(new File(mutantApplicationProject));

        return failure;
    }

    protected void initLog() throws IOException {
        log = new LogResult(resultDir.getAbsolutePath());
    }

    public static DiversifyClassLoader regressionClassLoader;
    protected void initRegressionClassLoader(String  dir) throws IOException, InterruptedException {
        List<String> classPaths = new ArrayList<>();
        classPaths.add(dir + "/" + inputConfiguration.getClassesDir());
        classPaths.add(dir + "/target/test-classes/");
        regressionClassLoader = new DiversifyClassLoader(Thread.currentThread().getContextClassLoader(), classPaths);

        Set<String> filter = new HashSet<>();
        for(String s : inputConfiguration.getProperty("filter").split(";") ) {
            filter.add(s);
        }
        regressionClassLoader.setClassFilter(filter);

        runTest(dir);
    }

    protected List<String> runTest(String dir) throws InterruptedException, IOException {
        String[] phases = new String[]{"clean", "test"};
        MavenBuilder builder = new MavenBuilder(dir);

        builder.setGoals(phases);
        builder.initTimeOut();
        if(builder.getCompileError()) {
            return null;
        }
        return builder.getFailedTests();
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

    protected List<CtType> run(DSpot dSpot, List<String> testsNameToExclude) {
        return testsNameToExclude.stream()
                .map(failure -> failure.substring(0,failure.lastIndexOf(".")))
                .distinct()
                .map(cl -> {
                    try {
                         return dSpot.generateTest(cl);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(cl -> cl != null)
                .collect(Collectors.toList());
    }

    protected static void suicide() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        Log.debug("suicide");
        Log.debug("PID :"+pid);
        Runtime r = Runtime.getRuntime();
        try {
            r.exec("kill "+pid);
        } catch (Exception e) {
            Log.error("suicide ",e);
        }
    }

    public static void main(String[] args) throws Exception, InvalidSdkException {
        ExpMutantDSpot exp = new ExpMutantDSpot(args[0], Integer.parseInt(args[1]));
        exp.runExp();
    }
}
