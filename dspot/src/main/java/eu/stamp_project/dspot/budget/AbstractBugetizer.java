package eu.stamp_project.dspot.budget;

import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.utils.program.InputConfiguration;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/11/18
 */
public abstract class AbstractBugetizer implements Budgetizer {

    protected List<Amplifier> amplifiers;

    public AbstractBugetizer() {
        this.amplifiers = InputConfiguration.get().getAmplifiers();
    }

    public AbstractBugetizer(List<Amplifier> amplifiers) {
        this.amplifiers = amplifiers;
    }

}
