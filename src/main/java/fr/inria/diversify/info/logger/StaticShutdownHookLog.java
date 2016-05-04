package fr.inria.diversify.info.logger;

/**
 * User: Simon
 * Date: 02/05/16
 * Time: 11:35
 */
public class StaticShutdownHookLog extends Thread {
    public void run() {
        StaticLogWriter.close();
    }
}
