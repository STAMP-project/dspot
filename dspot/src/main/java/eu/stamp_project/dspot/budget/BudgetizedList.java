package eu.stamp_project.dspot.budget;

import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/07/18
 */
public class BudgetizedList extends ArrayList<CtMethod<?>> {

    private int limitedSize = 5;

    @Override
    public boolean add(CtMethod<?> ctMethod) {
        if (this.size() + 1 > limitedSize) {
            throw new RuntimeException("limite exceed");
        }
        return super.add(ctMethod);
    }


    public static void main(String[] args) {
        final BudgetizedList ctMethods = new BudgetizedList();
        ctMethods.add(null);
        ctMethods.add(null);
        ctMethods.add(null);
        ctMethods.add(null);
        ctMethods.add(null);
        try {
            ctMethods.add(null);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
