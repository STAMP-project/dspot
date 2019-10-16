package eu.stamp_project.utils.options;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.selector.ChangeDetectorSelector;
import eu.stamp_project.dspot.selector.JacocoCoverageSelector;
import eu.stamp_project.dspot.selector.PitMutantScoreSelector;
import eu.stamp_project.dspot.selector.TakeAllSelector;
import eu.stamp_project.dspot.selector.TestSelector;
import eu.stamp_project.utils.execution.TestRunner;
import eu.stamp_project.utils.program.InputConfiguration;

public enum SelectorEnum {
    PitMutantScoreSelector {
        @Override
        public TestSelector buildSelector(AutomaticBuilder builder, InputConfiguration configuration, TestRunner testRunner) {
            return new PitMutantScoreSelector(builder, configuration, testRunner);
        }
    },
    JacocoCoverageSelector {
        @Override
        public TestSelector buildSelector(AutomaticBuilder builder, InputConfiguration configuration, TestRunner testRunner) {
            return new JacocoCoverageSelector(builder, configuration, testRunner);
        }
    },
    TakeAllSelector {
        @Override
        public TestSelector buildSelector(AutomaticBuilder builder, InputConfiguration configuration, TestRunner testRunner) {
            return new TakeAllSelector(builder, configuration, testRunner);
        }
    },
    ChangeDetectorSelector {
        @Override
        public TestSelector buildSelector(AutomaticBuilder builder, InputConfiguration configuration, TestRunner testRunner) {
            return new ChangeDetectorSelector(builder, configuration, testRunner);
        }
    };

    public abstract TestSelector buildSelector(AutomaticBuilder builder, InputConfiguration configuration, TestRunner testRunner);

}
