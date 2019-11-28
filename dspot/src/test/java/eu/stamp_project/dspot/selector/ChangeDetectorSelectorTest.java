package eu.stamp_project.dspot.selector;

import eu.stamp_project.UtilsModifier;
import eu.stamp_project.dspot.common.AmplificationHelper;
import spoon.reflect.declaration.CtMethod;

import java.io.File;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 10/08/17
 */
@SuppressWarnings("unchecked")
public class ChangeDetectorSelectorTest extends AbstractSelectorTest {

    @Override
    protected TestSelector getTestSelector() {
        this.configuration.setAbsolutePathToSecondVersionProjectRoot(new File("src/test/resources/regression/test-projects_1/").getAbsolutePath() + "/");
        return new ChangeDetectorSelector(this.builder, this.configuration);
    }

    @Override
    protected CtMethod<?> getAmplifiedTest() {
        final CtMethod clone = getTest().clone();
        UtilsModifier.replaceGivenLiteralByNewValue(this.factory, clone, -1);
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
}
