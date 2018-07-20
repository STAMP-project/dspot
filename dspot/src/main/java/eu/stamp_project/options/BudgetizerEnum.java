package eu.stamp_project.options;

import eu.stamp_project.dspot.budget.Budgetizer;
import eu.stamp_project.dspot.budget.NoBudgetizer;
import eu.stamp_project.dspot.budget.SimpleBudgetizer;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/07/18
 */
public enum BudgetizerEnum {

    NoBudgetizer {
        @Override
        public Budgetizer getBugtizer() {
            return new NoBudgetizer();
        }
    },
    SimpleBudgetizer {
        @Override
        public Budgetizer getBugtizer() {
            return new SimpleBudgetizer();
        }
    };

    public abstract Budgetizer getBugtizer();

}
