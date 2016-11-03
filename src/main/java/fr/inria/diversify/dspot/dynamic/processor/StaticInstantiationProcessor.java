package fr.inria.diversify.dspot.dynamic.processor;

import fr.inria.diversify.dspot.dynamic.objectInstanciationTree.ObjectInstantiation;
import fr.inria.diversify.profiling.processor.main.AbstractLoggingInstrumenter;
import fr.inria.diversify.runner.InputProgram;
import spoon.reflect.code.CtConstructorCall;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Simon
 * Date: 22/08/16
 */
public class StaticInstantiationProcessor extends AbstractLoggingInstrumenter<CtConstructorCall> {
    protected Set<ObjectInstantiation> objectInstantiations;

    public  StaticInstantiationProcessor(InputProgram inputProgram) {
        super(inputProgram);
        objectInstantiations = new HashSet<>();
    }

    public boolean isToBeProcessed(CtConstructorCall candidate) {
        String string = candidate.getExecutable().toString();
        return !objectInstantiations.stream()
                .map(oi -> oi.getConstructorString())
                .anyMatch(c -> c.equals(string));
    }

    @Override
    public void process(CtConstructorCall element) {
        ObjectInstantiation oi = new ObjectInstantiation(element);
        objectInstantiations.add(oi);
    }

    public void toDot(String file) throws IOException {
        FileWriter fileWriter = new FileWriter(new File(file));

        fileWriter.write("digraph G {\n");
        objectInstantiations.stream()
                .forEach(oi -> {
                    try {
                        fileWriter.write(oi.toDot());
                    } catch (IOException e) {}
                });

        fileWriter.write("}");
        fileWriter.close();
    }

    public Set<ObjectInstantiation> getObjectInstantiations() {
        return objectInstantiations;
    }
}
