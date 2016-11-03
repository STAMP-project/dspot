package fr.inria.diversify.dspot.dynamic.objectInstanciationTree;

import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

/**
 * User: Simon
 * Date: 25/08/16
 * Time: 17:07
 */
public abstract class AbstractObjectInstantiation {

    public abstract CtTypeReference getType();

    public abstract void update(Set<ObjectInstantiation> objectInstantiations);

    public abstract boolean isOk();

    public abstract CtExpression getValue();
}
