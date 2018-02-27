package fr.inria.stamp.minimization;

import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.utils.AmplificationChecker;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Map;
import java.util.Set;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class PitMutantMinimizer extends GeneralMinimizer {

    private Map<CtMethod, Set<PitResult>> testThatKilledMutants;

    public PitMutantMinimizer(Map<CtMethod, Set<PitResult>> testThatKilledMutants) {
        this.testThatKilledMutants = testThatKilledMutants;
    }

    @Override
    public CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized) {
        amplifiedTestToBeMinimized.getElements(
                new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                    @Override
                    public boolean matches(CtInvocation<?> element) {
                        return AmplificationChecker.isAssert(element);
                    }
                }
        );
        return super.minimize(amplifiedTestToBeMinimized);
    }
}
