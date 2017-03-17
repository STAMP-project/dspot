package fr.inria.diversify.dspot.support;

import fr.inria.diversify.dspot.AmplificationHelper;
import spoon.reflect.declaration.CtMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/10/17
 */
public class Counter {

    private static Counter _instance;

    //private Map<CtMethod<?>, Integer> numberOfAssertionAdded;
    private Map<String, Integer> numberOfAssertionAdded;

    //private Map<CtMethod<?>, Integer> numberOfInputAdded;
    private Map<String, Integer> numberOfInputAdded;

    private Counter() {
        this.numberOfAssertionAdded = new HashMap<>();
        this.numberOfInputAdded = new HashMap<>();
    }

    private static Counter getInstance() {
        if (_instance == null) {
            _instance = new Counter();
        }
        return _instance;
    }

    public static void updateAssertionOf(CtMethod<?> method, int number) {
        updateGivenMap(method, number, getInstance().numberOfAssertionAdded);
    }

    public static void updateInputOf(CtMethod<?> method, int number) {
        updateGivenMap(method, number, getInstance().numberOfInputAdded);
    }

    public static Integer getAssertionOf(CtMethod<?> method) {
        return getInstance().numberOfAssertionAdded.get(method.getSimpleName()) == null ? 0 : getInstance().numberOfAssertionAdded.get(method.getSimpleName());
    }

    public static Integer getInputOf(CtMethod<?> method) {
        return getInstance().numberOfInputAdded.get(method.getSimpleName()) == null ? 0 : getInstance().numberOfInputAdded.get(method.getSimpleName());
    }

    public static Integer getAssertionOfSinceOrigin(CtMethod<?> method) {
        CtMethod<?> currentMethod = method;
        CtMethod<?> parent;
        int countAssertion = getAssertionOf(currentMethod);
        while ((parent = AmplificationHelper.getAmpTestToParent().get(currentMethod)) != null) {
            currentMethod = parent;
            countAssertion += getAssertionOf(currentMethod);
        }
        return countAssertion;
    }

    public static Integer getInputOfSinceOrigin(CtMethod<?> method) {
        CtMethod<?> currentMethod = method;
        CtMethod<?> parent;
        int countAssertion = 0;
        while ((parent = AmplificationHelper.getAmpTestToParent().get(currentMethod)) != null) {
            countAssertion += getInputOf(currentMethod);
            currentMethod = parent;
        }
        countAssertion += getInputOf(currentMethod);
        return countAssertion;
    }

    private static void updateGivenMap(CtMethod<?> method, int number, Map<String, Integer> mapToBeUpdated) {
        if (!mapToBeUpdated.containsKey(method.getSimpleName())) {
            mapToBeUpdated.put(method.getSimpleName(), 0);
        }
        mapToBeUpdated.put(method.getSimpleName(), mapToBeUpdated.get(method.getSimpleName()) + number);
    }

}
