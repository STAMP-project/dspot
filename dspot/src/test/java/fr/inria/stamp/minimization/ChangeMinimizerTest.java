package fr.inria.stamp.minimization;

import fr.inria.diversify.Utils;
import fr.inria.diversify.dspot.AbstractTest;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.DSpotUtils;
import fr.inria.diversify.utils.Initializer;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import org.junit.Test;
import org.junit.runner.notification.Failure;
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

    @Test
    public void test() throws Exception {

        /*

         */

        final CtClass testClass = Utils.findClass("example.TestSuiteExample");
        final String configurationPath = Utils.getInputConfiguration().getProperty("configPath");
        final String pathToFolder = Utils.getInputConfiguration().getProperty("folderPath");
        InputConfiguration inputConfiguration = new InputConfiguration(configurationPath );
        InputProgram inputProgram = InputConfiguration.initInputProgram(inputConfiguration);
        inputConfiguration.setInputProgram(inputProgram);
        String pathToChangedVersionOfProgram = pathToFolder +
                DSpotUtils.shouldAddSeparator.apply(pathToFolder) +
                (inputConfiguration.getProperty("targetModule") != null ?
                        inputConfiguration.getProperty("targetModule") +
                                DSpotUtils.shouldAddSeparator.apply(pathToFolder)
                        : "");
        inputProgram.setProgramDir(pathToChangedVersionOfProgram);
        Initializer.initialize(Utils.getInputConfiguration(), inputProgram);
        final HashMap<CtMethod<?>, Failure> failurePerAmplifiedTest = new HashMap<>();
        final ChangeMinimizer changeMinimizer = new ChangeMinimizer(
                testClass,
                inputConfiguration,
                inputProgram,
                pathToChangedVersionOfProgram,
                failurePerAmplifiedTest
        );

        final CtMethod<?> test2 = Utils.findMethod(testClass, "test2");
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
        assertEquals(2, minimize.getBody().getStatements().size());
    }
}