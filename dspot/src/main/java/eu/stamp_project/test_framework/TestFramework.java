package eu.stamp_project.test_framework;

import eu.stamp_project.test_framework.junit.JUnit3Support;
import eu.stamp_project.test_framework.junit.JUnit4Support;
import eu.stamp_project.test_framework.junit.JUnit5Support;
import eu.stamp_project.utils.program.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 09/11/18
 * <p>
 * Singleton and Starting point of Chain of responsibility
 */
public class TestFramework implements TestFrameworkSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestFramework.class);

    private static final TestFramework _instance = new TestFramework();

    private List<TestFrameworkSupport> testFrameworkSupportList;

    public static TestFramework get() {
        return _instance;
    }

    private TestFramework() {
        this.testFrameworkSupportList = new ArrayList<>();
        this.testFrameworkSupportList.add(new JUnit3Support());
        this.testFrameworkSupportList.add(new JUnit4Support());
        this.testFrameworkSupportList.add(new JUnit5Support());
    }

    @Override
    public boolean isAssert(CtInvocation<?> invocation) {
        for (TestFrameworkSupport testFrameworkSupport : this.testFrameworkSupportList) {
            if (testFrameworkSupport.isAssert(invocation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAssert(CtStatement candidate) {
        if (candidate instanceof CtInvocation) {
            return this.isAssert(((CtInvocation<?>) candidate));
        } else {
            return false;
        }
    }

    @Override
    public boolean isInAssert(CtElement candidate) {
        if (candidate.getParent(CtInvocation.class) != null) {
            return isAssert(candidate.getParent(CtInvocation.class));
        } else {
            return false;
        }
    }

    @Override
    public boolean isTest(CtMethod<?> candidate) {
        for (TestFrameworkSupport testFrameworkSupport : this.testFrameworkSupportList) {
            if (testFrameworkSupport.isTest(candidate)) {
                return true;
            }
        }
        return false;
        /*
        LOGGER.error("Could not find any test framework support for {}",

                (candidate.getParent(CtType.class) != null ?
                        candidate.getParent(CtType.class).getQualifiedName() + "#" : "")
                        + candidate.getSimpleName());
        LOGGER.error("Current supported test framework are:");
        LOGGER.error(this.testFrameworkSupportList.stream().map(Object::toString).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR)));
        throw new UnsupportedTestFrameworkException(candidate.toString());
        */
    }

    @Override
    public CtInvocation<?> buildInvocationToAssertion() {
        return null;
    }

    /**
     * @return the stream of all test classes of the test suite.
     * We consider a class as test class if at least one of its method match {@link TestFramework#isTest(CtMethod)}
     */
    public static Stream<CtType<?>> getAllTestClassesAsStream() {
        return InputConfiguration.get()
                .getFactory()
                .Type()
                .getAll()
                .stream()
                .filter(ctType ->
                        ctType.getMethods()
                                .stream()
                                .anyMatch(TestFramework.get()::isTest)
                );
    }

    /**
     * @return the list of all test classes of the test suite.
     * We consider a class as test class if at least one of its method match {@link TestFramework#isTest(CtMethod)}
     */
    public static List<CtType<?>> getAllTestClasses() {
        return TestFramework.getAllTestClassesAsStream().collect(Collectors.toList());
    }

    /**
     * @return the qualified name's array of all test classes of the test suite.
     * We consider a class as test class if at least one of its method match {@link TestFramework#isTest(CtMethod)}
     */
    public static String[] getAllTestClassesName() {
        return TestFramework.getAllTestClassesAsStream().toArray(String[]::new);
    }

    public static final TypeFilter<CtInvocation<?>> ASSERTIONS_FILTER = new TypeFilter<CtInvocation<?>>(CtInvocation.class) {
        @Override
        public boolean matches(CtInvocation<?> element) {
            return TestFramework.get().isAssert(element);
        }
    };
}
