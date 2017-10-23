package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.utils.AmplificationChecker;
import fr.inria.diversify.utils.AmplificationHelper;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 12/02/16
 * Time: 10:31
 */
public class AssertGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertGenerator.class);

    private InputConfiguration configuration;

    private DSpotCompiler compiler;

    public AssertGenerator(InputConfiguration configuration, DSpotCompiler compiler) {
        this.configuration = configuration;
        this.compiler = compiler;
    }

    public List<CtMethod<?>> generateAsserts(CtType<?> testClass) throws IOException, ClassNotFoundException {
        return generateAsserts(testClass, new ArrayList<>(testClass.getMethods()));
    }

    static final int[] counter = new int[]{0};

    private CtMethod<?> removeAssertion(CtMethod<?> test) {
        CtMethod<?> testWithoutAssertion = AmplificationHelper.cloneMethodTest(test, "");
        testWithoutAssertion.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                return AmplificationChecker.isAssert(element);
            }
        }).forEach(ctInvocation -> {
            ctInvocation.getArguments().forEach(argument -> {
                CtExpression clone = ((CtExpression) argument).clone();
                if (clone instanceof CtStatement) {
                    ctInvocation.insertBefore((CtStatement) clone);
                } else if (! (clone instanceof CtLiteral || clone instanceof CtVariableRead)) {
                    CtTypeReference typeOfParameter = clone.getType();
                    if (clone.getType().equals(test.getFactory().Type().NULL_TYPE)) {
                        typeOfParameter = test.getFactory().Type().createReference(Object.class);
                    }
                    final CtLocalVariable localVariable = test.getFactory().createLocalVariable(
                            typeOfParameter,
                            typeOfParameter.getSimpleName() + "_" + counter[0]++,
                            clone
                    );
                    ctInvocation.insertBefore(localVariable);
                }
            });
            ctInvocation.getParent(CtBlock.class).removeStatement(ctInvocation);
        });
        return testWithoutAssertion;
    }

    public List<CtMethod<?>> generateAsserts(CtType<?> testClass, List<CtMethod<?>> tests) throws IOException, ClassNotFoundException {
        CtType cloneClass = testClass.clone();
        cloneClass.setParent(testClass.getParent());
        List<CtMethod<?>> testWithoutAssertions = tests.stream()
                .map(this::removeAssertion)
                .collect(Collectors.toList());
        testWithoutAssertions.forEach(cloneClass::addMethod);
        MethodsAssertGenerator ags = new MethodsAssertGenerator(testClass, this.configuration, compiler);
        final List<CtMethod<?>> amplifiedTestWithAssertion =
                ags.generateAsserts(cloneClass, testWithoutAssertions);
        LOGGER.debug("{} new tests with assertions generated", amplifiedTestWithAssertion.size());
        return amplifiedTestWithAssertion;
    }
}
