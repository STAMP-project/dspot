package fr.inria.diversify.utils.sosiefier;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.factory.Factory;
import spoon.support.reflect.code.CtTryImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marodrig on 27/06/2014.
 */
@Deprecated
public abstract class AbstractLoggingInstrumenter<E extends CtElement> extends AbstractProcessor<E> {
    protected static Map<CtExecutable, Integer> localId = new HashMap();

    protected static Map<Integer, CtTry> tryBodyMethod;

    protected InputProgram inputProgram;

    protected String logger;

    public AbstractLoggingInstrumenter(InputProgram inputProgram) {
        this.inputProgram = inputProgram;
        tryBodyMethod = new HashMap<>();
    }

    public String getLogger() {
        return logger;
    }

    public static void reset() {
        tryBodyMethod.clear();
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    protected CtTry tryFinallyBody(CtExecutable method) {
        if(!tryBodyMethod.containsKey(ProcessorUtil.methodId(method))) {
            Factory factory = method.getFactory();
            CtStatement thisStatement = getThisOrSuperCall(method.getBody());

            CtTry ctTry = factory.Core().createTry();
            ((CtTryImpl)ctTry).setBody(method.getBody());

            CtBlock finalizerBlock = factory.Core().createBlock();
            ctTry.setFinalizer(finalizerBlock);

            CtBlock methodBlock = factory.Core().createBlock();
            methodBlock.addStatement(ctTry);
            method.setBody(methodBlock);

            if (thisStatement != null) {
                ctTry.getBody().removeStatement(thisStatement);
                method.getBody().getStatements().add(0, thisStatement);
            }
            tryBodyMethod.put(ProcessorUtil.methodId(method), ctTry);
        }
        return tryBodyMethod.get(ProcessorUtil.methodId(method)) ;
    }

    protected CtStatement getThisOrSuperCall(CtBlock block) {
        if(!block.getStatements().isEmpty()) {
            CtStatement stmt = block.getStatement(0);
            if(stmt.toString().startsWith("this(") || stmt.toString().startsWith("super(")) {
                return stmt;
            }
        }
        return null;
    }
}
