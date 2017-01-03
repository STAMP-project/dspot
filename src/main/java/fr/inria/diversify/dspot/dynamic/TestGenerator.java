package fr.inria.diversify.dspot.dynamic;

import fr.inria.diversify.dspot.value.MethodCall;
import fr.inria.diversify.dspot.value.MethodCallReader;
import fr.inria.diversify.dspot.value.ValueFactory;
import fr.inria.diversify.log.LogReader;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.testRunner.TestRunner;
import fr.inria.diversify.testRunner.JunitResult;
import fr.inria.diversify.util.Log;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * User: Simon
 * Date: 23/03/16
 * Time: 15:36
 */
public class TestGenerator {
    protected TestRunner testRunner;
    protected TestClassMinimisation testClassMinimisation;
    protected ValueFactory valueFactory;

    protected InputProgram inputProgram;
    protected Map<CtType, CtType> testClasses;

    public TestGenerator(InputProgram inputProgram, TestRunner testRunner, ValueFactory valueFactory, TestClassMinimisation testClassMinimisation) {
        this.testRunner = testRunner;
        this.valueFactory = valueFactory;
        this.inputProgram = inputProgram;
        this.testClassMinimisation = testClassMinimisation;
        this.testClasses = new HashMap<>();
    }

    public Collection<CtType> generateTestClasses(String logDir) throws IOException {
        LogReader logReader = new LogReader(logDir);
        MethodCallReader reader = new MethodCallReader(inputProgram.getFactory(), valueFactory);
        logReader.addParser(reader);
        logReader.readLogs();

        Map<CtMethod, List<MethodCall>> methodCalls = reader.getResult().stream()
                .collect(Collectors.groupingBy(mc -> mc.getMethod()));

        int maxSize = 50;
        Random r = new Random();
        methodCalls.values().stream()
                .forEach(set -> {
                    if(set.size() < maxSize) {
                        set.stream()
                                .forEach(mc -> generateTest(mc));
                    } else {
                        IntStream.range(0, maxSize)
                                .mapToObj(i -> set.remove(r.nextInt(set.size())))
                                .forEach(mc -> generateTest(mc));
                    }
                });

        List<CtType> tests = getTestClasses();

        Log.debug("number of tests before minimisation: {}", tests.stream().mapToInt(test -> test.getMethods().size()).sum());
        return tests.stream()
                .map(test -> testClassMinimisation.minimiseTests(test))
                .filter(test -> test != null)
                .filter(test -> !test.getMethods().isEmpty())
                .collect(Collectors.toList());
    }

    protected List<CtType>  getTestClasses() {
        return testClasses.values().stream()
                .filter(testClass -> !testClass.getMethods().isEmpty())
                .map(testClass -> {
                    CtType cl = inputProgram.getFactory().Core().clone(testClass);
                    cl.setParent(testClass.getParent());
                    return cl;
                })
                .peek(testClass -> {
                    try {
                        JunitResult result = testRunner.runTests(testClass, testClass.getMethods());
                        Set<CtMethod> tests = new HashSet<CtMethod>(testClass.getMethods());
                                tests.stream()
                                .filter(test -> result.compileOrTimeOutTestName().contains(test.getSimpleName()))
                                .forEach(test -> {
                                    testClass.removeMethod(test);
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .filter(testClass -> !testClass.getMethods().isEmpty())
                .collect(Collectors.toList());
    }
    static int count;
    static int tryTest;
    protected void generateTest(MethodCall methodCall) {
        tryTest++;
        TestMethodGenerator testMethodGenerator = new TestMethodGenerator(inputProgram.getFactory(), valueFactory);
        CtMethod method = methodCall.getMethod();
        CtType declaringClass = method.getDeclaringType();

        if(!testClasses.containsKey(declaringClass)) {
            testClasses.put(declaringClass, generateNewTestClass(declaringClass));
        }
        CtType testClass = testClasses.get(declaringClass);
        try {
            boolean result = false;
            if(!isPrivate(method)) {
                result = testMethodGenerator.generateTestFromInvocation(methodCall, testClass);
            }
            if(!result && !containsThis(method) && !containsReferenceToPrivatElement(method) && containsCall(methodCall.getMethod(), "org.grobid")) {
                result = testMethodGenerator.generateTestFromBody(methodCall, testClass);
            }
             if(result) {
                 count++;
             }
        } catch (Exception e) {
            e.printStackTrace();
            Log.debug("");
        }
        Log.debug("test count: {}/{}", count, tryTest);
    }

    protected CtType generateNewTestClass(CtType classToTest) {
        CtType test = inputProgram.getFactory().Class().create(classToTest.getPackage(), classToTest.getSimpleName()+"Test");
        Set<ModifierKind> modifierKinds = new HashSet<>(test.getModifiers());
        modifierKinds.add(ModifierKind.PUBLIC);
        test.setModifiers(modifierKinds);

        return test;
    }

    protected boolean isPrivate(CtMethod method) {
        return method.getModifiers().contains(ModifierKind.PRIVATE);
    }

    protected boolean containsThis(CtMethod method) {
        return !Query.getElements(method, new TypeFilter(CtThisAccess.class)).isEmpty();
    }

    protected boolean containsReferenceToPrivatElement(CtMethod method) {
        return Query.getElements(method.getBody(), new TypeFilter<CtModifiable>(CtModifiable.class)).stream()
                .anyMatch(modifiable -> modifiable.hasModifier(ModifierKind.PRIVATE));
    }

    protected boolean containsCall(CtMethod method, String filter) {
        List<CtInvocation> calls = Query.getElements(method, new TypeFilter(CtInvocation.class));
        return calls.stream()
                .map(call -> call.getType())
                .anyMatch(type -> type.getQualifiedName().startsWith(filter));
    }
}
