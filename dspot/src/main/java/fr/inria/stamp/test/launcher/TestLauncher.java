package fr.inria.stamp.test.launcher;

import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.stamp.test.listener.TestListener;
import fr.inria.stamp.test.runner.TestRunnerFactory;
import org.junit.runner.notification.RunListener;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/07/17
 */
@Deprecated
public class TestLauncher {

	public static TestListener run(InputConfiguration configuration,
								   URLClassLoader classLoader,
								   CtType<?> testClass,
								   Collection<String> testMethodNames,
                                   RunListener... listeners) {
		if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
			final CtTypeReference<?> referenceToAbstractClass = testClass.getReference();
			return testClass.getFactory().Class().getAll().stream()
					.filter(ctType -> ctType.getSuperclass() != null)
					.filter(ctType ->
							ctType.getSuperclass().equals(referenceToAbstractClass)
					)
					.map(ctType -> run(configuration, classLoader, ctType, testMethodNames, listeners))
					.reduce(new TestListener(), TestListener::aggregate);
		}
		return TestRunnerFactory.createRunner(testClass, classLoader).run(testClass.getQualifiedName(), testMethodNames, listeners);
	}

	public static TestListener run(InputConfiguration configuration,
                                   URLClassLoader classLoader,
                                   CtType<?> testClass,
								   RunListener... listeners) {
		return run(configuration, classLoader, testClass, Collections.emptyList(), listeners);
	}


	public static TestListener run(InputConfiguration configuration,
                                   String classpath,
                                   CtType<?> testClass,
                                   Collection<String> testMethodNames,
                                   RunListener... listeners) {
		if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
			final CtTypeReference<?> referenceToAbstractClass = testClass.getReference();
			return testClass.getFactory().Class().getAll().stream()
					.filter(ctType -> ctType.getSuperclass() != null)
					.filter(ctType ->
							ctType.getSuperclass().equals(referenceToAbstractClass)
					)
					.map(subType -> run(configuration, classpath, subType, testMethodNames, listeners))
					.reduce(new TestListener(), TestListener::aggregate);
		}
		return TestRunnerFactory.createRunner(testClass, classpath).run(testClass.getQualifiedName(), testMethodNames, listeners);
	}

    public static TestListener run(InputConfiguration configuration,
                                   String classpath,
                                   CtType<?> testClass,
                                   RunListener... listeners) {
        return run(configuration, classpath, testClass, Collections.emptyList(), listeners);
    }

	public static TestListener runFromSpoonNodes(InputConfiguration configuration,
                                                 String classpath,
                                                 CtType<?> testClass,
                                                 Collection<CtMethod<?>> testMethods) {
		return run(configuration,
				classpath,
				testClass,
				testMethods.stream()
						.map(CtNamedElement::getSimpleName)
						.collect(Collectors.toList())
		);
	}
}
