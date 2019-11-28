package eu.stamp_project.utils.options;

import eu.stamp_project.dspot.amplifier.amplifiers.Amplifier;
import eu.stamp_project.dspot.amplifier.input_ampl_distributor.*;
import eu.stamp_project.dspot.amplifier.input_ampl_distributor.InputAmplDistributor;
import eu.stamp_project.dspot.amplifier.input_ampl_distributor.RandomInputAmplDistributor;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/07/18
 */
public enum InputAmplDistributorEnum {

    RandomInputAmplDistributor {
        @Override
        public InputAmplDistributor getInputAmplDistributor(int maxNumTest, List<Amplifier> amplifiers) {
            return new RandomInputAmplDistributor(maxNumTest, amplifiers);
        }
    },
    TextualDistanceInputAmplDistributor {
        @Override
        public InputAmplDistributor getInputAmplDistributor(int maxNumTest, List<Amplifier> amplifiers) {
            return new TextualDistanceInputAmplDistributor(maxNumTest, amplifiers);
        }
    },
    SimpleInputAmplDistributor {
        @Override
        public InputAmplDistributor getInputAmplDistributor(int maxNumTest, List<Amplifier> amplifiers) {
            return new SimpleInputAmplDistributor(maxNumTest, amplifiers);
        }
    };

    public abstract InputAmplDistributor getInputAmplDistributor(int maxNumTest, List<Amplifier> amplifiers);

}
