package fr.inria.diversify.testRunner;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Based on: http://stackoverflow.com/questions/6040962/wait-for-cancel-on-futuretask
 *
 * @author Aleksandr Dubinsky
 */
public class FixedFutureTask<T> extends FutureTask<T> {

    /**
     * Creates a {@code FutureTask} that will, upon running, execute the given {@code Runnable},
     * and arrange that {@code get} will return the given result on successful completion.
     *
     * @param runnable the runnable task
     * @param result the result to return on successful completion.
     *               If you don't need a particular result, consider using constructions of the form:
     *               {@code Future<?> f = new FutureTask<Void>(runnable, null)}
     * @throws NullPointerException if the runnable is null
     */
    public
    FixedFutureTask (Runnable runnable, T result) {
        this (Executors.callable (runnable, result));
    }

    /**
     * Creates a {@code FutureTask} that will, upon running, execute the given {@code Callable}.
     *
     * @param  callable the callable task
     * @throws NullPointerException if the callable is null
     */
    public
    FixedFutureTask (Callable<T> callable) {
        this (constructorHelper (callable));
    }

    /** Some ugly code to work around the compiler's limitations on constructors */
    private
    FixedFutureTask (MyCallable<T> myCallable) {
        super (myCallable);
        myCallable.task = this;
    }

    /** Some ugly code to work around the compiler's limitations on constructors */
    private static <T> MyCallable<T>
    constructorHelper (Callable<T> callable) {
        return new MyCallable (callable);
    }

    private final Semaphore semaphore = new Semaphore(1);

    private static class MyCallable<T> implements Callable<T>
    {
        MyCallable (Callable<T> callable) {
            this.callable = callable;
        }

        final Callable<T> callable;
        FixedFutureTask<T> task;

        @Override public T
        call() throws Exception {

            if (task.isCancelled())
                return null;

            task.semaphore.acquire();
            try
            {
                return callable.call();
            }
            finally
            {
                task.semaphore.release();
            }
        }
    }

    /**
     * Waits if necessary for the computation to complete or finish cancelling, and then retrieves its result, if available.
     *
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    @Override public T
    get() throws InterruptedException, ExecutionException, CancellationException {

        try
        {
            return super.get();
        }
        catch (CancellationException e)
        {
            semaphore.acquire();
            semaphore.release();
            throw e;
        }
    }

    /**
     * Waits if necessary for at most the given time for the computation to complete or finish cancelling, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws CancellationException
     * @throws TimeoutException if the wait timed out
     */
    @Override public T
    get (long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, CancellationException, TimeoutException {

        try
        {
            return super.get (timeout, unit);
        }
        catch (CancellationException e)
        {
            semaphore.acquire();
            semaphore.release();
            throw e;
        }
    }

    /**
     * Attempts to cancel execution of this task and waits for the task to complete if it has been started.
     * If the task has not started when {@code cancelWithJoin} is called, this task should never run.
     * If the task has already started, then the {@code mayInterruptIfRunning} parameter determines
     * whether the thread executing this task should be interrupted in an attempt to stop the task.
     *
     * <p>After this method returns, subsequent calls to {@link #isDone} will
     * always return {@code true}.  Subsequent calls to {@link #isCancelled}
     * will always return {@code true} if this method returned {@code true}.
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing this task should be interrupted;
     *                              otherwise, in-progress tasks are allowed to complete
     * @throws InterruptedException if the thread is interrupted
     */
    public void
    cancelAndWait (boolean mayInterruptIfRunning) throws InterruptedException {

        super.cancel (mayInterruptIfRunning);

        semaphore.acquire();
        semaphore.release();
    }
}
