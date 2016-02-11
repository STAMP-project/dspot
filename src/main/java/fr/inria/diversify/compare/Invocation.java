package fr.inria.diversify.compare;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

/**
 * User: Simon
 * Date: 25/09/15
 * Time: 11:27
 */
public class Invocation {
    protected final Object receiver;
    protected final Method target;
    protected Object result;
    protected Throwable error;

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


    public boolean sameStatus(Invocation other) {
        if(error != null && other.error != null) {
            return error.getClass().equals(other.getError().getClass());
        } else {
            return (error != null) == (other.error != null);
        }
    }

    public boolean hasTimeOutError() {
        return error != null && error instanceof TimeoutException;
    }

    public String toString() {
        return receiver + "." + target;
    }
}
