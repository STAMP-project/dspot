package eu.stamp_project.dspot.selector;

import eu.stamp_project.Utils;
import eu.stamp_project.automaticbuilder.maven.DSpotPOMCreator;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.utils.AmplificationHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import java.io.File;

import static org.junit.Assert.assertFalse;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/08/17
 */
@SuppressWarnings("unchecked")
public class ChangeDetectorSelectorTest extends AbstractSelectorTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        DSpotPOMCreator.createNewPom();
    }

    @Override
    protected TestSelector getTestSelector() {
        return new ChangeDetectorSelector();
    }

    @Override
    protected CtMethod<?> getAmplifiedTest() {
        final CtMethod clone = getTest().clone();
        Utils.replaceGivenLiteralByNewValue(clone, -1);
        return clone;
    }

    @Override
    protected String getContentReportFile() {
        return "1 amplified test fails on the new versions." + AmplificationHelper.LINE_SEPARATOR +
                "test2(example.TestSuiteExample): String index out of range: -1java.lang.StringIndexOutOfBoundsException: String index out of range: -1" + AmplificationHelper.LINE_SEPARATOR +
                "\tat java.lang.String.charAt(String.java:658)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat example.Example.charAt(Example.java:16)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat example.TestSuiteExample.test2(TestSuiteExample.java:8)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat java.lang.reflect.Method.invoke(Method.java:498)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:44)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner.run(ParentRunner.java:309)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.Suite.runChild(Suite.java:127)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.Suite.runChild(Suite.java:26)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat org.junit.runners.ParentRunner.run(ParentRunner.java:309)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat eu.stamp_project.testrunner.runner.JUnit4Runner.run(JUnit4Runner.java:63)" + AmplificationHelper.LINE_SEPARATOR +
                "\tat eu.stamp_project.testrunner.runner.JUnit4Runner.main(JUnit4Runner.java:27)" + AmplificationHelper.LINE_SEPARATOR;
    }

    @Test
    public void testOnMultiModuleProject() throws Exception {

        Utils.getInputConfiguration().setVerbose(true);
        EntryPoint.verbose = true;
        DSpotPOMCreator.createNewPom();

		/*
            Test that we can use the Change Detector on a multi module project
				The amplification is still done on one single module.
				DSpot should be able to return an amplified test that catch changes.
		 */

        try {
            FileUtils.forceDelete(new File("target/dspot/"));
        } catch (Exception ignored) {

        }

        final String configurationPath = "src/test/resources/multiple-pom/deep-pom-modules.properties";
        Utils.init(configurationPath);
        final ChangeDetectorSelector changeDetectorSelector = new ChangeDetectorSelector();
        changeDetectorSelector.init(Utils.getInputConfiguration());
        assertFalse(changeDetectorSelector.selectToKeep(changeDetectorSelector.selectToAmplify(
                Utils.findClass("fr.inria.multiple.pom.HelloWorldTest"), Utils.getAllTestMethodsFrom("fr.inria.multiple.pom.HelloWorldTest"))
        ).isEmpty());

        Utils.getInputConfiguration().setVerbose(false);
    }


}
