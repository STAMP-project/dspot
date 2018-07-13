package eu.stamp_project.minimization;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.program.InputConfiguration;
import eu.stamp_project.testrunner.runner.test.Failure;
import eu.stamp_project.utils.AmplificationChecker;
import org.junit.Ignore;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/03/18
 */
public class ChangeMinimizerTest extends AbstractTest {

    @Override
    public String getPathToPropertiesFile() {
        return "src/test/resources/regression/test-projects_0/test-projects.properties";
    }

    @Ignore
    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {

        /*
            ChangeMinimizer keeps only the assertions that trigger the failure on the second version
         */
        final CtClass testClass = Utils.findClass("example.TestSuiteExample");
        final String configurationPath = Utils.getInputConfiguration().getConfigPath();
        InputConfiguration inputConfiguration = InputConfiguration.initialize(configurationPath);
        final HashMap<CtMethod<?>, Failure> failurePerAmplifiedTest = new HashMap<>();
        final CtMethod<?> test2 = Utils.findMethod(testClass, "test2");
        failurePerAmplifiedTest.put(test2,
                new Failure("test2", testClass.getQualifiedName(), new StringIndexOutOfBoundsException(-1))
        );

        final ChangeMinimizer changeMinimizer = new ChangeMinimizer(
                testClass,
                inputConfiguration,
                failurePerAmplifiedTest
        );
        final CtInvocation assertion = test2.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return AmplificationChecker.isAssert(element);
            }
        }).get(0);
        final CtInvocation clone = assertion.clone();
        clone.getElements(new TypeFilter<CtLiteral>(CtLiteral.class) {
            @Override
            public boolean matches(CtLiteral element) {
                return element.getValue() instanceof Integer && super.matches(element);
            }
        }).get(0).setValue(-1);
        test2.getBody().insertEnd(clone);

        assertEquals(3, test2.getBody().getStatements().size());
        final CtMethod<?> minimize = changeMinimizer.minimize(test2);
        System.out.println(minimize);
        assertEquals(2, minimize.getBody().getStatements().size());
    }
}
