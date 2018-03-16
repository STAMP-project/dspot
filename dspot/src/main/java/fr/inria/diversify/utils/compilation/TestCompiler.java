package fr.inria.diversify.utils.compilation;

import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.Main;
import fr.inria.stamp.test.launcher.TestLauncher;
import fr.inria.stamp.test.listener.TestListener;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.*;
import spoon.support.reflect.declaration.CtMethodImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.codehaus.plexus.util.FileUtils.forceDelete;

/**
 * User: Simon
 * Date: 05/04/16
 * Time: 10:28
 */
public class TestCompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCompiler.class);

	public static TestListener compileAndRun(CtType<?> testClass, DSpotCompiler compiler, List<CtMethod<?>> testsToRun,
											 InputConfiguration configuration) {
		final InputProgram inputProgram = configuration.getInputProgram();
		final String dependencies = inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir() + System.getProperty("path.separator") +
				inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir() + System.getProperty("path.separator") +
				"target/dspot/dependencies/";
		if (! new File("target/dspot/dependencies/compare").exists()) {
			DSpotUtils.copyPackageFromResources("fr/inria/diversify/compare/",
					"MethodsHandler", "ObjectLog", "Observation", "Utils");
		}
		final List<CtMethod<?>> uncompilableMethods = TestCompiler.compile(compiler, testClass, dependencies);
		if (uncompilableMethods.contains(TestCompiler.METHOD_CODE_RETURN)) {
			return null;
		} else {
			testsToRun.removeAll(uncompilableMethods);
			uncompilableMethods.forEach(testClass::removeMethod);
			if (testsToRun.isEmpty()) {
				return null;
			}
			final String classPath = AmplificationHelper.getClassPath(compiler, configuration);
			return TestLauncher.runFromSpoonNodes(configuration, classPath, testClass, testsToRun);
		}
	}

	public static List<CtMethod<?>> compile(DSpotCompiler compiler, CtType<?> originalClassTest,
											String dependencies) {
		CtType<?> classTest = originalClassTest.clone();
		originalClassTest.getPackage().addType(classTest);

		//TODO we should only compile the new test, and not print it. How to compile one and only one CtType with Spoon?
		printAndDelete(compiler, classTest);
		final List<CategorizedProblem> problems = compiler.compileAndGetProbs(dependencies)
				.stream()
				.filter(IProblem::isError)
				.collect(Collectors.toList());
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
				methods.addAll(compile(compiler, classTest, dependencies));
				return new ArrayList<>(methods);
			} catch (Exception e) {
				return Collections.singletonList(METHOD_CODE_RETURN);
			}
		}
	}

	public static final CtMethod<?> METHOD_CODE_RETURN = new CtMethodImpl();

	private static CtClass<?> getNewModelCtClass(String pathToSrcFolder, String fullQualifiedName) {
		Launcher launcher = new Launcher();
		launcher.getEnvironment().setNoClasspath(true);
		launcher.getEnvironment().setCommentEnabled(true);
		launcher.addInputResource(pathToSrcFolder);
		launcher.buildModel();

		return launcher.getFactory().Class().get(fullQualifiedName);
	}

	@Deprecated
	private static void printAndDelete(DSpotCompiler compiler, CtType classTest) {
		try {
			DSpotUtils.printCtTypeToGivenDirectory(classTest, compiler.getSourceOutputDirectory());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			String pathToDotClass = compiler.getBinaryOutputDirectory().getAbsolutePath() + "/" + classTest.getQualifiedName().replaceAll("\\.", "/") + ".class";
			forceDelete(pathToDotClass);
		} catch (IOException ignored) {
			//ignored
		}
	}
}
