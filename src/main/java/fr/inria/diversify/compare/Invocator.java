package fr.inria.diversify.compare;

import java.util.concurrent.*;

/**
 * User: Simon
 * Date: 25/09/15
 * Time: 11:10
 */
public class Invocator {

    private int timeout;
    private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();

    public Invocator(int timeout) {
        this.timeout = timeout;
    }

    public void invoke(final Invocation invocation) {
        try {
            Object result = timedCall(new Callable<Object>() {
                public Object call() throws Exception {
                    return invocation.getTarget().invoke(invocation.getReceiver());
                }
            }, timeout, TimeUnit.MILLISECONDS);
            invocation.setResult(result);
        } catch (Throwable error) {
            invocation.setError(error);
        }
    }

    private <T> T timedCall(Callable<T> c, long timeout, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<T> task = new FutureTask<>(c);
        try {
            THREAD_POOL.execute(task);
            return task.get(timeout, timeUnit);
        } finally {
            task.cancel(true);
        }
    }
}
