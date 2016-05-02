package fr.inria.diversify.info.logger;

import fr.inria.diversify.profiling.logger.Logger;

/**
 * User: Simon
 * Date: 22/04/15
 * Time: 11:56
 */
public class ShutdownHookLog extends Thread {
    public void run() {
        Logger.close();
    }
}
