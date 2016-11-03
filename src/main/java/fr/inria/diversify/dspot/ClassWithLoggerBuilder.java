package fr.inria.diversify.dspot;

import fr.inria.diversify.processor.test.TestLogProcessor;
import fr.inria.diversify.profiling.processor.test.AssertionRemover;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        List<CtMethod> tests = new ArrayList<>(1);
        tests.add(test);
        return buildClassWithLogger(originalClass, tests);
    }

    public CtType buildClassWithLogger(CtType originalClass, Collection<CtMethod> tests) {
        CtType cloneClass = originalClass.getFactory().Core().clone(originalClass);
        cloneClass.setParent(originalClass.getParent());
        tests.stream()
                .map(test -> buildMethodWithLogger(cloneClass, test))
                .forEach(instruTest -> {
                    cloneClass.removeMethod(instruTest);
                    cloneClass.addMethod(instruTest);
                });
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
