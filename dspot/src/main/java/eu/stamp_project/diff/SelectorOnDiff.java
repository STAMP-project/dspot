package eu.stamp_project.diff;

import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.AmplificationHelper;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/03/18
 */
public class SelectorOnDiff {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectorOnDiff.class);

    /**
     * @param configuration of the project under amplification. This configuration must contain the following properties:
     *                      baseSha: with the commit sha of the base branch
     *                      project: with the path to the base project
     *                      folderPath: with the path to the changed project
     * @return a map that associates the full qualified name of test classes to their test methods to be amplified.
     */
    public static Map<String, List<String>> findTestMethodsAccordingToADiff(InputConfiguration configuration) {
        final Factory factory = configuration.getFactory();
        final String baseSha = configuration.getBaseSha();
        final String pathToFirstVersion = configuration.getAbsolutePathToProjectRoot();
        final String pathToSecondVersion = configuration.getAbsolutePathToSecondVersionProjectRoot();
        if (configuration.isVerbose()) {
            LOGGER.info("Selecting according to a diff between {} and {} ({})",
                    pathToFirstVersion,
                    pathToSecondVersion,
                    baseSha
            );
        }

        return new SelectorOnDiff(configuration,
                factory,
                baseSha,
                pathToFirstVersion,
                pathToSecondVersion
        ).findTestMethods();
    }

    private InputConfiguration configuration;
    private Factory factory;
    private String baseSha;
    private String pathToFirstVersion;
    private String pathToSecondVersion;

    /**
     * Constructor. Please, have look to {@link  eu.stamp_project.diff.SelectorOnDiff#findTestMethodsAccordingToADiff(InputConfiguration)}.
     * The usage of this constructor and the method  {@link  eu.stamp_project.diff.SelectorOnDiff#findTestMethods()} is discouraged.
     *
     * @param configuration
     * @param factory
     * @param baseSha
     * @param pathToFirstVersion
     * @param pathToSecondVersion
     */
    public SelectorOnDiff(InputConfiguration configuration,
                          Factory factory,
                          String baseSha,
                          String pathToFirstVersion,
                          String pathToSecondVersion) {
        this.configuration = configuration;
        this.factory = factory;
        this.baseSha = baseSha;
        this.pathToFirstVersion = pathToFirstVersion;
        this.pathToSecondVersion = pathToSecondVersion;
    }

    /**
     * This method does the same job than {@link  eu.stamp_project.diff.SelectorOnDiff#findTestMethodsAccordingToADiff(InputConfiguration)} but use an instance.
     * It is more convenient to use the static method {@link  eu.stamp_project.diff.SelectorOnDiff#findTestMethodsAccordingToADiff(InputConfiguration)}
     * which instantiate and set specific value rather than use this method.
     *
     * @return a map that associates the full qualified name of test classes to their test methods to be amplified.
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> findTestMethods() {
        Map<String, List<String>> selection = new HashMap<>();
        final Set<CtMethod> selectedTestMethods = new HashSet<>();
        // get the modified files
        final Set<String> modifiedJavaFiles = getModifiedJavaFiles();
        // get modified methods
        final Set<CtMethod> modifiedMethods = getModifiedMethods(modifiedJavaFiles);
        // get modified test cases
        final List<CtMethod> modifiedTestMethods = modifiedMethods.stream()
                .filter(AmplificationChecker::isTest)
                .collect(Collectors.toList());
        modifiedMethods.removeAll(modifiedTestMethods);
        if (!modifiedTestMethods.isEmpty()) { // if any, we add them to the selection
            LOGGER.info("Select {} modified test methods", modifiedTestMethods.size());
            selectedTestMethods.addAll(modifiedTestMethods);
        }
        // get all invocations to modified methods in test methods
        final List<CtMethod<?>> testMethodsThatExecuteDirectlyModifiedMethods =
                getTestMethodsThatExecuteDirectlyModifiedMethods(modifiedMethods, modifiedTestMethods);
        if (!modifiedTestMethods.isEmpty()) { // if any, we add them to the selection
            LOGGER.info("Select {} test methods that execute directly modified methods", modifiedTestMethods.size());
            selectedTestMethods.addAll(testMethodsThatExecuteDirectlyModifiedMethods);
        }
        // if we could not find any test methods above, we use naming convention
        if (selectedTestMethods.isEmpty()) {
            final List<CtMethod<?>> testMethodsAccordingToNameOfModifiedMethod =
                    getTestMethodsAccordingToNameOfModifiedMethod(modifiedMethods);
            // if any test methods has the name of a modified in its own name
            if (!testMethodsAccordingToNameOfModifiedMethod.isEmpty()) {
                selectedTestMethods.addAll(testMethodsAccordingToNameOfModifiedMethod);
            } else {
                final Set<CtMethod<?>> methodsOfTestClassesAccordingToModifiedJavaFiles =
                        getMethodsOfTestClassesAccordingToModifiedJavaFiles(modifiedJavaFiles);
                // we try to find test classes for modified java file,
                // e.g. ExampleClass is modified
                // we look for a test class named TestExampleClass or ExampleClassTest
                // also, if one of these test classes does not contain any test, but inherit from another test class,
                // we select the super class to be amplified
                if (!methodsOfTestClassesAccordingToModifiedJavaFiles.isEmpty()) {
                    selectedTestMethods.addAll(methodsOfTestClassesAccordingToModifiedJavaFiles);
                }
            }
        }

        // if no test could be find, amplify all the test methods inside modified test classes
        if (selectedTestMethods.isEmpty()) {
            selectedTestMethods.addAll(getTestMethodsOfModifiedTestClasses(modifiedJavaFiles));
        }
        if (selectedTestMethods.isEmpty()) {
            LOGGER.warn("No tests could be found for {}", modifiedJavaFiles);
            LOGGER.warn("DSpot will stop here, since it cannot find any tests to amplify according to the provided diff");
            return selection;
        }
        for (CtMethod selectedTestMethod : selectedTestMethods) {
            final CtType parent = selectedTestMethod.getParent(CtType.class);
            if (!selection.containsKey(parent.getQualifiedName())) {
                selection.put(parent.getQualifiedName(), new ArrayList<>());
            }
            selection.get(parent.getQualifiedName()).add(selectedTestMethod.getSimpleName());
        }
        return selection;
    }

    private Set<CtMethod> getTestMethodsOfModifiedTestClasses(Set<String> modifiedJavaFiles) {
        return modifiedJavaFiles.stream()
                .filter(presentInBothVersion)
                .map(modifiedJavaFile -> {
                    final String directoryPath =
                            (!this.configuration.getTargetModule().isEmpty() ?
                                    this.configuration.getTargetModule() + "/" : ""
                            ) + this.configuration.getPathToSourceCode();
                    final String qualifiedNameWithExtension = modifiedJavaFile.substring(directoryPath.length() + 2).replaceAll("/", ".");
                    return qualifiedNameWithExtension.substring(0, qualifiedNameWithExtension.length() - ".java".length());
                })
                .map(fullQualifiedName -> this.factory.Class().get(fullQualifiedName))
                .filter(Objects::nonNull)
                .filter(potentialTestClass -> potentialTestClass.getMethods().stream().anyMatch(AmplificationChecker::isTest))
                .flatMap(testClass -> testClass.getMethods().stream().filter(AmplificationChecker::isTest))
                .collect(Collectors.toSet());
    }

    // TODO
    /*
    private Predicate<String> presentInBothVersion = pathToClass ->
            new File(configuration.getProperties().getProperty("project") + pathToClass.substring(1)).exists() &&
            new File(configuration.getProperties().getProperty("folderPath") + pathToClass.substring(1)).exists();
     */
    private Predicate<String> presentInBothVersion = pathToClass ->
            new File(this.pathToFirstVersion + pathToClass.substring(1)).exists() &&
            new File(this.pathToSecondVersion + pathToClass.substring(1)).exists();

    private Set<CtMethod<?>> getMethodsOfTestClassesAccordingToModifiedJavaFiles(Set<String> modifiedJavaFiles) {
        final List<String> candidateTestClassName = modifiedJavaFiles.stream()
                .filter(presentInBothVersion)
                .flatMap(pathToClass -> {
                    final String directoryPath =
                            (!this.configuration.getTargetModule().isEmpty() ?
                                    this.configuration.getTargetModule() + "/" : ""
                            ) + this.configuration.getPathToSourceCode();
                    final String[] split = pathToClass.substring(directoryPath.length() + 2).split("/");
                    final String nameOfTestClass = split[split.length - 1].split("\\.")[0];
                    final String qualifiedName = IntStream
                            .range(0, split.length - 1)
                            .mapToObj(value -> split[value])
                            .collect(Collectors.joining("."));
                    return Stream.of(
                            qualifiedName + "." + nameOfTestClass + "Test",
                            qualifiedName + "." + "Test" + nameOfTestClass
                    );
                }).collect(Collectors.toList());
        // test classes directly dedicated to modified java files.
        return candidateTestClassName.stream()
                .map(testClassName -> this.factory.Type().get(testClassName))
                .filter(testClass ->
                        testClass != null &&
                                (testClass.getMethods().stream().anyMatch(AmplificationChecker::isTest) ||
                                        testClass.getSuperclass()
                                                .getTypeDeclaration()
                                                .getMethods()
                                                .stream()
                                                .anyMatch(AmplificationChecker::isTest)
                                )
                ).flatMap(testClass -> {
                    if (testClass.getMethods().stream().noneMatch(AmplificationChecker::isTest)) {
                        return testClass.getSuperclass().getTypeDeclaration().getMethods().stream();
                    } else {
                        return testClass.getMethods().stream();
                    }
                })
                .filter(AmplificationChecker::isTest)
                .collect(Collectors.toSet());
    }

    private List<CtMethod<?>> getTestMethodsAccordingToNameOfModifiedMethod(Set<CtMethod> modifiedMethods) {
        final Set<String> modifiedMethodsNames =
                modifiedMethods.stream().map(CtMethod::getSimpleName).collect(Collectors.toSet());
        return this.factory.Package().getRootPackage()
                .getElements(new TypeFilter<CtMethod<?>>(CtMethod.class) {
                    @Override
                    public boolean matches(CtMethod<?> element) {
                        return AmplificationChecker.isTest(element) &&
                                modifiedMethodsNames.stream()
                                        .anyMatch(element.getSimpleName()::contains);
                    }
                });
    }


    private List getTestMethodsThatExecuteDirectlyModifiedMethods(Set<CtMethod> modifiedMethods,
                                                                  List<CtMethod> modifiedTestMethods) {
        return this.factory.Package().getRootPackage()
                .getElements(new TypeFilter<CtExecutableReference>(CtExecutableReference.class) {
                    @Override
                    public boolean matches(CtExecutableReference element) {
                        return modifiedMethods.contains(element.getDeclaration());
                    }
                }).stream()
                .map(ctExecutableReference -> ctExecutableReference.getParent(CtMethod.class))
                .filter(Objects::nonNull)
                .filter(AmplificationChecker::isTest)
                .filter(ctMethod -> !(modifiedTestMethods.contains(ctMethod)))
                .collect(Collectors.toList());
    }

    public Set<CtMethod> getModifiedMethods(Set<String> modifiedJavaFiles) {
        return modifiedJavaFiles.stream()
                .flatMap(s ->
                        getModifiedMethods(pathToFirstVersion + s.substring(1),
                                pathToSecondVersion + s.substring(1)
                        )
                ).collect(Collectors.toSet());
    }

    public Stream<CtMethod> getModifiedMethods(String pathFile1, String pathFile2) {
        try {
            final File file1 = new File(pathFile1);
            final File file2 = new File(pathFile2);
            if (!file1.exists() || !file2.exists()) {
                return Stream.of();
            }
            Diff result = (new AstComparator()).compare(file1, file2);
            return result.getRootOperations()
                    .stream()
                    .map(operation -> operation.getSrcNode().getParent(CtMethod.class))
                    .filter(Objects::nonNull)
                    .map(this::getSameMethodFromAnotherFactory)
                    .filter(Objects::nonNull); // it seems that gumtree can return null value;
        } catch (Exception ignored) {
            // if something bad happen, we do not care, we go for next file
            return Stream.of();
        }
    }

    private CtMethod getSameMethodFromAnotherFactory(CtMethod<?> methodToFoundInAnotherFactory) {
        CtType<?> declaringType = methodToFoundInAnotherFactory.getDeclaringType();
        while (!declaringType.isTopLevel()) {
            declaringType = declaringType.getParent(CtType.class);
        }
        return factory.Class().get(declaringType.getQualifiedName()).getMethod(
                methodToFoundInAnotherFactory.getType(),
                methodToFoundInAnotherFactory.getSimpleName(),
                (CtTypeReference<?>[]) methodToFoundInAnotherFactory.getParameters()
                        .stream()
                        .map(parameter -> ((CtParameter) parameter).getType())
                        .toArray((IntFunction<CtTypeReference<?>[]>) CtTypeReference[]::new)
        );
    }

    private Set<String> getModifiedJavaFiles() {
        Process p;
        try {
            p = Runtime.getRuntime().exec(
                    "git diff " + this.baseSha,
                    new String[]{},
                    new File(this.pathToSecondVersion));
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

        if (configuration.isVerbose()) {
            LOGGER.info("Modified files:{}{}", AmplificationHelper.LINE_SEPARATOR,
                    modifiedJavaFiles.stream().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
            );
        }

        return modifiedJavaFiles;
    }

}
