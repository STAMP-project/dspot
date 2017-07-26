package fr.inria.diversify.dspot.support;

import fr.inria.diversify.compare.ObjectLog;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.Log;
import fr.inria.diversify.util.PrintClassUtils;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.TypeUtils;
import fr.inria.stamp.test.launcher.TestLauncher;
import fr.inria.stamp.test.listener.TestListener;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import spoon.Launcher;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.*;
import spoon.support.reflect.declaration.CtMethodImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.util.stream.Collectors;

import static org.codehaus.plexus.util.FileUtils.forceDelete;

/**
 * User: Simon
 * Date: 05/04/16
 * Time: 10:28
 */
public class TestCompiler {

	public static TestListener compileAndRun(CtType<?> testClass, boolean withLog,
											 DSpotCompiler compiler, List<CtMethod<?>> testsToRun,
											 InputConfiguration configuration) {
		final InputProgram inputProgram = configuration.getInputProgram();
		final String dependencies = inputProgram.getProgramDir() + "/" + inputProgram.getClassesDir() + System.getProperty("path.separator") +
				inputProgram.getProgramDir() + "/" + inputProgram.getTestClassesDir();
		final List<CtMethod<?>> uncompilableMethods = TestCompiler.compile(compiler, testClass,
				withLog, dependencies);
		if (uncompilableMethods.contains(TestCompiler.METHOD_CODE_RETURN)) {
			return null;
		} else {
			testsToRun.removeAll(uncompilableMethods);
			uncompilableMethods.forEach(testClass::removeMethod);
			final String classPath = AmplificationHelper.getClassPath(compiler, configuration.getInputProgram());
			return TestLauncher.runFromSpoonNodes(configuration, classPath, testClass, testsToRun);
		}
	}

	@Deprecated // TODO must be reimplemented
	public static List<CtMethod<?>> compile(DSpotCompiler compiler, CtType<?> originalClassTest,
											boolean withLogger, String dependencies) {

		CtType<?> classTest = originalClassTest.clone();
		originalClassTest.getPackage().addType(classTest);
		if (withLogger) {
			copyLoggerFile(compiler);
		}

		printAndDelete(compiler, classTest);
		final List<CategorizedProblem> problems = compiler.compileAndGetProbs(dependencies);
		if (problems.isEmpty()) {
			return Collections.emptyList();
		} else {
			Log.warn("{} errors during compilation, discarding involved test methods", problems.size());
			try {
				final CtClass<?> newModelCtClass = getNewModelCtClass(compiler.getSourceOutputDirectory().getAbsolutePath(),
						classTest.getQualifiedName());
				final HashSet<CtMethod<?>> methodsToRemove = problems.stream()
						.filter(IProblem::isError)
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
				methods.addAll(compile(compiler, classTest, withLogger, dependencies));
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

	private static void printAndDelete(DSpotCompiler compiler, CtType classTest) {
		try {
			PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), classTest);
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

	private static void copyLoggerFile(DSpotCompiler compiler) {
		try {
			String comparePackage = ObjectLog.class.getPackage().getName().replace(".", "/");
			File srcDir = new File(System.getProperty("user.dir") + "/src/main/java/" + comparePackage);

			File destDir = new File(compiler.getSourceOutputDirectory() + "/" + comparePackage);
			FileUtils.forceMkdir(destDir);

			FileUtils.copyDirectory(srcDir, destDir);

			/*String typeUtilsPackage = TypeUtils.class.getPackage().getName().replace(".", "/");
			File srcFile = new File(System.getProperty("user.dir") + "/src/main/java/" + typeUtilsPackage + "/TypeUtils.java");

			destDir = new File(compiler.getSourceOutputDirectory() + "/" + typeUtilsPackage);
			FileUtils.forceMkdir(destDir);

			File destFile = new File(compiler.getSourceOutputDirectory() + "/" + typeUtilsPackage + "/TypeUtils.java");
			FileUtils.copyFile(srcFile, destFile);*/
		} catch (FileAlreadyExistsException ignored) {
			//skip
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
