package fr.inria.diversify.exp;

import fr.inria.diversify.buildSystem.android.InvalidSdkException;
import fr.inria.diversify.buildSystem.maven.MavenBuilder;
import fr.inria.diversify.exp.tool.EvoSuite;
import fr.inria.diversify.mutant.Mutant;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.InitUtils;
import fr.inria.diversify.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * User: Simon
 * Date: 14/12/15
 * Time: 14:30
 */
public class ExpMutantEvoSuite {
    protected int nbVersion;
    protected InputConfiguration inputConfiguration;
    protected InputProgram inputProgram;
    protected Mutant mutant;
    protected String mutantClass;

    BufferedWriter log;
    File resultDir;

    public ExpMutantEvoSuite(String propertiesFile, int nbVersion) throws Exception, InvalidSdkException {
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
        resultDir = new File(inputConfiguration.getProperty("tmpDir") + "/EvoSuite_mutant" + System.currentTimeMillis());
        resultDir.mkdirs();

        initLog(inputConfiguration);
    }

    public void runExp() throws IOException {
        try {
            EvoSuite evosuite = new EvoSuite("/Users/Simon/Documents/code/defects4j/framework/lib/test_generation/generation/evosuite-0.2.0.jar", resultDir.getAbsolutePath() + "/" + System.currentTimeMillis());
            String evoSuiteTestDir = evosuite.run(new File(inputProgram.getProgramDir()).getAbsolutePath() + "/" + inputProgram.getClassesDir(), mutantClass);

            for(int i = 0; i <= nbVersion; i++) {
                log.flush();
                String mutantTestProject = mutant.checkout(inputConfiguration.getProperty("tmpDir") + "/mutantTestFT/", i, false, true);
                inputConfiguration.getProperties().setProperty("project", mutantTestProject);
                runTest(mutantTestProject);


                copyDir(evoSuiteTestDir, resultDir.getAbsolutePath() + "/evoSuite/" + i + "/" + inputConfiguration.getRelativeTestSourceCodeDir());
                if (verify(i, evoSuiteTestDir)) {
                    List<String> failures = findBug(i, evoSuiteTestDir);
                    if (!failures.isEmpty()) {
                        log.write("mutant " + i + ": " + failures.size() + " test fail\n");
                        for (String failure : failures) {
                            log.write("\t" + failure + "\n");
                        }
                    } else {
                        log.write("mutant " + i + ": all tests green\n");
                    }
                } else {
                    log.write(i + ": failing tests on correct version\n");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.debug("");
        }
        log.close();
        suicide();
    }

    protected boolean verify(int version, String evoSuiteTestDir) throws Exception {
        String  mutantApplicationProject = mutant.checkout(inputConfiguration.getProperty("tmpDir") +"/tmp"+ System.currentTimeMillis(), version, false, true);
        copyDir(evoSuiteTestDir, mutantApplicationProject + "/" + inputConfiguration.getRelativeTestSourceCodeDir());

        FileUtils.copyFile(new File(inputConfiguration.getProperty("mutant.pom")), new File(mutantApplicationProject + "/pom.xml"));
        List<String> failure = runTest(mutantApplicationProject);

        FileUtils.forceDelete(new File(mutantApplicationProject));

        return failure != null && failure.isEmpty();
    }

    protected List<String> findBug(int version, String evoSuiteTestDir) throws Exception {
        String  mutantApplicationProject = mutant.checkout(inputConfiguration.getProperty("tmpDir") +"/tmp"+ System.currentTimeMillis(), version, true, true);
        copyDir(evoSuiteTestDir, mutantApplicationProject + "/" + inputConfiguration.getRelativeTestSourceCodeDir());

        FileUtils.copyFile(new File(inputConfiguration.getProperty("mutant.pom")), new File(mutantApplicationProject + "/pom.xml"));
        List<String> failure = runTest(mutantApplicationProject);

        FileUtils.forceDelete(new File(mutantApplicationProject));

        return failure;
    }

    protected void copyDir(String source, String target) throws IOException {
        FileUtils.copyDirectory(new File(source), new File(target));
    }

    protected void initLog(InputConfiguration inputConfiguration) throws IOException {
        FileWriter fw = new FileWriter(resultDir + "/resultLog");
        log = new BufferedWriter(fw);
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
        ExpMutantEvoSuite exp = new ExpMutantEvoSuite(args[0], Integer.parseInt(args[1]));
        exp.runExp();
    }
}
