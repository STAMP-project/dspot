package eu.stamp_project.utils.collector;

import com.martiansoftware.jsap.JSAPResult;

public class NullCollector implements Collector {
	public NullCollector(){}
	public void reportInitInformation(JSAPResult jsapConfig){};
	@Override
	public void reportSelectorInformation(String str) {}
	@Override
	public void reportAmpTestPath(String pathName) {}
	@Override
	public void sendInfo(){}
}
