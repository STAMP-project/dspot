package eu.stamp_project.utils.compilation;

import eu.stamp_project.Main;
import eu.stamp_project.dspot.AmplificationException;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.runner.test.TestListener;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.sosiefier.InputConfiguration;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.codehaus.plexus.util.FileUtils.forceDelete;

/**
 * User: Simon
 * Date: 05/04/16
 * Time: 10:28
 */
public class TestCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCompiler.class);

    /**
     * <p>
     * This method will compile the given test class,
     * using the {@link eu.stamp_project.utils.compilation.DSpotCompiler}.
     * If any compilation problems is reported, the method discard involved method
     * (see {@link #compileAndDiscardUncompilableMethods(DSpotCompiler, CtType, String)} and then try again to compile.
     * </p>
     *
     * @param testClass     the test class to be compiled
     * @param compiler      the compiler
     * @param testsToRun    the test methods to be run, should be in testClass
     * @param configuration
     * @return an instance of {@link eu.stamp_project.testrunner.runner.test.TestListener}
     * that contains the result of the execution of test methods if everything went fine, null otherwise.
     * @throws AmplificationException in case the compilation failed or a timeout has been thrown.
     */
    public static TestListener compileAndRun(CtType<?> testClass,
                                             DSpotCompiler compiler,
                                             List<CtMethod<?>> testsToRun,
                                             InputConfiguration configuration) throws AmplificationException {
        final String dependencies = configuration.getClasspathClassesProject()
                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/";
        DSpotUtils.copyPackageFromResources();
        final List<CtMethod<?>> uncompilableMethods =
                TestCompiler.compileAndDiscardUncompilableMethods(compiler, testClass, dependencies);
        testsToRun.removeAll(uncompilableMethods);
        uncompilableMethods.forEach(testClass::removeMethod);
        if (testsToRun.isEmpty()) {
            throw new AmplificationException("Every test methods are uncompilable");
        }
        final String classPath = AmplificationHelper.getClassPath(compiler, configuration);
        try {
            EntryPoint.defaultTimeoutInMs = 1000 + (AmplificationHelper.getTimeOutInMs() * testsToRun.size());
            if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) { // if the test class is abstract, we use one of its implementation
                return testClass.getFactory().Type()
                        .getAll()
                        .stream()
                        .filter(ctType -> ctType.getSuperclass() != null && testClass.getReference().equals(ctType.getSuperclass()))
                        .map(CtType::getQualifiedName)
                        .map(testClassName -> {
                            try {
                                return EntryPoint.runTests(
                                        classPath + AmplificationHelper.PATH_SEPARATOR + new File( "target/dspot/dependencies/").getAbsolutePath(),
                                        testClassName,
                                        testsToRun.stream()
                                                .map(CtMethod::getSimpleName)
                                                .toArray(String[]::new));
                            } catch (TimeoutException e) {
                                throw new RuntimeException(e);
                            }
                        }).reduce(TestListener::aggregate)
                        .orElse(null);

            } else {
                return EntryPoint.runTests(
                        classPath + AmplificationHelper.PATH_SEPARATOR + new File("target/dspot/dependencies/").getAbsolutePath(),
                        testClass.getQualifiedName(),
                        testsToRun.stream()
                                .map(CtMethod::getSimpleName)
                                .toArray(String[]::new)
                );
            }
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout during execution of {}: {}",
                    testClass.getQualifiedName(),
                    testsToRun.stream()
                            .map(CtMethod::getSimpleName)
                            .collect(Collectors.joining(","))
            );
            throw new AmplificationException(e);
        }
    }

    /**
     * This method compiles the given Java class using the given compiler and dependencies.
     * This method chain compilation until it succeed.
     * If a compilation fails, this method removes uncompilable methods from the given class and retry to compile.
     *
     * @param compiler
     * @param testClassToBeCompiled
     * @param dependencies
     * @return a list that contains uncompilable methods in <code>testClassToBeCompiled</code>
     * @throws AmplificationException in case the compilation thrown an exception.
     *                              This Exception is not thrown when the compilation fails but rather when the arguments are wrong.
     */
    public static List<CtMethod<?>> compileAndDiscardUncompilableMethods(DSpotCompiler compiler,
                                                                         CtType<?> testClassToBeCompiled,
                                                                         String dependencies) throws AmplificationException {
        CtType<?> classTest = testClassToBeCompiled.clone();
        testClassToBeCompiled.getPackage().addType(classTest);
        printJavaFileAndDeleteClassFile(compiler, classTest);
        final List<CategorizedProblem> problems = compiler.compileAndGetProbs(dependencies)
                .stream()
                .filter(IProblem::isError)
                .collect(Collectors.toList());
        // no problem, the compilation is successful
        if (problems.isEmpty()) {
            return Collections.emptyList();
        } else {
            LOGGER.warn("{} errors during compilation, discarding involved test methods", problems.size());
            try {
                final CtClass<?> newModelCtClass = getNewModelCtClass(compiler.getSourceOutputDirectory().getAbsolutePath(),
                        classTest.getQualifiedName());

                if (Main.verbose) {
                    int maxNumber = problems.size() > 20 ? 20 : problems.size();
                    problems.subList(0, maxNumber)
                            .forEach(categorizedProblem ->
                                    LOGGER.error("{}", categorizedProblem)
                            );
                }

                final HashSet<CtMethod<?>> methodsToRemove = problems.stream()
                        .collect(HashSet<CtMethod<?>>::new,
                                (ctMethods, categorizedProblem) -> {
                                    final Optional<CtMethod<?>> methodToRemove = newModelCtClass.getMethods().stream()
                                            .filter(ctMethod ->
                                                    ctMethod.getPosition().getSourceStart() <= categorizedProblem.getSourceStart() &&
                                                            ctMethod.getPosition().getSourceEnd() >= categorizedProblem.getSourceEnd())
                                            .findFirst();
                                    methodToRemove.ifPresent(ctMethods::add);
                                },
                                HashSet<CtMethod<?>>::addAll);

                final List<CtMethod<?>> methods = methodsToRemove.stream()
                        .map(CtMethod::getSimpleName)
                        .map(methodName -> (CtMethod<?>) classTest.getMethodsByName(methodName).get(0))
                        .collect(Collectors.toList());

                final List<CtMethod<?>> methodToKeep = newModelCtClass.getMethods().stream()
                        .filter(ctMethod -> ctMethod.getBody().getStatements().stream()
                                .anyMatch(statement ->
                                        !(statement instanceof CtComment) && !methodsToRemove.contains(ctMethod)))
                        .collect(Collectors.toList());

                methodsToRemove.addAll(
                        newModelCtClass.getMethods().stream()
                                .filter(ctMethod -> !methodToKeep.contains(ctMethod))
                                .collect(Collectors.toList())
                );

                methods.forEach(classTest::removeMethod);
                methods.addAll(compileAndDiscardUncompilableMethods(compiler, classTest, dependencies));
                return new ArrayList<>(methods);
            } catch (Exception e) {
                throw new AmplificationException(e);
            }
        }
    }

    private static CtClass<?> getNewModelCtClass(String pathToSrcFolder, String fullQualifiedName) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.addInputResource(pathToSrcFolder);
        launcher.buildModel();
        return launcher.getFactory().Class().get(fullQualifiedName);
    }

    private static void printJavaFileAndDeleteClassFile(DSpotCompiler compiler, CtType classTest) {
        try {
            DSpotUtils.printCtTypeToGivenDirectory(classTest, compiler.getSourceOutputDirectory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String pathToDotClass =
                compiler.getBinaryOutputDirectory().getAbsolutePath() + "/"
                        + classTest.getQualifiedName().replaceAll("\\.", "/") + ".class";
        try {
            forceDelete(pathToDotClass);
        } catch (IOException ignored) {
            LOGGER.warn("An exception has been thrown when trying to delete old .class file {}, continue...", pathToDotClass);
        }
    }
}
