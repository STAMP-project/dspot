package eu.stamp_project.dspot.common.test_framework.implementations.junit;

import junit.framework.Test;
import junit.framework.TestSuite;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class JUnit3Support extends JUnitSupport {

    public JUnit3Support() {
        super("junit.framework.TestCase", "junit.framework.Assert");
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationAfterClass() {
        return "";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationTest() {
        return "";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationIgnore() {
        return "";
    }

    /*
        For JUnit3, a test method starts by test, otherwise we consider ignored
        Test suites cannot be ignore at class level
     */
    @Override
    public boolean isIgnored(CtElement candidate) {
        return candidate instanceof CtMethod?!((CtMethod<?>)candidate).getSimpleName().startsWith("test"):false;
    }

    /*
        For JUnit3, a test method starts by test.
     */
    @Override
    protected boolean isATest(CtMethod<?> candidate) {
        // check that the current test class inherit from TestCase
        final CtType<?> testClass = candidate.getParent(CtType.class);
        if (testClass == null) {
            return false;
        }
        return matchOneSuperClassToAssertClass(testClass.getReference()) &&
                //candidate.getAnnotations().isEmpty() && TODO checks if needed
                candidate.getSimpleName().startsWith("test");
    }

    private boolean matchOneSuperClassToAssertClass(CtTypeReference<?> currentTestClass) {
        if (currentTestClass.getSuperclass() == null) {
            return false;
        }
        return currentTestClass.getQualifiedName().equals(this.qualifiedNameOfAssertClass) ||
                matchOneSuperClassToAssertClass(currentTestClass.getSuperclass());
    }

    @Override
    public void generateAfterClassToSaveObservations(CtType<?> testClass, List<CtMethod<?>> testsToRun) {
        final Factory factory = testClass.getFactory();
        final CtMethod<?> suiteMethod = factory.createMethod();
        suiteMethod.setModifiers(
                new HashSet<>(Arrays.asList(ModifierKind.PUBLIC, ModifierKind.STATIC))
        );
        suiteMethod.setSimpleName("suite");
        suiteMethod.setType(
                factory.createCtTypeReference(Test.class)
        );

        // init TestSetup class
        final CtClass<?> testSetupClass = factory.Class().create("junit.extensions.TestSetup");

        CtParameter parameter = factory.createParameter();
        parameter.setType(factory.Type().createReference(Class.class));
        parameter.setSimpleName("test");

        testSetupClass.addConstructor(
                factory.createConstructor(
                        testSetupClass,
                        Collections.emptySet(),
                        Collections.singletonList(parameter),
                        Collections.emptySet()
                )
        );
        final CtExpression parameterValue = factory.createCodeSnippetExpression(testClass.getQualifiedName() + ".class");
        parameterValue.setType(factory.Type().createReference(Class.class));

        final CtConstructorCall constructorCall = factory.createConstructorCall(
                factory.createCtTypeReference(TestSuite.class), parameterValue
        );

        final CtNewClass testSetupNewClass = factory.Code().createNewClass(testSetupClass, parameterValue);
        //testSetupNewClass.addArgument(constructorCall);

        final CtReturn returnStatement = factory.createReturn();
        returnStatement.setReturnedExpression(testSetupNewClass);
        suiteMethod.setBody(returnStatement);

        // create tearDown method
        final CtMethod tearDown = factory.createMethod();
        tearDown.setModifiers(
                new HashSet<>(Collections.singletonList(ModifierKind.PROTECTED))
        );
        tearDown.setSimpleName("tearDown");
        tearDown.addThrownType(factory.createCtTypeReference(Exception.class));
        tearDown.setType(factory.Type().VOID_PRIMITIVE);
        createCallToSaveAndInsertAtTheEnd(factory, tearDown);
        // add tearDown method to the anonymous TestSetup Class
        testSetupNewClass.getAnonymousClass().addMethod(tearDown);

        testClass.addMethod(suiteMethod);
    }
}
