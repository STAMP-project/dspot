package eu.stamp_project.automaticbuilder;

import spoon.reflect.declaration.CtType;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public interface AutomaticBuilder {

    void setAbsolutePathToProjectRoot(String absolutePathToProjectRoot);

    String compileAndBuildClasspath();

    void compile();

    String buildClasspath();

    void reset();

    void runPit(CtType<?>... testClasses);

    void runPit();

    String getOutputDirectoryPit();
}
