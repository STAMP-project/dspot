package eu.stamp_project.prettifier.code2vec;

import java.io.PrintStream;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Code2VecRunnableProcess implements Runnable {

    private Process process;

    private PrintStream output;

    public Code2VecRunnableProcess(Process process, PrintStream output) {
        this.process = process;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            (new Code2VecThread(this.output, this.process.getInputStream())).start();
            (new Code2VecThread(System.err, this.process.getErrorStream())).start();
            this.process.waitFor();
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }

}
