package eu.stamp_project.dspot.input_ampl_distributor;

import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.utils.program.InputConfiguration;
import spoon.reflect.declaration.CtType;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/11/18
 */
public abstract class AbstractInputAmplDistributor implements InputAmplDistributor {

    protected List<Amplifier> amplifiers;

    public AbstractInputAmplDistributor() {
        this.amplifiers = InputConfiguration.get().getAmplifiers();
    }

    public AbstractInputAmplDistributor(List<Amplifier> amplifiers) {
        this.amplifiers = amplifiers;
    }

    public void resetAmplifiers(CtType parentClass) {
        this.amplifiers.forEach(amplifier -> amplifier.reset(parentClass));
    }

}
