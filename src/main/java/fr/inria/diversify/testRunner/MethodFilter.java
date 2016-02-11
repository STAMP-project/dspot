package fr.inria.diversify.testRunner;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.List;

/**
 * User: Simon
 * Date: 27/10/15
 * Time: 16:26
 */
public class MethodFilter extends Filter {
    protected List<String> methods;

    public MethodFilter(List<String> methods) {
        this.methods = methods;
    }

    public boolean shouldRun(Description description) {
        if (description.isTest()) {
            return methods.contains(description.getMethodName());
        }

        // explicitly check if any children want to run
        for (Description each : description.getChildren()) {
            if (shouldRun(each)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String describe() {
        return "";
    }
}
