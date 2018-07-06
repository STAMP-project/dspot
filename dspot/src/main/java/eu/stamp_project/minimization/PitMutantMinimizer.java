package eu.stamp_project.minimization;

import eu.stamp_project.mutant.pit.PitResult;
import eu.stamp_project.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.Map;
import java.util.Set;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class PitMutantMinimizer extends GeneralMinimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PitMutantMinimizer.class);

    private final InputConfiguration configuration;
    private CtType<?> testClass;
    private Map<CtMethod, Set<PitResult>> testThatKilledMutants;

    public PitMutantMinimizer(CtType<?> testClass,
                              InputConfiguration configuration,
                              Map<CtMethod, Set<PitResult>> testThatKilledMutants) {
        this.testThatKilledMutants = testThatKilledMutants;
        this.testClass = testClass;
        this.configuration = configuration;
    }

    @Override
    public CtMethod<?> minimize(CtMethod<?> amplifiedTestToBeMinimized) {
        CtMethod<?> reduced = super.minimize(amplifiedTestToBeMinimized);
        return reduced;
        /* TODO implement something faster, such as proposed by sbihel see #54
        final long time = System.currentTimeMillis();
        final List<CtInvocation<?>> assertions = reduced.getElements(
                new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                    @Override
                    public boolean matches(CtInvocation<?> element) {
                        return AmplificationChecker.isAssert(element);
                    }
                }
        );
        final long numberOfKilledMutant = this.testThatKilledMutants.get(amplifiedTestToBeMinimized)
                .stream()
                .filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED)
                .count();
        final List<CtInvocation<?>> removableAssertions = assertions.stream()
                .filter(invocation ->
                        runPitUsingTheGivenCtMethod(removeGivenAssertions(reduced, invocation))
                                .stream()
                                .filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED)
                                .count() == numberOfKilledMutant
                ).collect(Collectors.toList());
        final CtMethod<?> clone = reduced.clone();
        removableAssertions.forEach(clone.getBody()::removeStatement);

        LOGGER.info("Reduce {}, {} statements to {} statements in {} ms.",
                reduced.getSimpleName(),
                reduced.getBody().getStatements().size(),
                clone.getBody().getStatements().size(),
                System.currentTimeMillis() - time
        );

        return amplifiedTestToBeMinimized;
    }

    private CtMethod<?> removeGivenAssertions(CtMethod<?> amplifiedTest, CtStatement assertion) {
        final CtMethod<?> clone = amplifiedTest.clone();
        clone.getBody().removeStatement(
                clone.getBody()
                        .getStatements()
                        .stream()
                        .filter(assertion::equals)
                        .findFirst()
                        .get()
        );
        return clone;
    }

    private List<PitResult> runPitUsingTheGivenCtMethod(CtMethod<?> testCase) {
        final InputProgram program = this.configuration.getInputProgram();
        final CtType<?> clone = this.testClass.clone();
        this.testClass.getPackage().addType(clone);
        clone.getMethods().stream().filter(AmplificationChecker::isTest).forEach(clone::removeMethod);
        clone.addMethod(testCase);
        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.PATH_TO_AMPLIFIED_TEST_SRC));
        final AutomaticBuilder automaticBuilder = AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration);
        final String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration)
                .buildClasspath(program.getProgramDir())
                + AmplificationHelper.PATH_SEPARATOR +
                program.getProgramDir() + "/" + program.getClassesDir()
                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/"
                + AmplificationHelper.PATH_SEPARATOR +
                program.getProgramDir() + "/" + program.getTestClassesDir();
        DSpotCompiler.compile(DSpotCompiler.PATH_TO_AMPLIFIED_TEST_SRC, classpath,
                new File(program.getProgramDir() + "/" + program.getTestClassesDir()));
        AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration)
                .runPit(program.getProgramDir(), clone);
        return PitResultParser.parseAndDelete(program.getProgramDir() + automaticBuilder.getOutputDirectoryPit());
                */
    }
}
