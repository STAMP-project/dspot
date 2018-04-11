package fr.inria.stamp.diff;

import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.Main;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/03/18
 */
public class SelectorOnDiff {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectorOnDiff.class);

    public static int MAX_NUMBER_TEST_CLASSES = 5;

    private static int MAX_NUMBER_TEST_CASES = 20;

    public static List<CtType> findTestClassesAccordingToADiff(InputConfiguration configuration) {
        final Factory factory = configuration.getInputProgram().getFactory();
        final String baseSha = configuration.getProperties().getProperty("baseSha");
        final String pathToFirstVersion = configuration.getProperties().getProperty("project") +
                (configuration.getProperties().getProperty("targetModule") != null ?
                        configuration.getProperties().getProperty("targetModule") : "");
        final String pathToSecondVersion = configuration.getProperties().getProperty("folderPath") +
                (configuration.getProperties().getProperty("targetModule") != null ?
                        configuration.getProperties().getProperty("targetModule") : "");
        // TODO must use the configuration to compute path to src and testSrc, in case of non-standard path
        if (configuration.getProperties().get("maxSelectedTestClasses") != null) {
            MAX_NUMBER_TEST_CLASSES = Integer.parseInt(String.valueOf(configuration.getProperties().get("maxSelectedTestClasses")));
        }
        if (Main.verbose) {
            LOGGER.info("Selecting according to a diff between {} and {} ({})",
                    pathToFirstVersion,
                    pathToSecondVersion,
                    baseSha
            );
        }

        return findTestClassesAccordingToADiff(
                configuration,
                factory,
                baseSha,
                pathToFirstVersion,
                pathToSecondVersion
        );
    }

    private static List<CtType> findTestClassesAccordingToADiff(InputConfiguration configuration,
                                                                Factory factory,
                                                                String baseSha,
                                                                String pathToFirstVersion,
                                                                String pathToSecondVersion) {
        //get the modified methods
        final Set<String> modifiedJavaFiles = pathToModifiedJavaFile(baseSha, pathToSecondVersion);

        // keep modified test in the PR: must be present in both versions of the program
        final List<CtType> modifiedTestClasses =
                getModifiedTestClasses(configuration, factory, modifiedJavaFiles);
        if (!modifiedTestClasses.isEmpty()) {
            LOGGER.info("Selection done on modified test classes");
            return reduceIfNeeded(modifiedTestClasses);
        }
        // find all modified methods
        final Set<CtMethod> modifiedMethods = modifiedJavaFiles.stream()
                .flatMap(s ->
                        getModifiedMethod(
                                pathToFirstVersion + s.substring(1),
                                pathToSecondVersion + s.substring(1)
                        ).stream()
                ).collect(Collectors.toSet());

        // find test cases that contains the name of the modified methods
        final List<CtType> testClassesThatContainsTestNamedForModifiedMethod = factory.Package().getRootPackage()
                .getElements(new TypeFilter<CtMethod>(CtMethod.class) {
                    @Override
                    public boolean matches(CtMethod element) {
                        return AmplificationChecker.isTest(element) &&
                                modifiedMethods.stream()
                                        .anyMatch(ctMethod ->
                                                element.getSimpleName().contains(ctMethod.getSimpleName())
                                        );
                    }
                }).stream().map(ctMethod -> ctMethod.getParent(CtType.class))
                .distinct()
                .collect(Collectors.toList());
        if (!testClassesThatContainsTestNamedForModifiedMethod.isEmpty()) {
            LOGGER.info("Selection done on method name convention");
            return reduceIfNeeded(testClassesThatContainsTestNamedForModifiedMethod);
        }

        //find all test classes that execute those methods
        final List<CtType> selectedTestClasses = factory.Package().getRootPackage()
                .getElements(new TypeFilter<CtExecutableReference>(CtExecutableReference.class) {
                    @Override
                    public boolean matches(CtExecutableReference element) {
                        return modifiedMethods.contains(element.getDeclaration());
                    }
                }).stream()
                .map(ctExecutableReference -> ctExecutableReference.getParent(CtType.class))
                .filter(ctType -> ctType.getMethods()
                        .stream()
                        .anyMatch(method -> {
                                    try {
                                        return AmplificationChecker.isTest((CtMethod<?>) method);
                                    } catch (Exception e) {
                                        return false;
                                    }
                                }
                        )
                ).collect(Collectors.toList());
        // TODO we may need another way to limit the number of used tests
        // TODO we can use the number of test classes
        // TODO or we could use the number of test cases
        LOGGER.info("Selection done: using tests that execute modified method");
        return reduceIfNeeded(selectedTestClasses);
    }

    private static List<CtType> reduceIfNeeded(List<CtType> selectedTestClasses) {
        if (selectedTestClasses.size() > MAX_NUMBER_TEST_CLASSES) {
            Collections.shuffle(selectedTestClasses, AmplificationHelper.getRandom());
            selectedTestClasses = selectedTestClasses.subList(0, MAX_NUMBER_TEST_CLASSES);
        }
        LOGGER.info("{} test classes selected:{}{}",
                selectedTestClasses.size(),
                AmplificationHelper.LINE_SEPARATOR,
                selectedTestClasses.stream().map(CtType::getQualifiedName)
                        .collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
        );
        return selectedTestClasses;
    }

    private final static int computeSizeOfSubstring(InputConfiguration configuration) {
        final int testSrcLength = configuration.getProperties()
                .getProperty("testSrc", "src/test/java/").length();
        final int modulePathLength = configuration.getProperties().getProperty("targetModule", "").length();
        return 2 + modulePathLength + testSrcLength; // a/targetModulePath/testSrcFolder
    }

    private static List<CtType> getModifiedTestClasses(final InputConfiguration configuration,
                                                       Factory factory,
                                                       Set<String> modifiedJavaFiles) {
        return modifiedJavaFiles.stream()
                .filter(pathToClass ->
                        new File(configuration.getProperties().getProperty("project") + pathToClass.substring(1)).exists() &&
                                new File(configuration.getProperties().getProperty("folderPath") + pathToClass.substring(1)).exists() // it is present in both versions
                ).filter(pathToClass -> {
                    final String[] split = pathToClass.split("/");
                    return (split[split.length - 1].split("\\.")[0].endsWith("Test") || // the class in a test class
                            split[split.length - 1].split("\\.")[0].startsWith("Test"));
                })
                .map(pathToClass ->
                        pathToClass.substring(computeSizeOfSubstring(configuration))
                                .split("\\.")[0]
                                .replace("/", ".")
                )// TODO maybe be more flexible on the src/main/java (use the InputConfiguration)
                .map(nameOfModifiedTestClass -> factory.Class().get(nameOfModifiedTestClass))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static Set<CtMethod> getModifiedMethod(String pathFile1, String pathFile2) {
        try {
            final File file1 = new File(pathFile1);
            final File file2 = new File(pathFile2);
            if (!file1.exists() || !file2.exists()) {
                return Collections.emptySet();
            }
            Diff result = (new AstComparator()).compare(file1, file2);
            return result.getRootOperations()
                    .stream()
                    .map(operation -> operation.getSrcNode().getParent(CtMethod.class))
                    .collect(Collectors.toSet());
        } catch (Exception ignored) {
            // if something bad happen, we do not care, we go for next file
            return Collections.emptySet();
        }
    }

    private static Set<String> pathToModifiedJavaFile(String baseSha, String pathToChangedVersion) {
        Process p;
        try {
            p = Runtime.getRuntime().exec(
                    "git diff " + baseSha,
                    new String[]{},
                    new File(pathToChangedVersion));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Set<String> modifiedJavaFiles = new BufferedReader(new InputStreamReader(p.getInputStream()))
                .lines()
                .filter(line -> line.startsWith("diff") && line.endsWith(".java"))
                .map(line -> line.split(" ")[2])
                .collect(Collectors.toSet());
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (Main.verbose) {
            LOGGER.info("Modified files:{}{}", AmplificationHelper.LINE_SEPARATOR,
                    modifiedJavaFiles.stream().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
            );
        }

        return modifiedJavaFiles;
    }

}
