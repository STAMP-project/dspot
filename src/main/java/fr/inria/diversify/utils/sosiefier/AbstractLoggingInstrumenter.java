package fr.inria.diversify.utils.sosiefier;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtTryImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    protected CtExecutable<?> getMethod(CtElement stmt) {
        CtExecutable<?> ret = stmt.getParent(CtMethod.class);
        if (ret == null)
            ret = stmt.getParent(CtConstructor.class);
        return ret;
    }

  /*  protected int getLocalId(CtElement stmt) {
        CtExecutable parent = stmt.getParent(CtExecutable.class);
        if (localId.containsKey(parent)) {
            localId.put(parent, localId.get(parent) + 1);
        }
        else {
            localId.put(parent, 0);
        }
//        String methodName = parent.getReference().getDeclaringType().getQualifiedName() + "." + parent.getSignature();
        return ProcessorUtil.idFor(stmtWithoutLog(stmt).replaceAll("\n", "").trim() + ":" + localId.get(parent));
    }*/

    protected String stmtWithoutLog(CtElement stmt) {
        Set<String> toRemove = new HashSet<>();
        for(Object snippetStatement : Query.getElements(stmt, new TypeFilter(CtCodeSnippetStatement.class))) {
            if(snippetStatement.toString().contains(getLogger())) {
                toRemove.add(snippetStatement.toString());
            }
        }
        String ret = stmt.toString();
        for(String remove : toRemove) {
            ret = ret.replace(remove+";", "");
        }

        return ret;
    }

    protected int methodId(CtExecutable method) {
        return ProcessorUtil.idFor(method.getReference().getDeclaringType().getQualifiedName() + "." + method.getSignature());
    }

    protected boolean blockNeed(CtStatement statement) {
        return statement.getParent() instanceof CtLoop
                || statement.getParent() instanceof CtIf;
    }

    protected void insertInBlock(CtStatement statement, CtStatement snippetStmt)  {
        CtBlock<Object> block = getFactory().Core().createBlock();
        block.setParent(statement.getParent());
        block.addStatement(snippetStmt);
        block.addStatement(statement);

        if (statement.getParent() instanceof CtLoop) {
            CtLoop loop = (CtLoop) statement.getParent();
            loop.setBody(block);
        }
        if (statement.getParent() instanceof CtIf) {
            CtIf ctIf = (CtIf) statement.getParent();
            ctIf.setThenStatement(block);
        }
    }


    protected CtTry tryFinallyBody(CtExecutable method) {
        if(!tryBodyMethod.containsKey(methodId(method))) {
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
            tryBodyMethod.put(methodId(method), ctTry);
        }
        return tryBodyMethod.get(methodId(method)) ;
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
