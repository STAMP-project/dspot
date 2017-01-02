package fr.inria.diversify.mutant.transformation;

import fr.inria.diversify.transformation.SpoonTransformation;
import spoon.reflect.declaration.CtElement;

/**
 * User: Simon
 * Date: 18/11/15
 * Time: 11:17
 */
public abstract class MutationTransformation<P extends  CtElement, T  extends  CtElement> extends SpoonTransformation<P , T> {

    public MutationTransformation(P transplantationPoint) {
        this.transplantationPoint = transplantationPoint;
        buildTransplant();
    }

    protected T buildReplacementElement() {
        return (T) this.transplant.clone();
    }

    protected abstract void buildTransplant();
}
