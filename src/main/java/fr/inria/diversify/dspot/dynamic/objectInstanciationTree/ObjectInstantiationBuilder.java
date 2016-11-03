package fr.inria.diversify.dspot.dynamic.objectInstanciationTree;

import fr.inria.diversify.log.LogReader;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Simon
 * Date: 25/08/16
 * Time: 17:06
 */
public class ObjectInstantiationBuilder {
    protected InputProgram inputProgram;
    protected List<ObjectInstantiation> objectInstantiations;

    public ObjectInstantiationBuilder(InputProgram inputProgram) {
    this.inputProgram = inputProgram;
    }

    public void init(String logDir) throws IOException {
        Map<String, PrimitiveInstantiation> primitives;
        if(logDir != null) {
            LogReader logReader = new LogReader(logDir);
            PrimitiveForNewReader reader = new PrimitiveForNewReader(inputProgram);
            logReader.addParser(reader);
            logReader.readLogs();
            primitives = reader.getResult();
        } else {
            primitives = new HashMap<>();
        }
        objectInstantiations = buildObjectInstantiation().stream()
                .peek(oi -> oi.updatePrimitive(primitives))
                .filter(oi -> oi.isOk())
                .collect(Collectors.toList());
    }

    public CtExpression findConstructorCall(CtClass target, boolean withSubType) {
        if(withSubType) {
            CtTypeReference ref = target.getReference();
            return target.getFactory().Class().getAll(false).stream()
                    .filter(type -> type.getReference().isSubtypeOf(ref))
                    .filter(type -> type instanceof CtClass)
                    .map(cl -> findConstructorCall((CtClass) cl))
                    .filter(expression -> expression != null)
                    .findFirst()
                    .orElse(null);

        } else {
            return findConstructorCall(target);
        }
    }

    public CtExpression findConstructorCall(CtClass target) {
        CtExpression constructorCall = null;
        if(target != null && target.isTopLevel() && !target.getModifiers().contains(ModifierKind.ABSTRACT)) {
            String className = target.getQualifiedName();
            constructorCall = objectInstantiations.stream()
                    .filter(oi -> oi.getClassName().equals(className))
                    .map(oi -> oi.getValue())
                    .findFirst()
                    .orElse(null);

            if (constructorCall == null) {
                Set<CtConstructor> constructors = target.getConstructors();

                boolean hasConstructor = constructors.stream()
                        .anyMatch(c -> c.getParameters().size() == 0);

                if (hasConstructor) {
                    constructorCall = inputProgram.getFactory().Code().createConstructorCall(target.getReference());
                }
            }
        }
        return constructorCall;
    }

    protected Set<ObjectInstantiation> buildObjectInstantiation() {
        Set<ObjectInstantiation> objectInstantiations = new HashSet<>();
        Set<String> filter = new HashSet<>();
        List<CtConstructorCall> constructorCalls = inputProgram.getAllElement(CtConstructorCall.class);

        for (CtConstructorCall cc : constructorCalls) {
            String string = cc.getExecutable().toString();
            if (!filter.contains(string)) {
                filter.add(string);
                objectInstantiations.add(new ObjectInstantiation(cc));
            }
        }

        objectInstantiations.parallelStream()
                .forEach(oi -> oi.update(objectInstantiations));
        return objectInstantiations;
    }
}
