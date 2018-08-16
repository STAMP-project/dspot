package eu.stamp_project.dspot.assertgenerator;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.Utils;
import eu.stamp_project.dspot.amplifier.MethodGeneratorAmplifier;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public class AssertGeneratorHelperTest extends AbstractTest {

    @Test
    public void testContainsObjectReferences() throws Exception {
        assertFalse(AssertGeneratorHelper.containsObjectReferences("thaliana"));
        assertFalse(AssertGeneratorHelper.containsObjectReferences("thaliana.thaliana@"));
        assertFalse(AssertGeneratorHelper.containsObjectReferences("thaliana.thaliana$f465"));
        assertFalse(AssertGeneratorHelper.containsObjectReferences("thaliana.thaliana@z0545"));
        assertTrue(AssertGeneratorHelper.containsObjectReferences("thaliana@041a"));
        assertTrue(AssertGeneratorHelper.containsObjectReferences("thaliana.thaliana@041a"));
        assertTrue(AssertGeneratorHelper.containsObjectReferences(new Object().toString()));
        assertTrue(AssertGeneratorHelper.containsObjectReferences("Expected message : " + new Object().toString() + "not found"));
        assertTrue(AssertGeneratorHelper.containsObjectReferences("Expected message : " + new Object().toString()));
        assertTrue(AssertGeneratorHelper.containsObjectReferences(new Object().toString() + "not found"));
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

        AssertGeneratorHelper.addAfterClassMethod(testClass);
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
        AssertGeneratorHelper.addAfterClassMethod(testClass);
        assertTrue(afterClassMethod.getBody()
                .getStatements()
                .stream()
                .anyMatch(statement ->
                        statement.toString().endsWith("ObjectLog.save()")
                )
        );
    }

    @Test
    public void testOnLoops() throws Exception {

        /*
            Test the instrumentation on values used in loops
            For now, we do not log such values
            TODO implements the A-amplification on values inside loops
         */
        assertEquals("@org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                        "public void test2_withlog() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                        "    java.util.List<fr.inria.sample.TestClassWithLoop.MyClass> list = new java.util.ArrayList<>();" + AmplificationHelper.LINE_SEPARATOR +
                        "    boolean o_test2__3 = list.add(new fr.inria.sample.TestClassWithLoop.MyClass());" + AmplificationHelper.LINE_SEPARATOR +
                        "    eu.stamp_project.compare.ObjectLog.log(o_test2__3, \"o_test2__3\", \"test2__3\");" + AmplificationHelper.LINE_SEPARATOR +
                        "    boolean o_test2__5 = list.add(new fr.inria.sample.TestClassWithLoop.MyClass());" + AmplificationHelper.LINE_SEPARATOR +
                        "    eu.stamp_project.compare.ObjectLog.log(o_test2__5, \"o_test2__5\", \"test2__5\");" + AmplificationHelper.LINE_SEPARATOR +
                        "    boolean o_test2__7 = list.add(new fr.inria.sample.TestClassWithLoop.MyClass());" + AmplificationHelper.LINE_SEPARATOR +
                        "    eu.stamp_project.compare.ObjectLog.log(o_test2__7, \"o_test2__7\", \"test2__7\");" + AmplificationHelper.LINE_SEPARATOR +
                        "    for (fr.inria.sample.TestClassWithLoop.MyClass myClass : list) {" + AmplificationHelper.LINE_SEPARATOR +
                        "        myClass.getInteger();" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "    for (fr.inria.sample.TestClassWithLoop.MyClass myClass : list) {" + AmplificationHelper.LINE_SEPARATOR +
                        "        myClass.inc();" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "    for (fr.inria.sample.TestClassWithLoop.MyClass myClass : list) {" + AmplificationHelper.LINE_SEPARATOR +
                        "        myClass.getInteger();" + AmplificationHelper.LINE_SEPARATOR +
                        "    }" + AmplificationHelper.LINE_SEPARATOR +
                        "    eu.stamp_project.compare.ObjectLog.log(o_test2__3, \"o_test2__3\", \"test2__3___end\");" + AmplificationHelper.LINE_SEPARATOR +
                        "    eu.stamp_project.compare.ObjectLog.log(o_test2__5, \"o_test2__5\", \"test2__5___end\");" + AmplificationHelper.LINE_SEPARATOR +
                        "    eu.stamp_project.compare.ObjectLog.log(o_test2__7, \"o_test2__7\", \"test2__7___end\");" + AmplificationHelper.LINE_SEPARATOR +
                        "}",
                AssertGeneratorHelper.createTestWithLog(
                        new AssertionRemover().removeAssertion(Utils.findMethod("fr.inria.sample.TestClassWithLoop", "test2")),
                        "fr.inria.sample",
                        Collections.emptyList()).toString()
        );
    }


    @Test
    public void testNoInstrumentationOnGeneratedObject() throws Exception {

        /*
         * This test aims at verifying that dspot does not generate assertion for generated object.
         * To do this, it will check that the instrumentation does not add observation points on those objects.
         * If no observation point is added, any assertion would be generated.
         * We verify the number of ObjectLog.log statement inside the instrumented tests
         */

        final String packageName = "fr.inria.statementaddarray";
        final Factory factory = Utils.getFactory();
        MethodGeneratorAmplifier amplifier = new MethodGeneratorAmplifier();
        amplifier.reset(factory.Class().get(packageName + ".ClassTargetAmplify"));

        CtMethod<?> ctMethod = Utils.findMethod(factory.Class().get(packageName + ".TestClassTargetAmplify"), "test");
        List<CtMethod> amplifiedMethods = amplifier.amplify(ctMethod, 0).collect(Collectors.toList());

        assertEquals(4, amplifiedMethods.size());

        final List<CtMethod<?>> instrumentedAmplifiedTests = amplifiedMethods.stream()
                .map(method -> AssertGeneratorHelper.createTestWithLog(method, "fr.inria.statementaddarray", Collections.emptyList()))
                .collect(Collectors.toList());

        assertEquals(4, instrumentedAmplifiedTests.size());

        assertEquals(17,
                instrumentedAmplifiedTests.parallelStream()
                        .mapToInt(instrumentedAmplifiedTest ->
                                instrumentedAmplifiedTest.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
                                    @Override
                                    public boolean matches(CtInvocation element) {
                                        return "eu.stamp_project.compare.ObjectLog".equals(element.getTarget().toString()) &&
                                                "log".equals(element.getExecutable().getSimpleName());
                                    }
                                }).size()
                        ).sum()
        );
    }

    @Test
    public void testMultipleObservationsPoints() throws Exception {
        final CtMethod<?> test1 = Utils.findMethod("fr.inria.multipleobservations.TestClassToBeTest", "test");
        final CtMethod<?> testWithLog =
                AssertGeneratorHelper.createTestWithLog(test1, "fr.inria.multipleobservations", Collections.emptyList());
        final String expectedMethodWithLogs = "@org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "public void test_withlog() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "    final fr.inria.multipleobservations.ClassToBeTest classToBeTest = new fr.inria.multipleobservations.ClassToBeTest();" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(classToBeTest, \"classToBeTest\", \"test__1\");" + AmplificationHelper.LINE_SEPARATOR +
                "    classToBeTest.method();" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(classToBeTest, \"classToBeTest\", \"test__1___end\");" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMethodWithLogs, testWithLog.toString());
    }

    @Test
    public void testCreateTestWithLogClassTargetAmplify() throws Exception {
        final CtMethod<?> test1 = Utils.findMethod("fr.inria.statementaddarray.TestClassTargetAmplify", "test");
        final CtMethod<?> testWithLog =
                AssertGeneratorHelper.createTestWithLog(test1, "fr.inria.statementaddarray", Collections.emptyList());
        final String expectedMethod = "@org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "public void test_withlog() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "    fr.inria.statementaddarray.ClassTargetAmplify clazz = new fr.inria.statementaddarray.ClassTargetAmplify();" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(clazz, \"clazz\", \"test__1\");" + AmplificationHelper.LINE_SEPARATOR +
                "    fr.inria.statementaddarray.ClassParameterAmplify o_test__3 = clazz.methodWithReturn();" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(o_test__3, \"o_test__3\", \"test__3\");" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(clazz, \"clazz\", \"test__1___end\");" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMethod, testWithLog.toString());
    }

    @Test
    public void testCreateTestWithLog() throws Exception {
        /*
            test the creation of test with log
         */

        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        final CtMethod<?> test1 = (CtMethod<?>) testClass.getMethodsByName("test1").get(0);
        final CtMethod<?> testWithLog =
                AssertGeneratorHelper.createTestWithLog(test1, "fr.inria.sample", Collections.emptyList());

        final String expectedMethod = "@org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "public void test1_withlog() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(cl, \"cl\", \"test1__1\");" + AmplificationHelper.LINE_SEPARATOR +
                "    cl.getFalse();" + AmplificationHelper.LINE_SEPARATOR +
                "    cl.getBoolean();" + AmplificationHelper.LINE_SEPARATOR +
                "    java.io.File file = new java.io.File(\"\");" + AmplificationHelper.LINE_SEPARATOR +
                "    boolean var = cl.getTrue();" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(cl, \"cl\", \"test1__1___end\");" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMethod, testWithLog.toString());
    }

    @Test
    public void testCreateTestWithLogWithoutChainSameObservations() throws Exception {
        CtMethod test1 = Utils.findMethod("fr.inria.sample.TestClassWithSpecificCaseToBeAsserted", "test1");
        final CtMethod<?> testWithLog =
                AssertGeneratorHelper.createTestWithLog(test1, "fr.inria.sample", Collections.emptyList());

        final String expectedMethodWithLog = "@org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "public void test1_withlog() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "    int a = 0;" + AmplificationHelper.LINE_SEPARATOR +
                "    int b = 1;" + AmplificationHelper.LINE_SEPARATOR +
                "    int o_test1__3 = new java.util.Comparator<java.lang.Integer>() {" + AmplificationHelper.LINE_SEPARATOR +
                "        @java.lang.Override" + AmplificationHelper.LINE_SEPARATOR +
                "        public int compare(java.lang.Integer integer, java.lang.Integer t1) {" + AmplificationHelper.LINE_SEPARATOR +
                "            return integer - t1;" + AmplificationHelper.LINE_SEPARATOR +
                "        }" + AmplificationHelper.LINE_SEPARATOR +
                "    }.compare(a, b);" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(o_test1__3, \"o_test1__3\", \"test1__3\");" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMethodWithLog, testWithLog.toString());
    }

    @Test
    public void testCreateTestWithLogWithDuplicatedStatement() throws Exception {
        /*
            test the creation of log with duplicates statement
		 */
        CtClass testClass = Utils.findClass("fr.inria.sample.TestClassWithoutAssert");
        final CtMethod<?> test2 = (CtMethod<?>) testClass.getMethodsByName("test2").get(0);
        final CtMethod<?> testWithLog =
                AssertGeneratorHelper.createTestWithLog(test2, "fr.inria.sample", Collections.emptyList());

        final String expectedMethod = "@org.junit.Test(timeout = 10000)" + AmplificationHelper.LINE_SEPARATOR +
                "public void test2_withlog() throws java.lang.Exception {" + AmplificationHelper.LINE_SEPARATOR +
                "    fr.inria.sample.ClassWithBoolean cl = new fr.inria.sample.ClassWithBoolean();" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(cl, \"cl\", \"test2__1\");" + AmplificationHelper.LINE_SEPARATOR +
                "    cl.getFalse();" + AmplificationHelper.LINE_SEPARATOR +
                "    cl.getFalse();" + AmplificationHelper.LINE_SEPARATOR +
                "    cl.getFalse();" + AmplificationHelper.LINE_SEPARATOR +
                "    eu.stamp_project.compare.ObjectLog.log(cl, \"cl\", \"test2__1___end\");" + AmplificationHelper.LINE_SEPARATOR +
                "}";
        assertEquals(expectedMethod, testWithLog.toString());
    }

}
