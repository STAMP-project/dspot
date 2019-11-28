package eu.stamp_project.dspot.common.collector;

import eu.stamp_project.dspot.common.configuration.options.AmplifierEnum;
import eu.stamp_project.dspot.common.configuration.options.SelectorEnum;

import java.util.List;

public class NullCollector implements Collector {
	public NullCollector(){}

	@Override
	public void reportInitInformation(List<AmplifierEnum> amplifiers,
									  SelectorEnum testCriterion,
									  int iteration,
									  boolean gregor,
									  boolean descartes,
									  int executeTestParallelWithNumberProcessors) { }
	@Override
	public void reportSelectorInformation(String str) {}
	@Override
	public void reportAmpTestPath(String pathName) {}
	@Override
	public void sendInfo(){}
}
