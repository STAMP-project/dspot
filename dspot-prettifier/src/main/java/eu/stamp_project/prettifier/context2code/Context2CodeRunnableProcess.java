package eu.stamp_project.prettifier.context2code;

import java.io.PrintStream;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Context2CodeRunnableProcess implements Runnable {

    private Process process;

    private PrintStream output;

    public Context2CodeRunnableProcess(Process process, PrintStream output) {
        this.process = process;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            (new Context2CodeThread(this.output, this.process.getInputStream())).start();
            (new Context2CodeThread(System.err, this.process.getErrorStream())).start();
            this.process.waitFor();
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }

}
