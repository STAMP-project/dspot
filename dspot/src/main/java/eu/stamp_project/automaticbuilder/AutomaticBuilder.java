package eu.stamp_project.automaticbuilder;

import spoon.reflect.declaration.CtType;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public interface AutomaticBuilder {

    String compileAndBuildClasspath();

    void compile();

    String buildClasspath();

    void reset();

    void runPit(String pathToRootOfProject, CtType<?>... testClasses);

    void runPit(String pathToRootOfProject);

    String getOutputDirectoryPit();
}
