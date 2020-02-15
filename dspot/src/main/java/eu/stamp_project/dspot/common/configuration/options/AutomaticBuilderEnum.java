package eu.stamp_project.dspot.common.configuration.options;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.automaticbuilder.gradle.GradleAutomaticBuilder;
import eu.stamp_project.dspot.common.automaticbuilder.maven.MavenAutomaticBuilder;
import eu.stamp_project.dspot.common.configuration.UserInput;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 03/10/19
 */
public enum AutomaticBuilderEnum {

    Maven() {
        @Override
        public AutomaticBuilder getAutomaticBuilder(UserInput configuration) {
            return new MavenAutomaticBuilder(configuration);
        }
    },
    Gradle() {
        @Override
        public AutomaticBuilder getAutomaticBuilder(UserInput configuration) {
            return new GradleAutomaticBuilder(configuration);
        }
    };

    public abstract AutomaticBuilder getAutomaticBuilder(UserInput configuration);

}
