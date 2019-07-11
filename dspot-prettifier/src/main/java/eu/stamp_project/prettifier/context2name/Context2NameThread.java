package eu.stamp_project.prettifier.context2name;

import java.io.InputStream;
import java.io.PrintStream;

public class Context2NameThread extends Thread {

    private final PrintStream output;
    private final InputStream input;

    Context2NameThread(PrintStream output, InputStream input) {
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