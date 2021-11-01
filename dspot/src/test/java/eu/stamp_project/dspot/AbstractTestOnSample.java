package eu.stamp_project.dspot;

import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.AssertionGeneratorUtils;
import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.dspot.common.miscellaneous.CloneHelper;
import eu.stamp_project.dspot.common.configuration.DSpotCache;
import eu.stamp_project.dspot.amplifier.amplifiers.utils.RandomHelper;
import eu.stamp_project.dspot.common.execution.TestRunner;
import org.junit.Before;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/10/19
 */
public class AbstractTestOnSample {

    protected Launcher launcher;

    protected TestRunner testRunner;

    protected CtMethod<?> findMethod(CtClass<?> ctClass, String methodName) {
        return ctClass.getMethodsByName(methodName).get(0);
    }

    protected CtMethod<?> findMethod(String className, String methodName) {
        return findClass(className).getMethodsByName(methodName).get(0);
    }

    protected CtClass<?> findClass(String className) {
        return launcher.getFactory().Class().get(className);
    }

    @Before
    public void setUp() {
        launcher = new Launcher();
        launcher.addInputResource(getPathToProjectRoot() + "src/");
        launcher.getEnvironment().setNoClasspath(true);
        launcher.buildModel();
        AssertionGeneratorUtils.init(false);
        CloneHelper.init(false);
        TestFramework.init(launcher.getFactory());
        DSpotCache.init(10000);
        RandomHelper.setSeedRandom(72L);
        DSpotUtils.init(CommentEnum.None, null,null,null);
        this.testRunner = new TestRunner(getPathToProjectRoot(), "", false);
    }

    public String getPathToProjectRoot() {
        return "src/test/resources/sample/";
    }

}
