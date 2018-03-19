package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.utils.compilation.DSpotCompiler;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

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

    /**
     * Adds new assertions to all methods of a test class.
     *
     * <p>See {@link #generateAsserts(CtType, List)}.
     *
     * @param testClass Test class
     * @return New amplified tests
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Deprecated
    public List<CtMethod<?>> generateAsserts(CtType<?> testClass) throws IOException, ClassNotFoundException {
        return generateAsserts(testClass, new ArrayList<>(testClass.getMethods()));
    }

    /**
     * Adds new assertions in multiple tests.
     *
     * <p>Details of the assertions generation in {@link MethodsAssertGenerator#generateAsserts(CtType, List)}.
     *
     * @param testClass Test class
     * @param tests Test methods to amplify
     * @return New amplified tests
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public List<CtMethod<?>> generateAsserts(CtType<?> testClass, List<CtMethod<?>> tests) throws IOException, ClassNotFoundException {
        if (tests.isEmpty()) {
            return tests;
        }
        CtType cloneClass = testClass.clone();
        cloneClass.setParent(testClass.getParent());
        List<CtMethod<?>> testsWithoutAssertions = tests.stream()
                .map(this.assertionRemover::removeAssertion)
                .collect(Collectors.toList());
        testsWithoutAssertions.forEach(cloneClass::addMethod);
        MethodsAssertGenerator ags = new MethodsAssertGenerator(testClass, this.configuration, compiler);
        final List<CtMethod<?>> amplifiedTestsWithAssertions =
                ags.generateAsserts(cloneClass, testsWithoutAssertions);
        if (amplifiedTestsWithAssertions.isEmpty()) {
            LOGGER.info("Could not generate any test with assertions");
        } else {
            LOGGER.info("{} new tests with assertions generated", amplifiedTestsWithAssertions.size());
        }
        return amplifiedTestsWithAssertions;
    }
}
