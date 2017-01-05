package fr.inria.diversify.dspot.selector;

import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public class PitMutantScoreSelector implements TestSelector {

    @Override
    public void init() {
        //TODO
    }

    @Override
    public List<CtMethod> selectToAmplify(List<CtMethod> testsToBeAmplified) {
        //TODO
        return null;
    }

    @Override
    public List<CtMethod> selectToKeep(List<CtMethod> amplifiedTestToBeKept) {
        //TODO
        return null;
    }

    @Override
    public void update() {
        //TODO
    }
}
