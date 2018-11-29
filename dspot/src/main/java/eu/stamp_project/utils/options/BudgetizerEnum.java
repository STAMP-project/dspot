package eu.stamp_project.utils.options;

import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.dspot.budget.Budgetizer;
import eu.stamp_project.dspot.budget.NoBudgetizer;
import eu.stamp_project.dspot.budget.SimpleBudgetizer;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 20/07/18
 */
public enum BudgetizerEnum {

    NoBudgetizer {
        @Override
        public Budgetizer getBudgetizer() {
            return new NoBudgetizer();
        }
        @Override
        public Budgetizer getBudgetizer(List<Amplifier> amplifiers) {
            return new NoBudgetizer(amplifiers);
        }
    },
    SimpleBudgetizer {
        @Override
        public Budgetizer getBudgetizer() {
            return new SimpleBudgetizer();
        }
        @Override
        public Budgetizer getBudgetizer(List<Amplifier> amplifiers) {
            return new SimpleBudgetizer(amplifiers);
        }
    };

    public abstract Budgetizer getBudgetizer();

    public abstract Budgetizer getBudgetizer(List<Amplifier> amplifiers);

}
