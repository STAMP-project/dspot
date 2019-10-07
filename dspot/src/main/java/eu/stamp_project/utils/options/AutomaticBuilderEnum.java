package eu.stamp_project.utils.options;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.automaticbuilder.gradle.GradleAutomaticBuilder;
import eu.stamp_project.automaticbuilder.maven.MavenAutomaticBuilder;
import eu.stamp_project.utils.program.InputConfiguration;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 03/10/19
 */
public enum AutomaticBuilderEnum {

    Maven() {
        @Override
        public AutomaticBuilder getAutomaticBuilder(InputConfiguration configuration) {
            return new MavenAutomaticBuilder(configuration);
        }
    },
    Gradle() {
        @Override
        public AutomaticBuilder getAutomaticBuilder(InputConfiguration configuration) {
            return new GradleAutomaticBuilder(configuration);
        }
    };

    public abstract AutomaticBuilder getAutomaticBuilder(InputConfiguration configuration);

}
