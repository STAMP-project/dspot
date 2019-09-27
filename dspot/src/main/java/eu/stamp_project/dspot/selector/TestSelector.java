package eu.stamp_project.dspot.selector;

import eu.stamp_project.utils.report.output.selector.TestSelectorElementReport;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 1/5/17
 */
public interface TestSelector {

    /**
       This methods initialize the test selector
       @return a boolean to tell whether the initialization went well (true) or not (false)
     */
    boolean init();

    /**
     * This method selects test method to be amplified among the provided test list.
     * Contract: the provided test list should contain only already compiled test methods and all the test methods should pass.
     *
     * @param classTest test class under amplification
     * @param testsToBeAmplified the list among which the selection is done.
     * @return selected test methods to be amplified
     */
    List<CtMethod<?>> selectToAmplify(CtType<?> classTest, List<CtMethod<?>> testsToBeAmplified);

    /**
     * This method selects test methods to be kept, <i>i.e.</i> output of DSpot.
     * Contract: the provided test list should contain only already compiled test methods and all the test methods should pass.
     * @param amplifiedTestToBeKept the list among which the selection is done.
     * @return selected amplified test methods to output
     */
    List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept);

    /**
     * this method should wrap up the result of the amplification according to the test criterion.
     * @return an instance of {@link TestSelectorElementReport} that contains all the result of the amplification for one test class
     */
    TestSelectorElementReport report();

    // TODO need to refactor selectToKeep in two methods: selectToKeep and selectForNextIteration
    // TODO because amplified test methods are not necessarily meant to be kept(selectToKeep might return an empty list),
    //  but we need something to amplify (selectForNextIteration will never return an empty list, unless Dspot is not able to amplify)
    @Deprecated
    List<CtMethod<?>> getAmplifiedTestCases();

}
