package fr.inria.diversify.profiling.processor.test;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;

import java.util.ArrayList;


/**
 * User: Simon
 * Date: 03/06/15
 * Time: 11:16
 */
public class EvosuiteClassProcessor extends AbstractProcessor<CtClass> {

    public boolean isToBeProcessed(CtClass element) {
        String className = element.getSimpleName();
        return className.endsWith("ESTest");
    }

    @Override
    public void process(CtClass cl) {
        cl.setAnnotations(new ArrayList<>());
    }
}
