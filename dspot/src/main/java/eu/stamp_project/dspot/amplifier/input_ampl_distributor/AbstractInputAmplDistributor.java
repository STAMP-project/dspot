package eu.stamp_project.dspot.amplifier.input_ampl_distributor;

import eu.stamp_project.dspot.amplifier.amplifiers.Amplifier;
import spoon.reflect.declaration.CtType;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/11/18
 */
public abstract class AbstractInputAmplDistributor implements InputAmplDistributor {

    protected List<Amplifier> amplifiers;

    protected int maxNumTests;

    public AbstractInputAmplDistributor(int maxNumTests, List<Amplifier> amplifiers) {
        this.maxNumTests = maxNumTests;
        this.amplifiers = amplifiers;
    }

    public void resetAmplifiers(CtType parentClass) {
        this.amplifiers.forEach(amplifier -> amplifier.reset(parentClass));
    }

    @Override
    public boolean shouldBeRun() {
        return !this.amplifiers.isEmpty();
    }
}
