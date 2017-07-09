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

    //TODO we should use spoon to compile.
    void compile(String pathToRootOfProject);

    String buildClasspath(String pathToRootOfProject);

    List<PitResult> runPit(String pathToRootOfProject, CtType<?> testClass);

    List<PitResult> runPit(String pathToRootOfProject);
}
