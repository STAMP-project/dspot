package eu.stamp_project.dspot.common.configuration.options;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.selector.*;

public enum SelectorEnum {
    PitMutantScoreSelector {
        @Override
        public TestSelector buildSelector(AutomaticBuilder builder, UserInput configuration) {
            return new PitMutantScoreSelector(builder, configuration);
        }
    },
    JacocoCoverageSelector {
        @Override
        public TestSelector buildSelector(AutomaticBuilder builder, UserInput configuration) {
            return new JacocoCoverageSelector(builder, configuration);
        }
    },
    TakeAllSelector {
        @Override
        public TestSelector buildSelector(AutomaticBuilder builder, UserInput configuration) {
            return new TakeAllSelector(builder, configuration);
        }
    },
    ChangeDetectorSelector {
        @Override
        public TestSelector buildSelector(AutomaticBuilder builder, UserInput configuration) {
            return new ChangeDetectorSelector(builder, configuration);
        }
    },
    ExtendedCoverageSelector {
        @Override
        public TestSelector buildSelector(AutomaticBuilder builder, UserInput configuration) {
            return new ExtendedCoverageSelector(builder, configuration);
        }
    };

    public abstract TestSelector buildSelector(AutomaticBuilder builder, UserInput configuration);

}
