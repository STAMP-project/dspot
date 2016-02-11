package fr.inria.diversify.exp;

import fr.inria.diversify.exp.tool.Defect4J;
import fr.inria.diversify.util.Log;

import java.io.IOException;

/**
 * User: Simon
 * Date: 05/01/16
 * Time: 16:22
 */
public class EvosuiteExp {
    protected String projectId;
    protected int nbVersion;
    protected Defect4J defect4J;
    protected String tmpDir;

    EvosuiteExp(String projectId, int nbVersion, String defect4JHome) throws Exception {
        this.projectId = projectId;
        this.nbVersion = nbVersion;
        tmpDir = "tmpDir/evosuite_"+ System.currentTimeMillis();

        defect4J = new Defect4J(defect4JHome +"/framework", tmpDir);

    }

    private void run() throws IOException, InterruptedException {
        for(int i = 1; i <= nbVersion; i++) {
            try {
                String archive = defect4J.runEvosuite(projectId, i, false);
                defect4J.fixTestSuite(projectId, i, "evosuite-branch");

                boolean status = defect4J.bugDetection(projectId, i, "evosuite-branch");
                Log.info("Lang_" + i + ": test status on bug version : " + status);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        (new EvosuiteExp(args[0], Integer.parseInt(args[1]), args[2])).run();

    }
}
