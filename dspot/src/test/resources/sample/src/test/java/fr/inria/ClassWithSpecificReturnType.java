package fr.inria;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class ClassWithSpecificReturnType {

    public class Element<T> {

    }

    public Element<?>[] elements = new Element[5];

    public ClassWithSpecificReturnType() {
        elements[0] = new Element<String>();
    }

    public Element<?> getElementQuestionMark(int i) {
        return (Element<?>) this.elements[i];
    }

    public <T> Element<T> getElementGeneric(int i) {
        return (Element<T>) this.elements[i];
    }

    public Element<String> getElementSpecific() {
        return (Element<String>) this.elements[0];
    }

    public void tryGetters() {
        final ClassWithSpecificReturnType myClassWithSpecificReturnType = new ClassWithSpecificReturnType();
        myClassWithSpecificReturnType.getElementQuestionMark(0);
        myClassWithSpecificReturnType.getElementGeneric(0);
        myClassWithSpecificReturnType.getElementSpecific();
    }
}
