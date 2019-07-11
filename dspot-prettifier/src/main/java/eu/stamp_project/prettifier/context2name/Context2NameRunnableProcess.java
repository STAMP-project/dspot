package eu.stamp_project.prettifier.context2name;

import java.io.PrintStream;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Context2NameRunnableProcess implements Runnable {

    private Process process;

    private PrintStream output;

    public Context2NameRunnableProcess(Process process, PrintStream output) {
        this.process = process;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            (new Context2NameThread(this.output, this.process.getInputStream())).start();
            (new Context2NameThread(System.err, this.process.getErrorStream())).start();
            this.process.waitFor();
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }

}
