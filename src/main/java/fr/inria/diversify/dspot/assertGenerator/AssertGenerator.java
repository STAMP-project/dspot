package fr.inria.diversify.dspot.assertGenerator;

import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.runner.InputProgram;
import fr.inria.diversify.util.Log;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 12/02/16
 * Time: 10:31
 */
public class AssertGenerator {

    private InputProgram inputProgram;

    private DSpotCompiler compiler;

    public AssertGenerator(InputProgram inputProgram, DSpotCompiler compiler) {
        this.inputProgram = inputProgram;
        this.compiler = compiler;
    }

    public List<CtMethod<?>> generateAsserts(CtType testClass) throws IOException, ClassNotFoundException {
        return generateAsserts(testClass, testClass.getMethods());
    }

    public List<CtMethod<?>> generateAsserts(CtType testClass, Collection<CtMethod<?>> tests) throws IOException, ClassNotFoundException {
        CtType cloneClass = testClass.clone();
        cloneClass.setParent(testClass.getParent());
        final Map<CtMethod<?>, List<Integer>> statementIndexToAssert = tests.stream()
                .collect(Collectors.toMap(Function.identity(), AssertGeneratorHelper::findStatementToAssert));
        MethodsAssertGenerator ags = new MethodsAssertGenerator(testClass, inputProgram, compiler);
        final List<CtMethod<?>> amplifiedTestWithAssertion = ags.generateAsserts(testClass, new ArrayList<>(tests), statementIndexToAssert);
        Log.debug("{} new tests with assertions generated", amplifiedTestWithAssertion.size());
        return amplifiedTestWithAssertion;
    }
}
