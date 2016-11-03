package fr.inria.diversify.exp.tool;

import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 14/12/15
 * Time: 10:34
 */
public class Defect4J {
    protected String frameworkBaseDir;
    protected String tmpDir;


    public Defect4J(String frameworkBaseDir, String tmpDir) {
        this.frameworkBaseDir = frameworkBaseDir;
        this.tmpDir = tmpDir;
    }

    public String checkout(String project, int id, boolean bugVersion) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(frameworkBaseDir + "/bin/defects4j");
        command.add("checkout");
        command.add("-p");
        command.add(project);
        command.add("-v");
        if(bugVersion) {
            command.add(id + "b");
        } else {
            command.add(id + "f");
        }
        command.add("-w");

        File dir = new File(tmpDir + "/" + project + (bugVersion ? "b" : "f"));
        if(dir.exists()) {
            FileUtils.forceDelete(dir);
        }
        dir.mkdirs();

        command.add(dir.getAbsolutePath());

        run(command);
        return dir.getAbsolutePath();
    }

    public boolean runTest(String workingDir, String archive) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();

        command.add(frameworkBaseDir + "/bin/defects4j");
        command.add("test");
        command.add("-w");
        command.add(workingDir + "/");
        command.add("-s");
        command.add(archive);

        List<String> result = run(command);

        boolean runTest = result.stream()
                .anyMatch(line -> line.contains("run.gen.tests") && line.endsWith("OK"));
        if(runTest) {
            return !result.stream()
                    .anyMatch(line -> line.contains("Failing tests"));
        }
        return false;
    }

    public String buildArchive(String projectId, int versionId, String toolName, String testsDir, String outputDir) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        File outputDirFile = new File(outputDir + "/" + projectId + "/" + toolName + "/" + versionId + "/");
        outputDirFile.mkdirs();

        String archiveName = outputDirFile.getAbsolutePath() + "/"
            + projectId + "-" + versionId + "f-" + toolName + "." + versionId + ".tar.bz2";

        command.add("tar");
        command.add("-jcvf");
        command.add(archiveName);
        command.add(".");

        run(command, new File(testsDir));

        return archiveName;
    }

    public Set<String> triggerTests(String project, int id) throws IOException {
        Path path = FileSystems.getDefault().getPath(frameworkBaseDir, "projects", project, "trigger_tests", id+"");
        return Files.readAllLines(path).stream()
                .filter(line -> line.startsWith("--- "))
                .map(line -> line.substring(4, line.length()))
                .collect(Collectors.toSet());
    }

    public String runEvosuite(String projectId, int versionId, boolean bugVersion) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();

        command.add(frameworkBaseDir + "/bin/run_evosuite.pl");
        command.add("-p");
        command.add(projectId);
        command.add("-v");

        String fullVersionId;
        if(bugVersion) {
            fullVersionId = versionId + "b";
        } else {
            fullVersionId = versionId + "f";
        }
        command.add(fullVersionId);

        command.add("-n");
        command.add(versionId + "");
        command.add("-o");
        command.add(tmpDir);
        command.add("-cbranch");
        command.add("-A");
        command.add("-b");
        command.add("30");
        command.add("-a");
        command.add("30");

        run(command);
        return tmpDir + "/" + projectId + "/evosuite-branch/" + versionId + "/"
                + projectId + "-" + fullVersionId + "-evosuite-branch." + versionId + ".tar.bz2";
    }

    protected String runRandoop(String projectId, int versionId, boolean bugVersion) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();

        command.add(frameworkBaseDir + "/bin/run_evosuite.pl");
        command.add("-p");
        command.add(projectId);
        command.add("-v");

        String fullVersionId;
        if(bugVersion) {
            fullVersionId = versionId + "b";
        } else {
            fullVersionId = versionId + "f";
        }
        command.add(fullVersionId);

        command.add("-n");
        command.add(versionId + "");
        command.add("-o");
        command.add(tmpDir);
        command.add("-cbranch");
        command.add("-A");
        command.add("-b");
        command.add("100");
        command.add("-a");
        command.add("300");

        run(command);
        return tmpDir + "/" + projectId + "/evosuite-branch/" + versionId + "/"
                + projectId + "-" + fullVersionId + "-evosuite-branch." + versionId + ".tar.bz2";
    }

    public boolean bugDetection(String projectId, int versionId, String toolName) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();

        command.add(frameworkBaseDir + "/bin/run_bug_detection.pl");
        command.add("-p");
        command.add(projectId);
        command.add("-d");
        command.add(tmpDir + "/" + projectId + "/" + toolName + "/" + versionId + "/");
        command.add("-o");
        command.add(tmpDir);

        run(command);

        File triggerTestsF = new File(tmpDir + "/bug_detection_log/" + projectId + "/" + toolName + "/" + versionId + "f." + versionId + ".trigger.log");
        File triggerTestsB = new File(tmpDir + "/bug_detection_log/" + projectId + "/" + toolName + "/" + versionId + "b." + versionId + ".trigger.log");
//        return FileUtils.contentEquals(triggerTestsF, triggerTestsB);
        return true;
    }


    public void fixTestSuite(String projectId, int versionId, String toolName) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();

        command.add(frameworkBaseDir + "/util/fix_test_suite.pl");
        command.add("-p");
        command.add(projectId);
        command.add("-d");
        command.add(tmpDir + "/" + projectId + "/" + toolName + "/" + versionId + "/");

        run(command);
    }

    protected List<String> run(List<String> command) throws IOException, InterruptedException {

        return run(command, null);
    }

    protected List<String> run(List<String> command, File workingDir) throws IOException, InterruptedException {
        List<String> result = new ArrayList<>();

        ProcessBuilder builder = new ProcessBuilder(command);

        if(workingDir != null) {
            builder.directory(workingDir);
        }
        if(new File("/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home").exists()) {
            //osx
            builder.environment().put("JAVA_HOME", "/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home");
        } else {
            //coreff3
            builder.environment().put("JAVA_HOME", "/usr/lib/jvm/java-7-oracle");
            String path = builder.environment().get("PATH");
            builder.environment().put("PATH", "/usr/lib/jvm/java-7-oracle/bin:"+path);
        }
        Process process = builder.start();


        InputStreamReader isr = new InputStreamReader(process.getErrorStream());
        BufferedReader br = new BufferedReader(isr);

        String line;
        while ((line = br.readLine()) != null) {
            result.add(line);
            Log.info(line);
        }
        process.waitFor();

        return result;
    }
}
