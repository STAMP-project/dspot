package fr.inria.diversify.compare;

import java.lang.reflect.Method;

/**
 * User: Simon
 * Date: 25/09/15
 * Time: 11:27
 */
public class Invocation {

    private final Object receiver;

    private final Method target;

    private Object result;

    private Throwable error;

    public Invocation(Object receiver, Method target) {
        this.receiver = receiver;
        this.target = target;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }

    public Object getReceiver() {
        return receiver;
    }

    public Method getTarget() {
        return target;
    }

}
