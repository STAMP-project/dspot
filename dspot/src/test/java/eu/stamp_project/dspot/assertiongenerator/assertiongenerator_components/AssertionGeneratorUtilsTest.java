package eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.assertiongenerator.assertiongenerator_components.AssertionGeneratorUtils;
import eu.stamp_project.test_framework.TestFramework;
import eu.stamp_project.utils.program.InputConfiguration;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public class AssertionGeneratorUtilsTest extends AbstractTest {

    @Test
    public void testCanGenerateAssertionFor() {
        assertFalse(AssertionGeneratorUtils.canGenerateAnAssertionFor("yes/no"));

        assertFalse(AssertionGeneratorUtils.canGenerateAnAssertionFor(InputConfiguration.get().getAbsolutePathToProjectRoot()));
        assertFalse(AssertionGeneratorUtils.canGenerateAnAssertionFor(InputConfiguration.get().getAbsolutePathToProjectRoot() + " is a directory"));
        assertTrue(AssertionGeneratorUtils.canGenerateAnAssertionFor("This is not a path"));

        assertTrue(AssertionGeneratorUtils.canGenerateAnAssertionFor("thaliana"));
        assertTrue(AssertionGeneratorUtils.canGenerateAnAssertionFor("thaliana.thaliana@"));
        assertTrue(AssertionGeneratorUtils.canGenerateAnAssertionFor("thaliana.thaliana$f465"));
        assertTrue(AssertionGeneratorUtils.canGenerateAnAssertionFor("thaliana.thaliana@z0545"));
        assertFalse(AssertionGeneratorUtils.canGenerateAnAssertionFor("thaliana@041a"));
        assertFalse(AssertionGeneratorUtils.canGenerateAnAssertionFor("thaliana.thaliana@041a"));
        assertFalse(AssertionGeneratorUtils.canGenerateAnAssertionFor(new Object().toString()));
        assertFalse(AssertionGeneratorUtils.canGenerateAnAssertionFor("Expected message : " + new Object().toString() + "not found"));
        assertFalse(AssertionGeneratorUtils.canGenerateAnAssertionFor("Expected message : " + new Object().toString()));
        assertFalse(AssertionGeneratorUtils.canGenerateAnAssertionFor(new Object().toString() + "not found"));

        InputConfiguration.get().setAllowPathInAssertion(true);
        assertTrue(AssertionGeneratorUtils.canGenerateAnAssertionFor("yes/no"));
    }

    @Test
    public void testContainsAPath() {

        /*
            Test the method to check if a string contains a path
         */

        assertTrue(AssertionGeneratorUtils.containsAPath(InputConfiguration.get().getAbsolutePathToProjectRoot()));
        assertTrue(AssertionGeneratorUtils.containsAPath("yes/no"));
        assertTrue(AssertionGeneratorUtils.containsAPath(InputConfiguration.get().getAbsolutePathToProjectRoot() + " is a directory"));
        assertFalse(AssertionGeneratorUtils.containsAPath("This is not a path"));
    }

    @Test
    public void testGetCorrectTypeOfInvocation() {

        /*
            Test that we remove correctly the actual type argument if it is generics.

            TODO: I don't understand why Spoon change the <T> by <java.lang.Object> and makes the test failing...
         */

        final Factory factory = InputConfiguration.get().getFactory();
        final CtClass<?> myClassWithSpecificReturnType = factory.Class().get("fr.inria.ClassWithSpecificReturnType");
        final CtMethod<?> tryGetters = myClassWithSpecificReturnType.getMethodsByName("tryGetters").get(0);
        final List<CtInvocation> invocations = tryGetters.getElements(new TypeFilter<>(CtInvocation.class));
        CtTypeReference correctTypeOfInvocation = AssertionGeneratorUtils.getCorrectTypeOfInvocation(invocations.get(0));
        assertEquals("doest not have the correct type",
                "fr.inria.ClassWithSpecificReturnType.Element<?>",
                correctTypeOfInvocation.toString()
        );
        correctTypeOfInvocation = AssertionGeneratorUtils.getCorrectTypeOfInvocation(invocations.get(1));
        /*assertEquals("doest not have the correct type",
                "fr.inria.ClassWithSpecificReturnType.Element",
                correctTypeOfInvocation.toString()
        );*/
        correctTypeOfInvocation = AssertionGeneratorUtils.getCorrectTypeOfInvocation(invocations.get(2));
        assertEquals("doest not have the correct type",
                "fr.inria.ClassWithSpecificReturnType.Element<java.lang.String>",
                correctTypeOfInvocation.toString()
        );
    }

    @Test
    public void testContainsObjectReferences() throws Exception {
        assertFalse(AssertionGeneratorUtils.containsObjectReferences("thaliana"));
        assertFalse(AssertionGeneratorUtils.containsObjectReferences("thaliana.thaliana@"));
        assertFalse(AssertionGeneratorUtils.containsObjectReferences("thaliana.thaliana$f465"));
        assertFalse(AssertionGeneratorUtils.containsObjectReferences("thaliana.thaliana@z0545"));
        assertTrue(AssertionGeneratorUtils.containsObjectReferences("thaliana@041a"));
        assertTrue(AssertionGeneratorUtils.containsObjectReferences("thaliana.thaliana@041a"));
        assertTrue(AssertionGeneratorUtils.containsObjectReferences(new Object().toString()));
        assertTrue(AssertionGeneratorUtils.containsObjectReferences("Expected message : " + new Object().toString() + "not found"));
        assertTrue(AssertionGeneratorUtils.containsObjectReferences("Expected message : " + new Object().toString()));
        assertTrue(AssertionGeneratorUtils.containsObjectReferences(new Object().toString() + "not found"));
    }

    @Test
    public void testAddAfterClassMethod() throws Exception {

        /*
            test the method addAfterClassMethod
                1 - it generates the whole method, since it does not exist
                2 - it adds at the end of the existing method an invocation to save() of ObjectLog
         */

        final CtClass<?> testClass = Utils.findClass("fr.inria.sample.TestClassWithLoop");
        assertFalse(testClass.getMethods()
                .stream()
                .anyMatch(method ->
                        method.getAnnotations()
                                .stream()
                                .anyMatch(ctAnnotation ->
                                        "org.junit.AfterClass".equals(ctAnnotation.getAnnotationType().getQualifiedName())
                                )
                ));

        TestFramework.get().generateAfterClassToSaveObservations(testClass, Collections.singletonList(Utils.findMethod(testClass, "test")));
        final CtMethod<?> afterClassMethod = testClass.getMethods()
                .stream()
                .filter(method ->
                        method.getAnnotations()
                                .stream()
                                .anyMatch(ctAnnotation ->
                                        "org.junit.AfterClass".equals(ctAnnotation.getAnnotationType().getQualifiedName())
                                )
                ).findFirst()
                .orElseThrow(() -> new AssertionError("Should have a value of a method with the org.junit.AfterClass annotation"));

        afterClassMethod.getBody().removeStatement(afterClassMethod.getBody().getLastStatement());
        assertTrue(afterClassMethod.getBody()
                .getStatements()
                .stream()
                .noneMatch(statement ->
                        statement.toString().endsWith("ObjectLog.save()")
                )
        );
        TestFramework.get().generateAfterClassToSaveObservations(testClass, Collections.singletonList(Utils.findMethod(testClass, "test")));
        assertTrue(afterClassMethod.getBody()
                .getStatements()
                .stream()
                .anyMatch(statement ->
                        statement.toString().endsWith("ObjectLog.save()")
                )
        );
    }
}
