package eu.stamp_project.dspot.common.collector;

import eu.stamp_project.utils.options.AmplifierEnum;
import eu.stamp_project.utils.options.SelectorEnum;

import java.util.List;

// Used to collect output information from selector class.
public interface Collector {

    /*save the parsed java options*/
    void reportInitInformation(List<AmplifierEnum> amplifiers,
                               SelectorEnum testCriterion,
                               int iteration,
                               boolean gregor,
                               boolean descartes,
                               int executeTestParallelWithNumberProcessors);

    /*string reports, containing values from selectors.*/
    void reportSelectorInformation(String info);

    /*collect each test's path*/
    void reportAmpTestPath(String pathName);

    /*send info over to some other instance*/
    void sendInfo();
}
