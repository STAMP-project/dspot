package eu.stamp_project.utils.compilation;

import eu.stamp_project.dspot.AmplificationException;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.runner.test.TestListener;
import eu.stamp_project.utils.AmplificationHelper;
import eu.stamp_project.utils.CloneHelper;
import eu.stamp_project.utils.DSpotUtils;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
     * Create a clone of the test class, using {@link CloneHelper#cloneTestClassAndAddGivenTest(CtType, List)}.
     * Then, compile and run the test using {@link eu.stamp_project.utils.compilation.TestCompiler#compileAndRun(CtType, DSpotCompiler, List, InputConfiguration)}
     * Finally, discard all failing test methods
     *
     * @param classTest       Test class
     * @param currentTestList test methods to be run
     * @return Results of tests' run
     * @throws AmplificationException forward the AmplificationException thrown by {@link eu.stamp_project.utils.compilation.TestCompiler#compileAndRun(CtType, DSpotCompiler, List, InputConfiguration)}
     */
    public static List<CtMethod<?>> compileRunAndDiscardUncompilableAndFailingTestMethods(CtType classTest,
                                                                                    List<CtMethod<?>> currentTestList,
                                                                                    DSpotCompiler compiler,
                                                                                    InputConfiguration configuration) {
        CtType amplifiedTestClass = CloneHelper.cloneTestClassAndAddGivenTest(classTest, currentTestList);
        try {
            final TestListener result = TestCompiler.compileAndRun(
                    amplifiedTestClass,
                    compiler,
                    currentTestList,
                    configuration
            );
            return AmplificationHelper.getPassingTests(currentTestList, result);
        } catch (AmplificationException e) {
            if (configuration.isVerbose()) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }
    }

    /**
     * <p>
     * This method will compile the given test class,
     * using the {@link eu.stamp_project.utils.compilation.DSpotCompiler}.
     * If any compilation problems is reported, the method discard involved test methods, by modifying given test methods, (it has side-effect)
     * (see {@link #compileAndDiscardUncompilableMethods(DSpotCompiler, CtType, String, List)} and then try again to compile.
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
                + AmplificationHelper.PATH_SEPARATOR + DSpotUtils.getAbsolutePathToDSpotDependencies();
        DSpotUtils.copyPackageFromResources();
        testsToRun = TestCompiler.compileAndDiscardUncompilableMethods(compiler, testClass, dependencies, testsToRun);
        final String classPath = AmplificationHelper.getClassPath(compiler, configuration);
        EntryPoint.timeoutInMs = 1000 + (configuration.getTimeOutInMs() * testsToRun.size());
        if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) { // if the test class is abstract, we use one of its implementation
            return TestRunner.runSubClassesForAbstractTestClass(testClass, testsToRun, classPath);
        } else {
            return TestRunner.runGivenTestMethods(testClass, testsToRun, classPath);
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
     *                                This Exception is not thrown when the compilation fails but rather when the arguments are wrong.
     */
    public static List<CtMethod<?>> compileAndDiscardUncompilableMethods(DSpotCompiler compiler,
                                                                         CtType<?> testClassToBeCompiled,
                                                                         String dependencies,
                                                                         List<CtMethod<?>> testsToRun) throws AmplificationException {
        final List<CtMethod<?>> uncompilableMethod = compileAndDiscardUncompilableMethods(compiler, testClassToBeCompiled, dependencies, 0);
        testsToRun.removeAll(uncompilableMethod);
        uncompilableMethod.forEach(testClassToBeCompiled::removeMethod);
        if (testsToRun.isEmpty()) {
            throw new AmplificationException("Every test methods are uncompilable");
        }
        return testsToRun;
    }

    private static List<CtMethod<?>> compileAndDiscardUncompilableMethods(DSpotCompiler compiler,
                                                                          CtType<?> testClassToBeCompiled,
                                                                          String dependencies,
                                                                          int numberOfTry) {

        printJavaFileAndDeleteClassFile(compiler, testClassToBeCompiled);
        final List<CategorizedProblem> problems = compiler.compileAndReturnProblems(dependencies)
                .stream()
                .filter(IProblem::isError)
                .collect(Collectors.toList());
        // no problem, the compilation is successful
        if (problems.isEmpty()) {
            return Collections.emptyList();
        } else if (numberOfTry > 3) {
            LOGGER.warn("Trying three time to compile with no success. Give up.");
            return Collections.emptyList();
        } else {
            int maxNumber = problems.size() > 20 ? 20 : problems.size();
            LOGGER.error("Error(s) during compilation:");
            problems.subList(0, maxNumber).forEach(categorizedProblem -> LOGGER.error("{}", categorizedProblem));
            // Here, we compute the spoon model of the compiled test class,
            // since it does not match with the model given in parameter.
            // TODO report this to Spoon ?
            final CtClass<?> newModelCtClass = getNewModelCtClass(compiler.getSourceOutputDirectory().getAbsolutePath(), testClassToBeCompiled.getQualifiedName());
            final HashSet<CtMethod<?>> methodsToRemove = getMethodToRemove(problems, newModelCtClass);
            final List<CtMethod<?>> methodsToRemoveInOriginalModel = methodsToRemove.stream()
                    .map(CtMethod::getSimpleName)
                    .map(methodName -> (CtMethod<?>) testClassToBeCompiled.getMethodsByName(methodName).get(0))
                    .collect(Collectors.toList());

            // TODO can't remember why I did that
            /*final List<CtMethod<?>> methodToKeep = newModelCtClass.getMethods().stream()
                    .filter(ctMethod -> ctMethod.getBody().getStatements().stream()
                            .anyMatch(statement ->
                                    !(statement instanceof CtComment) && !methodsToRemove.contains(ctMethod)))
                    .collect(Collectors.toList());

            methodsToRemove.addAll(
                    newModelCtClass.getMethods().stream()
                            .filter(ctMethod -> !methodToKeep.contains(ctMethod))
                            .collect(Collectors.toList())
            );*/
            methodsToRemoveInOriginalModel.forEach(testClassToBeCompiled::removeMethod);
            final List<CtMethod<?>> recursiveMethodToRemove =
                    compileAndDiscardUncompilableMethods(compiler, testClassToBeCompiled, dependencies, numberOfTry + 1);
            methodsToRemoveInOriginalModel.addAll(recursiveMethodToRemove);
            return new ArrayList<>(methodsToRemoveInOriginalModel);
        }
    }

    // compute the CtMethod in the new model to remove according to the given compilation problems
    private static HashSet<CtMethod<?>> getMethodToRemove(List<CategorizedProblem> problems, CtClass<?> newModelCtClass) {
        return problems.stream()
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
    }

    // compute a new spoon model for the given CtClass
    private static CtClass<?> getNewModelCtClass(String pathToSrcFolder, String fullQualifiedName) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.addInputResource(pathToSrcFolder);
        launcher.buildModel();
        return launcher.getFactory().Class().get(fullQualifiedName);
    }

    // output the .java of the test class to be compiled
    // this method delete also the old .class, i.e. the old compiled file of the same test class, if exists
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
