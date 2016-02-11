package fr.inria.diversify.compare;

import java.util.concurrent.*;

/**
 * User: Simon
 * Date: 25/09/15
 * Time: 11:10
 */
public class Invocator {
    protected int timeout;
    protected static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();

    public Invocator(int timeout) {
        this.timeout = timeout;
    }


    public void invoke(final Invocation invocation) {
        try {
            Object result = timedCall(new Callable<Object>() {
                public Object call() throws Exception {
                    return invocation.target.invoke(invocation.receiver);
                }
            }, timeout, TimeUnit.MILLISECONDS);

            invocation.setResult(result);

        } catch (Throwable e1) {
            invocation.setError(e1);
            return;
        }
    }

    protected <T> T timedCall(Callable<T> c, long timeout, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        FutureTask<T> task = new FutureTask<T>(c);
        THREAD_POOL.execute(task);
        return task.get(timeout, timeUnit);
    }
}
