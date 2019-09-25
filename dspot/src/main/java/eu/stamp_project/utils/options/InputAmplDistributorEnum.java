package eu.stamp_project.utils.options;

import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.input_ampl_distributor.*;
import eu.stamp_project.dspot.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.input_ampl_distributor.RandomInputAmplDistributor;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/07/18
 */
public enum InputAmplDistributorEnum {

    RandomInputAmplDistributor {
        @Override
        public InputAmplDistributor getInputAmplDistributor() {
            return new RandomInputAmplDistributor();
        }
        @Override
        public InputAmplDistributor getInputAmplDistributor(List<Amplifier> amplifiers) {
            return new RandomInputAmplDistributor(amplifiers);
        }
    },
    TextualDistanceInputAmplDistributor {
        @Override
        public InputAmplDistributor getInputAmplDistributor() {
            return new TextualDistanceInputAmplDistributor();
        }
        @Override
        public InputAmplDistributor getInputAmplDistributor(List<Amplifier> amplifiers) {
            return new TextualDistanceInputAmplDistributor(amplifiers);
        }
    },
    SimpleInputAmplDistributor {
        @Override
        public InputAmplDistributor getInputAmplDistributor() {
            return new SimpleInputAmplDistributor();
        }
        @Override
        public InputAmplDistributor getInputAmplDistributor(List<Amplifier> amplifiers) {
            return new SimpleInputAmplDistributor(amplifiers);
        }
    };

    public abstract InputAmplDistributor getInputAmplDistributor();

    public abstract InputAmplDistributor getInputAmplDistributor(List<Amplifier> amplifiers);

}
