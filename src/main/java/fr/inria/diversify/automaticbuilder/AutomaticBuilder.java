package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.runner.InputConfiguration;
import spoon.reflect.declaration.CtType;

import java.util.List;

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

    List<PitResult> runPit(String pathToRootOfProject, CtType<?> testClass);

    List<PitResult> runPit(String pathToRootOfProject);
}
