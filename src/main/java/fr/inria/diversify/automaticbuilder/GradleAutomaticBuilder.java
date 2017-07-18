package fr.inria.diversify.automaticbuilder;

import fr.inria.diversify.mutant.pit.PitResult;
import spoon.reflect.declaration.CtType;

import java.util.List;

/**
 * Created by Daniele Gagliardi
 * daniele.gagliardi@eng.it
 * on 18/07/17.
 */
public class GradleAutomaticBuilder implements AutomaticBuilder {

    @Override
    public void compile(String pathToRootOfProject) {

    }

    @Override
    public String buildClasspath(String pathToRootOfProject) {
        return null;
    }

    @Override
    public List<PitResult> runPit(String pathToRootOfProject, CtType<?> testClass) {
        return null;
    }

    @Override
    public List<PitResult> runPit(String pathToRootOfProject) {
        return null;
    }

    protected void runTasks() {

    }
}
