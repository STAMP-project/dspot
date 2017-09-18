package fr.inria.stamp.test.launcher;

import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.stamp.test.listener.TestListener;
import fr.inria.stamp.test.runner.DefaultTestRunner;
import org.junit.runner.notification.RunListener;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.stream.Collectors;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/07/17
 */
public class TestLauncher {

	public static TestListener run(InputConfiguration configuration, String classpath, CtType<?> testClass, Collection<String> testMethodNames) {
		Logger.reset();
		Logger.setLogDir(new File(configuration.getInputProgram().getProgramDir() + "/log"));
		if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
			final CtTypeReference<?> referenceToAbstractClass = testClass.getReference();
			return testClass.getFactory().Class().getAll().stream()
					.filter(ctType -> ctType.getSuperclass() != null)
					.filter(ctType ->
							ctType.getSuperclass().equals(referenceToAbstractClass)
					)
					.map(ctType -> run(configuration, classpath, ctType, testMethodNames))
					.reduce(new TestListener(), TestListener::aggregate);
		}
		return new DefaultTestRunner(classpath).run(testClass.getQualifiedName(), testMethodNames);
	}

	public static TestListener run(InputConfiguration configuration, URLClassLoader classLoader, CtType<?> testClass,
								   Collection<String> testMethodNames, RunListener listener) {
		Logger.reset();
		Logger.setLogDir(new File(configuration.getInputProgram().getProgramDir() + "/log"));
		if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
			final CtTypeReference<?> referenceToAbstractClass = testClass.getReference();
			return testClass.getFactory().Class().getAll().stream()
					.filter(ctType -> ctType.getSuperclass() != null)
					.filter(ctType ->
							ctType.getSuperclass().equals(referenceToAbstractClass)
					)
					.map(ctType -> run(configuration, classLoader, ctType, testMethodNames, listener))
					.reduce(new TestListener(), TestListener::aggregate);
		}
		return new DefaultTestRunner(classLoader).run(testClass.getQualifiedName(), testMethodNames, listener);
	}

	public static TestListener run(InputConfiguration configuration, URLClassLoader classLoader, CtType<?> testClass,
								   RunListener listener) {
		Logger.reset();
		Logger.setLogDir(new File(configuration.getInputProgram().getProgramDir() + "/log"));
		if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
			final CtTypeReference<?> referenceToAbstractClass = testClass.getReference();
			return testClass.getFactory().Class().getAll().stream()
					.filter(ctType -> ctType.getSuperclass() != null)
					.filter(ctType ->
							ctType.getSuperclass().equals(referenceToAbstractClass)
					)
					.map(ctType -> run(configuration, classLoader, ctType, listener))
					.reduce(new TestListener(), TestListener::aggregate);
		}
		return new DefaultTestRunner(classLoader).run(testClass.getQualifiedName(), listener);
	}


	public static TestListener runFromSpoonNodes(InputConfiguration configuration, String classpath, CtType<?> testClass, Collection<CtMethod<?>> testMethods) {
		return run(configuration,
				classpath,
				testClass,
				testMethods.stream()
						.map(CtNamedElement::getSimpleName)
						.collect(Collectors.toList())
		);
	}

	public static TestListener run(InputConfiguration configuration, String classpath, CtType<?> testClass) {
		Logger.reset();
		Logger.setLogDir(new File(configuration.getInputProgram().getProgramDir() + "/log"));
		if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
			final CtTypeReference<?> referenceToAbstractClass = testClass.getReference();
			return testClass.getFactory().Class().getAll().stream()
					.filter(ctType -> ctType.getSuperclass() != null)
					.filter(ctType ->
							ctType.getSuperclass().equals(referenceToAbstractClass)
					)
					.map(subType -> run(configuration, classpath, subType))
					.reduce(new TestListener(), TestListener::aggregate);
		}
		return new DefaultTestRunner(classpath).run(testClass.getQualifiedName());
	}

	public static TestListener run(InputConfiguration configuration, URLClassLoader classLoader, CtType<?> testClass) {
		Logger.reset();
		Logger.setLogDir(new File(configuration.getInputProgram().getProgramDir() + "/log"));
		if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
			final CtTypeReference<?> referenceToAbstractClass = testClass.getReference();
			return testClass.getFactory().Class().getAll().stream()
					.filter(ctType -> ctType.getSuperclass() != null)
					.filter(ctType ->
							ctType.getSuperclass().equals(referenceToAbstractClass)
					)
					.map(subType -> run(configuration, classLoader, subType))
					.reduce(new TestListener(), TestListener::aggregate);
		}
		return new DefaultTestRunner(classLoader).run(testClass.getQualifiedName());
	}
}
