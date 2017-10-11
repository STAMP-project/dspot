package fr.inria.diversify.automaticbuilder;

import spoon.reflect.declaration.CtType;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public interface AutomaticBuilder {

    @Deprecated
    void compile(String pathToRootOfProject);

    @Deprecated // TODO this method will be replaced by a paramters of the main of DSpot.
    // TODO the idea is to extract this information for the usage as a maven plugin
    String buildClasspath(String pathToRootOfProject);

    void reset();

    void runPit(String pathToRootOfProject, CtType<?> testClass);

    void runPit(String pathToRootOfProject);

    String getOutputDirectoryPit();
}
