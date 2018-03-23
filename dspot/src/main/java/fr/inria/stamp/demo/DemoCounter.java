package fr.inria.stamp.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.compilation.DSpotCompiler;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.demo.json.DataJSON;
import fr.inria.stamp.demo.json.MutantDataJSON;
import fr.inria.stamp.demo.json.TestClassDataJSON;
import fr.inria.stamp.demo.json.TestClassDetailedDataJSON;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DemoCounter {

    public static void count(InputConfiguration configuration,
                             CtType<?> testClass,
                             List<CtMethod<?>> amplifiedTestMethods) {
        final List<PitResult> originalMutationAnalysis = runPit(configuration, testClass);
        final long originalNbMutantSurvived = getNumberOfGivenState(originalMutationAnalysis, PitResult.State.SURVIVED);
        final long originalNbMutantKilled = getNumberOfGivenState(originalMutationAnalysis, PitResult.State.KILLED);

        final Integer maxNumberofAssertions = amplifiedTestMethods.stream()
                .map(ctMethod -> ctMethod.getElements(AmplificationHelper.ASSERTIONS_FILTER).size())
                .max(Comparator.naturalOrder())
                .get();

        final TestClassDataJSON testClassDataJSON = new TestClassDataJSON(
                testClass.getQualifiedName(),
                originalNbMutantSurvived + originalNbMutantKilled,
                originalNbMutantKilled,
                amplifiedTestMethods.size(),
                maxNumberofAssertions
        );

        {
            final List<PitResult> pitResults = runPit(configuration, testClass);
            final long nbMutantSurvived = getNumberOfGivenState(pitResults, PitResult.State.SURVIVED);
            final long nbMutantKilled = getNumberOfGivenState(pitResults, PitResult.State.KILLED);
            testClassDataJSON.data.add(new DataJSON(nbMutantSurvived + nbMutantKilled, nbMutantKilled, 0, 0));
            createDetailledTestClassDataJson(configuration, testClass.getQualifiedName(), 0, 0, pitResults);
        }

        for (int i = 1; i < amplifiedTestMethods.size() ; i++) {
            // prepare new test class to be run
            final List<CtMethod<?>> subListOfAmplifiedTests =
                    amplifiedTestMethods.subList(1, i)
                            .stream()
                            .map(CtMethod::clone)
                            .collect(Collectors.toList());
            for (int a = 0; a < maxNumberofAssertions; a++) {
                reduceAssertions(subListOfAmplifiedTests, a);
                CtType clone = testClass.clone();
                clone.setParent(testClass.getParent());
                subListOfAmplifiedTests.forEach(clone::addMethod);
                final List<PitResult> pitResults = runPit(configuration, clone);
                final long nbMutantSurvived = getNumberOfGivenState(pitResults, PitResult.State.SURVIVED);
                final long nbMutantKilled = getNumberOfGivenState(pitResults, PitResult.State.KILLED);
                testClassDataJSON.data.add(new DataJSON(nbMutantSurvived + nbMutantKilled, nbMutantKilled, i, a));
                createDetailledTestClassDataJson(configuration, testClass.getQualifiedName(), i, a, pitResults);
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(configuration.getOutputDirectory() + "/" + testClass.getQualifiedName() + ".json");
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(testClassDataJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createDetailledTestClassDataJson(InputConfiguration configuration,
                                                         String qualifiedName,
                                                         int i,
                                                         int a,
                                                         List<PitResult> pitResults) {
        final TestClassDetailedDataJSON testClassDetailedDataJSON = new TestClassDetailedDataJSON(a, i, qualifiedName);
        pitResults.forEach(pitResult ->
                testClassDetailedDataJSON.mutants.add(new MutantDataJSON(
                        pitResult.getFullQualifiedNameOfMutatedClass(),
                        pitResult.getNameOfMutatedMethod(),
                        pitResult.getLineNumber(),
                        pitResult.getStateOfMutant().toString(),
                        pitResult.getFullQualifiedNameMutantOperator())
                )
        );
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(configuration.getOutputDirectory() + "/" + qualifiedName + "_" + a  + "_" + i + ".json");
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(testClassDetailedDataJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long getNumberOfGivenState(List<PitResult> pitResults, PitResult.State desiredState) {
        return pitResults.stream().filter(pitResult -> pitResult.getStateOfMutant() == desiredState).count();
    }

    private static void reduceAssertions(List<CtMethod<?>> subListOfAmplifiedTests, int a) {
        for (CtMethod<?> subListOfAmplifiedTest : subListOfAmplifiedTests) {
            final List<CtInvocation<?>> assertions =
                    subListOfAmplifiedTest.getElements(AmplificationHelper.ASSERTIONS_FILTER);
            final List<CtInvocation<?>> assertionsToKeep = assertions.subList(0, Math.min(a, assertions.size()));
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
