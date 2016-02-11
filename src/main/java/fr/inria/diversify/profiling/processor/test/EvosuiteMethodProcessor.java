package fr.inria.diversify.profiling.processor.test;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtMethod;

/**
 * User: Simon
 * Date: 01/06/15
 * Time: 11:10
 */
public class EvosuiteMethodProcessor extends AbstractProcessor<CtMethod> {

    public boolean isToBeProcessed(CtMethod element) {
        String methodName = element.getSimpleName();
        return methodName.equals("setSystemProperties")
                || methodName.equals("initEvoSuiteFramework")
                || methodName.equals("clearEvoSuiteFramework")
                || methodName.equals("initTestCase")
                || methodName.equals("doneWithTestCase");
    }

    @Override
    public void process(CtMethod element) {
        element.setBody(getFactory().Core().createBlock());
    }
}
