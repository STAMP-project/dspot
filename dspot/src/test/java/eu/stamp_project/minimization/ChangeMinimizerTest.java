package eu.stamp_project.minimization;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.automaticbuilder.AutomaticBuilderFactory;
import eu.stamp_project.testrunner.runner.test.Failure;
import eu.stamp_project.utils.AmplificationChecker;
import eu.stamp_project.utils.DSpotUtils;
import eu.stamp_project.utils.Initializer;
import eu.stamp_project.utils.sosiefier.InputConfiguration;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
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

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {

        /*
            ChangeMinimizer keeps only the assertions that trigger the failure on the second version
         */
        final CtClass testClass = Utils.findClass("example.TestSuiteExample");
        final String configurationPath = Utils.getInputConfiguration().getProperty("configPath");
        final String pathToFolder = Utils.getInputConfiguration().getProperty("folderPath");
        InputConfiguration inputConfiguration = new InputConfiguration(configurationPath);
        String pathToChangedVersionOfProgram = DSpotUtils.shouldAddSeparator.apply(pathToFolder) +
                (inputConfiguration.getProperty("targetModule") != null ?
                        DSpotUtils.shouldAddSeparator.apply(inputConfiguration.getProperty("targetModule")) : "");
        Initializer.initialize(inputConfiguration);
        final HashMap<CtMethod<?>, Failure> failurePerAmplifiedTest = new HashMap<>();
        final CtMethod<?> test2 = Utils.findMethod(testClass, "test2");
        failurePerAmplifiedTest.put(test2,
                new Failure("test2", testClass.getQualifiedName(), new StringIndexOutOfBoundsException(-1))
        );

        InputConfiguration changedConfiguration = new InputConfiguration(configurationPath);
        changedConfiguration.setAbsolutePathToProjectRoot(new File(pathToChangedVersionOfProgram).getAbsolutePath());
        AutomaticBuilderFactory.reset();
        AutomaticBuilderFactory.getAutomaticBuilder(changedConfiguration).compile();

        final ChangeMinimizer changeMinimizer = new ChangeMinimizer(
                testClass,
                inputConfiguration,
                changedConfiguration,
                pathToChangedVersionOfProgram,
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
