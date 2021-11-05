package eu.stamp_project.dspot.amplifier;

import eu.stamp_project.dspot.AbstractTestOnSample;
import eu.stamp_project.dspot.amplifier.amplifiers.MethodDuplicationAmplifier;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/7/16
 */
public class TestMethodCallAdder extends AbstractTestOnSample {

    @Before
    @Override
    public void setUp() {
        super.setUp();
        UserInput configuration = new UserInput();
        configuration.setAbsolutePathToProjectRoot(new File("src/test/resources/sample/").getAbsolutePath());

        DSpotUtils.init(CommentEnum.Amplifier, "target/dspot/",
                configuration.getFullClassPathWithExtraDependencies(),
                "src/test/resources/sample/"
        );
    }

    @Test
    public void testMethodCallAddOnInvocationWithCast() {
        /*
            In case of the invocation has type cast(s), we need to remove them.
         */

        CtClass<Object> testClass = launcher.getFactory().Class().get("fr.inria.mutation.ClassUnderTestTest");

        MethodDuplicationAmplifier methodCallAdder = new MethodDuplicationAmplifier();
        methodCallAdder.reset(testClass);

        final CtMethod<?> originalMethod = testClass.getMethods().stream().filter(m -> "testWithCast".equals(m.getSimpleName())).findFirst().get();
        List<CtMethod> amplifiedMethods = methodCallAdder.amplify(originalMethod, 0).collect(Collectors.toList());
        System.out.println(amplifiedMethods);
    }

    @Test
    public void testMethodCallAddAll() throws Exception {

        /*
            Test that we reuse method call in a test for each used method in the test.
                3 method are called in the original test, we produce 3 test methods.
         */

        CtClass<Object> testClass = launcher.getFactory().Class().get("fr.inria.mutation.ClassUnderTestTest");

        MethodDuplicationAmplifier methodCallAdder = new MethodDuplicationAmplifier();
        methodCallAdder.reset(testClass);

        final CtMethod<?> originalMethod = testClass.getMethods().stream().filter(m -> "testAddCall".equals(m.getSimpleName())).findFirst().get();
        List<CtMethod> amplifiedMethods = methodCallAdder.amplify(originalMethod, 0).collect(Collectors.toList());

        assertEquals(2, amplifiedMethods.size());

        for (int i = 0; i < amplifiedMethods.size(); i++) {
            CtMethod amplifiedMethod = amplifiedMethods.get(i);
            assertEquals(originalMethod.getBody().getStatements().size() + 1, amplifiedMethod.getBody().getStatements().size());
            CtStatement expectedStatement =originalMethod.getBody().getStatements().get(i + 1);//+1 to skip the construction statement.
            assertEquals( "// MethodDuplicationAmplifier: duplicated method call\n" + expectedStatement.toString(),
                    amplifiedMethod.getBody().getStatements().get(i + 1).toString());
            assertEquals(expectedStatement.toString(),
                    amplifiedMethod.getBody().getStatements().get(i + 2).toString());
        }
    }

    @Test
    public void testAddInIf() throws Exception {
        CtClass<Object> testClass = launcher.getFactory().Class().get("fr.inria.mutation.ClassUnderTestTest");
        MethodDuplicationAmplifier methodCallAdder = new MethodDuplicationAmplifier();
        methodCallAdder.reset(testClass);
        final CtMethod<?> originalMethod = findMethod(testClass, "testWithIf");
        final Stream<CtMethod<?>> amplify = methodCallAdder.amplify(originalMethod, 0);
        assertEquals(3, amplify.findFirst()
                .get()
                .getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
                                 @Override
                                 public boolean matches(CtInvocation<?> element) {
                                     return element.getExecutable().getSimpleName().equals("getBoolean");
                                 }
                             }
                ).size()
        );
    }
}
