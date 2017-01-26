package fr.inria.diversify.dspot;

import fr.inria.diversify.processor.test.TestLogProcessor;
import fr.inria.diversify.profiling.processor.test.AssertionRemover;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.util.Collection;
import java.util.Collections;

/**
 * User: Simon
 * Date: 23/02/16
 * Time: 11:45
 */
public class ClassWithLoggerBuilder {

    private final AssertionRemover assertionRemoverProcessor;
    private final TestLogProcessor loggingProcessor;


    public ClassWithLoggerBuilder(InputProgram inputProgram) {
        String logger = "fr.inria.diversify.logger";

        assertionRemoverProcessor = new AssertionRemover(inputProgram.getAbsoluteTestSourceCodeDir());
        assertionRemoverProcessor.setFactory(inputProgram.getFactory());
        loggingProcessor = new TestLogProcessor();
        loggingProcessor.setLogger(logger + ".Logger");
        loggingProcessor.setFactory(inputProgram.getFactory());
    }

    public ClassWithLoggerBuilder(Factory factory, String absoluteTestSourceCodeDir) {
        String logger = "fr.inria.diversify.logger";

        assertionRemoverProcessor = new AssertionRemover(absoluteTestSourceCodeDir);
        assertionRemoverProcessor.setFactory(factory);
        loggingProcessor = new TestLogProcessor();
        loggingProcessor.setLogger(logger + ".Logger");
        loggingProcessor.setFactory(factory);
    }

    public CtType buildClassWithLogger(CtType originalClass, Collection<CtMethod> tests) {
        CtType cloneClass = originalClass.clone();
        originalClass.getPackage().addType(cloneClass);
//        tests.forEach(cloneClass::removeMethod);
        tests.forEach(test -> cloneClass.addMethod(buildMethodWithLogger(cloneClass, test)));
        return cloneClass;
    }

    private CtMethod buildMethodWithLogger(CtType parentClass, CtMethod method) {
        CtMethod clone = cloneMethod(method);
        clone.setParent(parentClass);

        if(assertionRemoverProcessor != null) {
            assertionRemoverProcessor.process(clone);
        }
        loggingProcessor.process(clone);

        return clone;
    }


    private CtMethod cloneMethod(CtMethod method) {
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
