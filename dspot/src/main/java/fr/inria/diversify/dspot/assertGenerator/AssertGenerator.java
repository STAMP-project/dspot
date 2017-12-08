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

    private AssertionRemover assertionRemover;

    public AssertGenerator(InputConfiguration configuration, DSpotCompiler compiler) {
        this.configuration = configuration;
        this.compiler = compiler;
        this.assertionRemover = new AssertionRemover();
    }

    public List<CtMethod<?>> generateAsserts(CtType<?> testClass) throws IOException, ClassNotFoundException {
        return generateAsserts(testClass, new ArrayList<>(testClass.getMethods()));
    }

    public List<CtMethod<?>> generateAsserts(CtType<?> testClass, List<CtMethod<?>> tests) throws IOException, ClassNotFoundException {
        CtType cloneClass = testClass.clone();
        cloneClass.setParent(testClass.getParent());
        List<CtMethod<?>> testWithoutAssertions = tests.stream()
                .map(this.assertionRemover::removeAssertion)
                .collect(Collectors.toList());
        testWithoutAssertions.forEach(cloneClass::addMethod);
        MethodsAssertGenerator ags = new MethodsAssertGenerator(testClass, this.configuration, compiler);
        final List<CtMethod<?>> amplifiedTestWithAssertion =
                ags.generateAsserts(cloneClass, testWithoutAssertions);
        if (amplifiedTestWithAssertion.isEmpty()) {
            LOGGER.info("Could not generate any test with assertions");
        } else {
            LOGGER.info("{} new tests with assertions generated", amplifiedTestWithAssertion.size());
        }
        return amplifiedTestWithAssertion;
    }
}
