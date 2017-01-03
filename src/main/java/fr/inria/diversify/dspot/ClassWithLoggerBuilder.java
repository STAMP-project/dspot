package fr.inria.diversify.dspot;

import fr.inria.diversify.processor.test.TestLogProcessor;
import fr.inria.diversify.profiling.processor.test.AssertionRemover;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * User: Simon
 * Date: 23/02/16
 * Time: 11:45
 */
public class ClassWithLoggerBuilder {
    protected final String logger;
    protected AssertionRemover assertionRemoverProcessor;
    protected TestLogProcessor loggingProcessor;


    public ClassWithLoggerBuilder(InputProgram inputProgram) {
        this.logger = "fr.inria.diversify.logger";
        assertionRemoverProcessor = new AssertionRemover(inputProgram.getAbsoluteTestSourceCodeDir());
        assertionRemoverProcessor.setLogger(logger + ".Logger");
        assertionRemoverProcessor.setFactory(inputProgram.getFactory());

        loggingProcessor = new TestLogProcessor();
        loggingProcessor.setLogger(logger + ".Logger");
        loggingProcessor.setFactory(inputProgram.getFactory());
    }

    public ClassWithLoggerBuilder(Factory factory) {
        this.logger = "fr.inria.diversify.logger";

        loggingProcessor = new TestLogProcessor();
        loggingProcessor.setLogger(logger + ".Logger");
        loggingProcessor.setFactory(factory);
    }

    protected CtType buildClassWithLogger(CtType originalClass, CtMethod test) {
        return buildClassWithLogger(originalClass, Collections.singletonList(test));
    }

    public CtType buildClassWithLogger(CtType originalClass, Collection<CtMethod> tests) {
        CtType cloneClass = originalClass.clone();
        cloneClass.setParent(originalClass.getParent());
        tests.forEach(cloneClass::removeMethod);
        tests.forEach(test -> cloneClass.addMethod(buildMethodWithLogger(cloneClass, test)));
//        methodsTestInstrumented
//                .forEach(testWithLogger -> {
//                    cloneClass.removeMethod(testWithLogger);
//                    cloneClass.addMethod(testWithLogger);
//                });
        return cloneClass;
    }

    protected CtMethod buildMethodWithLogger(CtType parentClass, CtMethod method) {
        CtMethod clone = cloneMethod(method);
        clone.setParent(parentClass);

        if(assertionRemoverProcessor != null) {
            assertionRemoverProcessor.process(clone);
        }
        loggingProcessor.process(clone);

        return clone;
    }


    protected CtMethod cloneMethod(CtMethod method) {
        CtMethod cloned_method = method.getFactory().Core().clone(method);

        CtAnnotation toRemove = cloned_method.getAnnotations().stream()
                .filter(annotation -> annotation.toString().contains("Override"))
                .findFirst().orElse(null);

        if(toRemove != null) {
            cloned_method.removeAnnotation(toRemove);
        }
        return cloned_method;
    }
}
