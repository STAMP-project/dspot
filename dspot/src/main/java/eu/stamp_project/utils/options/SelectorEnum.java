package eu.stamp_project.utils.options;

import eu.stamp_project.dspot.selector.ChangeDetectorSelector;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.dspot.selector.TakeAllSelector;
import eu.stamp_project.dspot.selector.TestSelector;

public enum SelectorEnum {
    PitMutantScoreSelector {
        @Override
        public TestSelector buildSelector() {
            return new PitMutantScoreSelector();
        }
    },
    JacocoCoverageSelector {
        @Override
        public TestSelector buildSelector() {
            return new JacocoCoverageSelector();
        }
    },
    TakeAllSelector {
        @Override
        public TestSelector buildSelector() {
            return new TakeAllSelector();
        }
    },
    ChangeDetectorSelector {
        @Override
        public TestSelector buildSelector() {
            return new ChangeDetectorSelector();
        }
    };

    public abstract TestSelector buildSelector();

}
