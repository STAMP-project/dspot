package eu.stamp_project.utils.options;

import eu.stamp_project.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.automaticbuilder.gradle.GradleAutomaticBuilder;
import eu.stamp_project.automaticbuilder.maven.MavenAutomaticBuilder;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 17/09/19
 */
public enum AutomaticBuilderEnum {

    Maven {
        @Override
        public AutomaticBuilder toAutomaticBuilder() {
            return new MavenAutomaticBuilder();
        }
    },
    Gradle {
        @Override
        public AutomaticBuilder toAutomaticBuilder() {
            return new GradleAutomaticBuilder();
        }
    };

    public abstract AutomaticBuilder toAutomaticBuilder();

}
