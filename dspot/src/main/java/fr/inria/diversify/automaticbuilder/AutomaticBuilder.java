package fr.inria.diversify.automaticbuilder;

import spoon.reflect.declaration.CtType;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/07/17.
 */
public interface AutomaticBuilder {

    void compile(String pathToRootOfProject);

    String buildClasspath(String pathToRootOfProject);

    void reset();

    void runPit(String pathToRootOfProject, CtType<?>... testClasses);

    void runPit(String pathToRootOfProject);

    String getOutputDirectoryPit();
}
