package eu.stamp_project.utils;

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

    private Map<String, Integer> numberOfAssertionAdded;

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

    public static void updateAssertionOf(CtMethod method, int number) {
        updateGivenMap(method, number, getInstance().numberOfAssertionAdded);
    }

    public static void updateInputOf(CtMethod method, int number) {
        updateGivenMap(method, number, getInstance().numberOfInputAdded);
    }

    public static Integer getAssertionOf(CtMethod method) {
        return getInstance().numberOfAssertionAdded.get(method.getSimpleName()) == null ? 0 : getInstance().numberOfAssertionAdded.get(method.getSimpleName());
    }

    public static Integer getInputOf(CtMethod method) {
        return getInstance().numberOfInputAdded.get(method.getSimpleName()) == null ? 0 : getInstance().numberOfInputAdded.get(method.getSimpleName());
    }


    public static Integer getAllAssertions() {
        return getAllOfGivenMap(getInstance().numberOfAssertionAdded);
    }

    public static Integer getAllInput() {
        return getAllOfGivenMap(getInstance().numberOfInputAdded);
    }

    private static Integer getAllOfGivenMap(Map<String, Integer> map) {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static Integer getAssertionOfSinceOrigin(CtMethod method) {
        CtMethod<?> currentMethod = method;
        CtMethod<?> parent;
        int countAssertion = getAssertionOf(currentMethod);
        while ((parent = AmplificationHelper.getAmpTestParent(currentMethod)) != null) {
            currentMethod = parent;
            countAssertion += getAssertionOf(currentMethod);
        }
        return countAssertion;
    }

    public static Integer getInputOfSinceOrigin(CtMethod method) {
        CtMethod currentMethod = method;
        CtMethod parent;
        int countAssertion = 0;
        while ((parent = AmplificationHelper.getAmpTestParent(currentMethod)) != null) {
            countAssertion += getInputOf(currentMethod);
            currentMethod = parent;
        }
        countAssertion += getInputOf(currentMethod);
        return countAssertion;
    }

    private synchronized static void updateGivenMap(CtMethod method, int number, Map<String, Integer> mapToBeUpdated) {
        if (!mapToBeUpdated.containsKey(method.getSimpleName())) {
            mapToBeUpdated.put(method.getSimpleName(), 0);
        }
        mapToBeUpdated.put(method.getSimpleName(), mapToBeUpdated.get(method.getSimpleName()) + number);
    }

    public static void reset() {
        _instance = null;
    }

}
