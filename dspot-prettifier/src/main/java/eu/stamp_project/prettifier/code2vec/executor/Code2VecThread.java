package eu.stamp_project.prettifier.code2vec.executor;

import java.io.InputStream;
import java.io.PrintStream;

public class Code2VecThread extends Thread {

    private final PrintStream output;
    private final InputStream input;

    Code2VecThread(PrintStream output, InputStream input) {
        this.output = output;
        this.input = input;
    }

    public synchronized void start() {
        while (true) {
            try {
                int read;
                if ((read = this.input.read()) != -1) {
                    this.output.print((char) read);
                    continue;
                }
            } catch (Exception var6) {
                ;
            } finally {
                this.interrupt();
            }

            return;
        }
    }
}