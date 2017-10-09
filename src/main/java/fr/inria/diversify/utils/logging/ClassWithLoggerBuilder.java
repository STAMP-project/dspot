package fr.inria.diversify.utils.logging;

import fr.inria.diversify.utils.sosiefier.TestLogProcessor;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.util.List;

/**
 * User: Simon
 * Date: 23/02/16
 * Time: 11:45
 */
@Deprecated
public class ClassWithLoggerBuilder {

    private final AssertionRemover assertionRemoverProcessor;
    private final TestLogProcessor loggingProcessor;

    public ClassWithLoggerBuilder(Factory factory) {
        String logger = "fr.inria.diversify.logger";

        assertionRemoverProcessor = new AssertionRemover();
        assertionRemoverProcessor.setFactory(factory);
        loggingProcessor = new TestLogProcessor();
        loggingProcessor.setLogger(logger + ".Logger");
        loggingProcessor.setFactory(factory);
    }

    public CtType buildClassWithLogger(CtType originalClass, List<CtMethod<?>> tests) {
        CtType cloneClass = originalClass.clone();
        originalClass.getPackage().addType(cloneClass);
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
