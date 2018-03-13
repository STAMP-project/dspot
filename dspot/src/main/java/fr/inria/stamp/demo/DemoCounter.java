package fr.inria.stamp.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.demo.json.DataJSON;
import fr.inria.stamp.demo.json.TestClassDataJSON;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DemoCounter {

    public static final int MAX_NUMBER_OF_ADDED_TEST = 20;

    public static final int MAX_NUMBER_OF_ASSERTIONS = 5;

    public static void count(List<PitResult> originalMutationAnalysis,
                             InputConfiguration configuration,
                             CtType<?> testClass,
                             List<CtMethod<?>> amplifiedTestMethods) {
        final long originalNbMutantSurvived = getNumberOfGivenState(originalMutationAnalysis, PitResult.State.SURVIVED);
        final long originalNbMutantKilled = getNumberOfGivenState(originalMutationAnalysis, PitResult.State.KILLED);
        final TestClassDataJSON testClassDataJSON = new TestClassDataJSON(
                testClass.getQualifiedName(),
                originalNbMutantSurvived + originalNbMutantKilled,
                originalNbMutantKilled
        );
        for (int i = 0; i < MAX_NUMBER_OF_ADDED_TEST; i++) {
            // prepare new test class to be run
            final List<CtMethod<?>> subListOfAmplifiedTests =
                    amplifiedTestMethods.subList(1, i)
                            .stream()
                            .map(CtMethod::clone)
                            .collect(Collectors.toList());
            for (int a = 0; a < MAX_NUMBER_OF_ASSERTIONS; a++) {
                reduceAssertions(subListOfAmplifiedTests, a);
                CtType clone = testClass.clone();
                clone.setParent(testClass.getParent());
                subListOfAmplifiedTests.forEach(clone::addMethod);
                final List<PitResult> pitResults = runPit(configuration, clone);
                final long nbMutantSurvived = getNumberOfGivenState(pitResults, PitResult.State.SURVIVED);
                final long nbMutantKilled = getNumberOfGivenState(pitResults, PitResult.State.KILLED);
                testClassDataJSON.data.add(new DataJSON(nbMutantSurvived + nbMutantKilled, nbMutantKilled,i,a));
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(configuration.getOutputDirectory() + "/"  + testClass.getQualifiedName() + ".json");
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(testClassDataJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long getNumberOfGivenState(List<PitResult> pitResults, PitResult.State desiredState) {
        return pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == desiredState).count();
    }

    private static void reduceAssertions(List<CtMethod<?>> subListOfAmplifiedTests, int a) {
        for (CtMethod<?> subListOfAmplifiedTest : subListOfAmplifiedTests) {
            final List<CtInvocation> assertions =
                    subListOfAmplifiedTest.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
                        @Override
                        public boolean matches(CtInvocation element) {
                            return AmplificationChecker.isAssert(element);
                        }
                    });
            final List<CtInvocation> assertionsToKeep = assertions.subList(0, a);
            assertions.stream()
                    .filter(assertion -> !assertionsToKeep.contains(assertion))
                    .forEach(subListOfAmplifiedTest.getBody()::removeStatement);
        }
    }

    private static List<PitResult> runPit(InputConfiguration configuration, CtType<?> testClass) {
        InputProgram program = configuration.getInputProgram();
        DSpotUtils.printCtTypeToGivenDirectory(testClass, new File(DSpotCompiler.pathToTmpTestSources));
        final AutomaticBuilder automaticBuilder = AutomaticBuilderFactory
                .getAutomaticBuilder(configuration);
        final String classpath = AutomaticBuilderFactory
                .getAutomaticBuilder(configuration)
                .buildClasspath(program.getProgramDir())
                + AmplificationHelper.PATH_SEPARATOR +
                program.getProgramDir() + "/" + program.getClassesDir()
                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/"
                + AmplificationHelper.PATH_SEPARATOR +
                program.getProgramDir() + "/" + program.getTestClassesDir();
        DSpotCompiler.compile(DSpotCompiler.pathToTmpTestSources, classpath,
                new File(program.getProgramDir() + "/" + program.getTestClassesDir()));
        AutomaticBuilderFactory
                .getAutomaticBuilder(configuration)
                .runPit(program.getProgramDir(), testClass);
        return PitResultParser.parseAndDelete(
                program.getProgramDir() + automaticBuilder.getOutputDirectoryPit());
    }

}
