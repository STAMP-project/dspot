package fr.inria.diversify.dspot.dynamic.invocation;

import fr.inria.diversify.dspot.dynamic.objectInstanciationTree.ObjectInstantiationBuilder;
import spoon.reflect.code.CtLocalVariable;

/**
 * User: Simon
 * Date: 28/09/16
 * Time: 14:27
 */
public interface Value {

    String getDynamicType();
    void initLocalVar(CtLocalVariable localVar, ObjectInstantiationBuilder objectInstantiationBuilder) throws Exception;
}
