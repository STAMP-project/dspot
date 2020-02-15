package eu.stamp_project.dspot.common.configuration;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.List;

public class TestTuple {
    public CtType<?> testClassToBeAmplified;
    public List<CtMethod<?>> testMethodsToBeAmplified;

    public TestTuple(CtType<?> testClassToBeAmplified, List<CtMethod<?>> testMethodsToBeAmplified){
        this.testClassToBeAmplified = testClassToBeAmplified;
        this.testMethodsToBeAmplified = testMethodsToBeAmplified;
    }
}
