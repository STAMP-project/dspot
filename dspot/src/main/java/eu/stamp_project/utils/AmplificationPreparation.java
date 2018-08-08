package eu.stamp_project.utils;

import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.utils.compilation.DSpotCompiler;
import org.junit.After;
import org.junit.Before;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.declaration.CtClassImpl;

import java.io.File;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/08/18
 */
public class AmplificationPreparation {

    public static CtType<?> rename(CtType<?> testClassToBeAmplified) {
        final String amplifiedName = getAmplifiedName(testClassToBeAmplified.getSimpleName());
        testClassToBeAmplified.setSimpleName(amplifiedName);
        return testClassToBeAmplified;
    }

    private static String getAmplifiedName(String originalName) {
        return originalName.startsWith("Test") ? originalName + "Ampl" : "Ampl" + originalName;
    }

    /**
     * Prepare the given test class to be amplified. Here, it will remove all the original test methods and
     * rename the test class to be amplified using {@link AmplificationPreparation#rename(CtType)}.
     * It replaces also all the references to the original test class by the amplified version.
     *
     * @param testClassToBeAmplified
     * @param testMethodsToBeAmplified
     * @return a version of the given test class ready to be amplified
     */
    public static CtType<?> prepareTestClassForAmplification(CtType<?> testClassToBeAmplified, List<CtMethod<?>> testMethodsToBeAmplified) {
        final CtType<?> amplifiedTest = rename(testClassToBeAmplified.clone());
        testClassToBeAmplified.getMethods()
                .stream()
                .filter(AmplificationChecker::isTest)
                .forEach(amplifiedTest::removeMethod);
        testClassToBeAmplified.getPackage().addType(amplifiedTest);
        final CtTypeReference classTestReference = testClassToBeAmplified.getReference();
        final String amplifiedName = getAmplifiedName(testClassToBeAmplified.getSimpleName());
        amplifiedTest.getElements(new FILTER_TYPE_REFERENCE(classTestReference))
                .forEach(ctTypeReference -> ctTypeReference.setSimpleName(amplifiedName));
        testMethodsToBeAmplified.forEach(testMethodToBeAmplified ->
                        testMethodToBeAmplified.getElements(new FILTER_TYPE_REFERENCE(classTestReference))
                                .forEach(ctTypeReference -> ctTypeReference.setSimpleName(amplifiedName))
                );
        return convertToJUnit4(amplifiedTest, InputConfiguration.get());
    }

    private static class FILTER_TYPE_REFERENCE extends TypeFilter<CtTypeReference<?>> {
        private final CtTypeReference<?> referenceToLookFor;

        public FILTER_TYPE_REFERENCE(CtTypeReference<?> reference) {
            super(CtTypeReference.class);
            this.referenceToLookFor = reference;
        }

        @Override
        public boolean matches(CtTypeReference element) {
            return element.equals(referenceToLookFor) && super.matches(element);
        }
    }

