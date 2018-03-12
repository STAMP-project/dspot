package fr.inria.stamp.demo;

import fr.inria.diversify.automaticbuilder.AutomaticBuilder;
import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.dspot.assertGenerator.AssertionRemover;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.mutant.pit.PitResult;
import fr.inria.diversify.mutant.pit.PitResultParser;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class DemoCounter {

    public static final int MAX_NUMBER_OF_ADDED_TEST = 20;

    public static final int MAX_NUMBER_OF_ASSERTIONS = 5;

    public static void count(InputConfiguration configuration,
                             CtType<?> testClass,
                             List<CtMethod<?>> amplifiedTestMethods) {
        AssertionRemover assertionRemover = new AssertionRemover();
        for (int i = 0; i < MAX_NUMBER_OF_ADDED_TEST; i++) {
            // prepare new test class to be run
            final List<CtMethod<?>> subListOfAmplifiedTests =
                    amplifiedTestMethods.subList(1, i)
                            .stream()
                            .map(CtMethod::clone)
                            .collect(Collectors.toList());
            for (int a = 0; a < MAX_NUMBER_OF_ASSERTIONS; a++) {
                subListOfAmplifiedTests.forEach(ctMethod -> {
                            final List<CtInvocation> assertions =
                                    ctMethod.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
                                        @Override
                                        public boolean matches(CtInvocation element) {
                                            return AmplificationChecker.isAssert(element);
                                        }
                                    });
                        }
                );
                CtType clone = testClass.clone();
                clone.setParent(testClass.getParent());
//                testClass.getMethods().stream()
//                        .filter(AmplificationChecker::isTest)
//                        .forEach(clone::removeMethod);
                subListOfAmplifiedTests.forEach(clone::addMethod);
                final List<PitResult> pitResults =
                        runPitWithGivenAmplifiedTests(configuration,
                                clone,
                                subListOfAmplifiedTests
                        );
            }
        }
    }

    private static List<PitResult> runPitWithGivenAmplifiedTests(InputConfiguration configuration,
                                                                 CtType<?> testClass,
                                                                 List<CtMethod<?>> amplifiedTestMethods) {
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
