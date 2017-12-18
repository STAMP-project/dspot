package fr.inria.diversify.dspot.selector;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 15/12/17
 */
public class ExecutedMutantSelector extends TakeAllSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(PitMutantScoreSelector.class);

    private List<PitResult> originalMutantExecuted;

    private Map<CtMethod<?>, Set<PitResult>> mutantExecutedPerAmplifiedTestMethod;

    public ExecutedMutantSelector() {
        this.mutantExecutedPerAmplifiedTestMethod = new HashMap<>();
    }

    public ExecutedMutantSelector(String pathToInitialResults) {
        this.originalMutantExecuted = PitResultParser.parse(new File(pathToInitialResults));
        this.mutantExecutedPerAmplifiedTestMethod = new HashMap<>();
    }

    @Override
    public void init(InputConfiguration configuration) {
        super.init(configuration);
        if (this.originalMutantExecuted == null) {
            LOGGER.info("Computing executed mutants by the original test suite...");
            final AutomaticBuilder automaticBuilder = AutomaticBuilderFactory.getAutomaticBuilder(this.configuration);
            automaticBuilder.runPit(this.program.getProgramDir());
            this.originalMutantExecuted =
                    PitResultParser.parseAndDelete(
                            this.program.getProgramDir() + automaticBuilder.getOutputDirectoryPit()
                    ).stream().filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED ||
                            pitResult.getStateOfMutant() == PitResult.State.SURVIVED)
                            .collect(Collectors.toList());
        }
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(List<CtMethod<?>> testsToBeAmplified) {
        if (this.currentClassTestToBeAmplified == null && !testsToBeAmplified.isEmpty()) {
            this.currentClassTestToBeAmplified = testsToBeAmplified.get(0).getDeclaringType();
            this.mutantExecutedPerAmplifiedTestMethod.clear();
            this.selectedAmplifiedTest.clear();
        }
        return testsToBeAmplified;
    }

    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
        if (amplifiedTestToBeKept.isEmpty()) {
            return amplifiedTestToBeKept;
        }

        // construct a test classes with only amplified tests
        CtType clone = this.currentClassTestToBeAmplified.clone();
        clone.setParent(this.currentClassTestToBeAmplified.getParent());
        this.currentClassTestToBeAmplified.getMethods().stream()
                .filter(AmplificationChecker::isTest)
                .forEach(clone::removeMethod);
        amplifiedTestToBeKept.forEach(clone::addMethod);

        // pretty print it
        DSpotUtils.printJavaFileWithComment(clone, new File(DSpotCompiler.pathToTmpTestSources));

        // then compile
        final String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration)
                .buildClasspath(this.program.getProgramDir())
                + AmplificationHelper.PATH_SEPARATOR +
                this.program.getProgramDir() + "/" + this.program.getClassesDir()
                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/"
                + AmplificationHelper.PATH_SEPARATOR +
                this.program.getProgramDir() + "/" + this.program.getTestClassesDir();

        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources, classpath,
                new File(this.program.getProgramDir() + "/" + this.program.getTestClassesDir()));

        AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration)
                .runPit(this.program.getProgramDir(), clone);
        final List<PitResult> pitResults = PitResultParser.parseAndDelete(program.getProgramDir() + AutomaticBuilderFactory
                .getAutomaticBuilder(this.configuration).getOutputDirectoryPit());
        final int numberOfSelectedAmplifiedTest = pitResults.stream()
                .filter(pitResult -> pitResult.getStateOfMutant() == PitResult.State.KILLED ||
                        pitResult.getStateOfMutant() == PitResult.State.SURVIVED)
                .filter(pitResult -> !this.originalMutantExecuted.contains(pitResult))
                .map(pitResult -> {
                    final CtMethod amplifiedTestThatExecuteMoreMutants = pitResult.getMethod(clone);
                    if (!this.mutantExecutedPerAmplifiedTestMethod.containsKey(amplifiedTestThatExecuteMoreMutants)) {
                        this.mutantExecutedPerAmplifiedTestMethod.put(amplifiedTestThatExecuteMoreMutants, new HashSet<>());
                    }
                    this.mutantExecutedPerAmplifiedTestMethod.get(amplifiedTestThatExecuteMoreMutants).add(pitResult);
                    this.selectedAmplifiedTest.add(amplifiedTestThatExecuteMoreMutants);
                    return amplifiedTestThatExecuteMoreMutants;
                }).collect(Collectors.toSet()).size();
        LOGGER.info("{} has been selected to amplify the test suite", numberOfSelectedAmplifiedTest);
        return amplifiedTestToBeKept;
    }
}
