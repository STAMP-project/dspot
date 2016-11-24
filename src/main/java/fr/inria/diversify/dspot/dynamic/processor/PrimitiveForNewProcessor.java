package fr.inria.diversify.dspot.dynamic.processor;

import fr.inria.diversify.processor.ProcessorUtil;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.utils.CtTypeUtils;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

/**
 * User: Simon
 * Date: 26/08/16
 * Time: 10:49
 */
public class PrimitiveForNewProcessor extends AbstractLoggingInstrumenter<CtConstructorCall> {

    public PrimitiveForNewProcessor(InputProgram inputProgram) {
        super(inputProgram);
    }

    public boolean isToBeProcessed(CtConstructorCall constructor) {
        List<CtTypeReference<?>> params = constructor.getExecutable().getParameters();
        return  getMethod(constructor) != null
                && params.stream()
                .anyMatch(param -> CtTypeUtils.isPrimitive(param) || CtTypeUtils.isString(param));
    }

    @Override
    public void process(CtConstructorCall constructorCall) {
        CtExecutableReference constructor = constructorCall.getExecutable();
        int mthId = ProcessorUtil.methodId(getMethod(constructorCall));
        int constructorCallId = ProcessorUtil.idFor(constructor.toString());

        List<CtTypeReference<?>> params = constructor.getParameters();
        for(int i = 0; i < params.size(); i++) {
            CtTypeReference<?> param = params.get(i);
            if(CtTypeUtils.isPrimitive(param) || CtTypeUtils.isString(param)) {
                CtExpression arg = (CtExpression) constructorCall.getArguments().get(i);
                int argId = ProcessorUtil.idFor(arg.toString());

                CtLocalVariable localVar = getFactory().Code().createLocalVariable(param, "var_" + argId, arg);
                arg.replace(getFactory().Code().createLocalVariableReference(localVar));

                String snippet = getLogger() + ".logPrimitive(Thread.currentThread(), " + mthId
                        + ", " + constructorCallId + ", " + i + ","
                        + arg.toString() + ")";

                CtStatement snippetStatement = getFactory().Code().createCodeSnippetStatement(snippet);

                try {
                    CtStatement parent = constructorCall.getParent(CtStatement.class);
                    if(parent.toString().startsWith("super") || parent.toString().startsWith("this(")) {
                        parent.insertAfter(snippetStatement);
                    } else {
                        parent.insertBefore(snippetStatement);
                    }
                } catch (Exception e) {}
            }
        }
    }
}