    /**
     * <p>Convert a JUnit3 test class into a JUnit4.
     * This is done in two steps:
     * <ol>
     * <li>Remove the "extends TestCase"</li>
     * <li>Add an @Test annotation, with a default value for the timeout</li>
     * </ol>
     * The timeout is added at this step since the converted test classes, and its test methods,
     * will be amplified.
     * This method convert also super classes in case they inherit from TestCase.
     * This method recompile every converted test class, because they will be executed.
     * </p>
     *
     * @param testClassJUnit3 test class to be converted
     * @return the same test class but in JUnit4
     */
    @SuppressWarnings("unchecked")
    public static CtType<?> convertToJUnit4(CtType<?> testClassJUnit3,
                                            InputConfiguration configuration) {
        if (AmplificationChecker.isTestJUnit4(testClassJUnit3)) {
            return testClassJUnit3;
        }
        final Factory factory = testClassJUnit3.getFactory();

        // convert setUp and tearDown
        convertGivenMethodWithGivenClass(testClassJUnit3, "setUp", Before.class);
        convertGivenMethodWithGivenClass(testClassJUnit3, "tearDown", After.class);

        // remove "extends TestCases"
        if (AmplificationChecker.inheritFromTestCase(testClassJUnit3)) {
            ((CtClassImpl) testClassJUnit3).setSuperclass(null);
        } else {
            if (testClassJUnit3.getSuperclass() != null) {
                CtType<?> superclass = testClassJUnit3.getSuperclass().getDeclaration();
                while (superclass != null) {
                    if (AmplificationChecker.inheritFromTestCase(superclass)) {
                        final CtType<?> convertedSuperclass = convertToJUnit4(superclass, configuration);
                        DSpotUtils.printCtTypeToGivenDirectory(convertedSuperclass,
                                new File(configuration.getAbsolutePathToTestClasses()), configuration.withComment());
                        final String classpath = configuration.getDependencies()
                                + AmplificationHelper.PATH_SEPARATOR +
                                configuration.getClasspathClassesProject()
                                + AmplificationHelper.PATH_SEPARATOR + "target/dspot/dependencies/";
                        DSpotCompiler.compile(configuration, DSpotCompiler.PATH_TO_AMPLIFIED_TEST_SRC, classpath,
                                new File(configuration.getAbsolutePathToTestClasses()));
                    }
                    if (superclass.getSuperclass() == null) {
                        break;
                    }
                    superclass = superclass.getSuperclass().getDeclaration();
                }
            }
        }

        // convertToJUnit4 JUnit3 into JUnit4 test methods
        testClassJUnit3
                .getElements(AmplificationChecker.IS_TEST_TYPE_FILTER)
                .forEach(testMethod ->
                        AmplificationHelper.prepareTestMethod(testMethod, factory)
                );

        // convert call to junit.framework.Assert calls to org.junit.Assert
        testClassJUnit3.filterChildren(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation<?> invocation) {
                return invocation.getTarget() != null &&
                        (invocation.getExecutable().getSimpleName().startsWith("assert") ||
                                invocation.getExecutable().getSimpleName().startsWith("fail")) &&
                        invocation.getTarget().getReferencedTypes().stream()
                                .anyMatch(reference ->
                                        reference.equals(
                                                factory.Type().createReference(junit.framework.TestCase.class)
                                        ) || reference.equals(
                                                factory.Type().createReference(junit.framework.Assert.class)
                                        ));
            }
        }).forEach(invocation -> ((CtInvocation) invocation).setTarget(
                factory.createTypeAccess(factory.Type().createReference(org.junit.Assert.class))
                )
        );
        return testClassJUnit3;
    }

    private static void convertGivenMethodWithGivenClass(CtType<?> testClass, String methodName,
                                                         final Class annotationClass) {
        testClass.getElements(new FILTER_OVERRIDE_METHOD_WITH_NAME(methodName))
                .forEach(ctMethod -> {
                    ctMethod.removeModifier(ModifierKind.PROTECTED);
                    ctMethod.addModifier(ModifierKind.PUBLIC);
                    ctMethod.removeAnnotation(ctMethod.getAnnotations().get(0));
                    testClass.getFactory().Annotation().annotate(ctMethod, annotationClass);
                    if (AmplificationChecker.inheritFromTestCase(testClass)) {
                        ctMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                            @Override
                            public boolean matches(CtInvocation<?> element) {
                                return element.getTarget() instanceof CtSuperAccess;
                            }
                        }).forEach(ctMethod.getBody()::removeStatement);
                    }
                });
    }

    private final static class FILTER_OVERRIDE_METHOD_WITH_NAME extends TypeFilter<CtMethod<?>> {
        private final String name;

        FILTER_OVERRIDE_METHOD_WITH_NAME(String name) {
            super(CtMethod.class);
            this.name = name;
        }

        @Override
        public boolean matches(CtMethod<?> element) {
            return element.getAnnotations().size() == 1 &&
                    element.getAnnotation(Override.class) != null &&
                    element.getSimpleName().equals(this.name);
        }
    }

}
