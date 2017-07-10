package fr.inria.stamp.test.launcher;

import fr.inria.diversify.logger.Logger;
import fr.inria.diversify.runner.InputConfiguration;
import fr.inria.stamp.test.listener.TestListener;
import fr.inria.stamp.test.runner.DefaultTestRunner;
import fr.inria.stamp.test.runner.MockitoTestRunner;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import java.io.File;
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
		final TypeTestEnum typeTest = TypeTestEnum.getTypeTest(testClass);
		if (typeTest == TypeTestEnum.DEFAULT) {
			return new DefaultTestRunner(classpath).run(testClass.getQualifiedName(), testMethodNames);
		} else {
			return new MockitoTestRunner(classpath).run(testClass.getQualifiedName(), testMethodNames);
		}
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
		final TypeTestEnum typeTest = TypeTestEnum.getTypeTest(testClass);
		if (typeTest == TypeTestEnum.DEFAULT) {
			return new DefaultTestRunner(classpath).run(testClass.getQualifiedName());
		} else {
			return new MockitoTestRunner(classpath).run(testClass.getQualifiedName());
		}
	}
}
