package fr.inria.diversify.exp.tool;

import fr.inria.diversify.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Simon
 * Date: 21/01/16
 * Time: 10:37
 */
public class EvoSuite {
    protected String path;
    protected String workingDir;

    public EvoSuite(String path, String workingDir) {
        this.path = path;
        this.workingDir = workingDir;
        File file = new File(workingDir);
        file.mkdirs();
    }

    public String run(String projectCP, String classTarget, String criterion, int searchBudget, int assertionTimeOut) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(path);

        command.add("-projectCP");
        command.add(projectCP);

        command.add("-class");
        command.add(classTarget);

//        command.add("-criterion");
//        command.add(criterion);

        command.add("-Dsearch_budget=" + searchBudget);

        command.add("-Dassertion_timeout="+ assertionTimeOut);

        command.add("-Dtest_dir=evoSuite");

        run(command, new File(workingDir));

        return workingDir + "/evosuite/";
    }

    public String run(String projectCP, String classTarget) throws IOException, InterruptedException {
        return run(projectCP, classTarget, "BRANCH", 30, 30);
    }

    protected List<String> run(List<String> command, File workingDir) throws IOException, InterruptedException {
        List<String> result = new ArrayList<>();

        ProcessBuilder builder = new ProcessBuilder(command);

        if(workingDir != null) {
            builder.directory(workingDir);
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
