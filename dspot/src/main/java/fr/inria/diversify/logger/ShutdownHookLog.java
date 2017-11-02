package fr.inria.diversify.logger;

/**
 * User: Simon
 * Date: 22/04/15
 * Time: 11:56
 */
public class ShutdownHookLog  extends Thread {
    public void run() {
        Logger.close();
    }
}
