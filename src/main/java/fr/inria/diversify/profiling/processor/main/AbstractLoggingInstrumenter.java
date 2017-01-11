package fr.inria.diversify.profiling.processor.main;

import fr.inria.diversify.runner.InputProgram;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marodrig on 27/06/2014.
 */
public abstract class AbstractLoggingInstrumenter<E extends CtElement> extends AbstractProcessor<E> {

    protected static Map<Integer, CtTry> tryBodyMethod;

    protected InputProgram inputProgram;

    protected String logger;

    public  AbstractLoggingInstrumenter(InputProgram inputProgram) {
        this.inputProgram = inputProgram;
    }

    public String getLogger() {
        return logger;
    }

    public static void reset() {
        tryBodyMethod = new HashMap<>();
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    protected CtClass<?> getClass(CtElement stmt) {
        return stmt.getParent(CtClass.class);
    }

}
