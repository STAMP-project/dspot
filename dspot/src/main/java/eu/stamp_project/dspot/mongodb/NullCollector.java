package eu.stamp_project.mongodb;

import com.martiansoftware.jsap.JSAPResult;

public class NullCollector implements DspotInformationCollector {

	public NullCollector(){}
	public void reportInitInformation(JSAPResult jsapConfig){};
	@Override
	public void reportSelectorInformation(String str) {}
	@Override
	public void reportAmpTestPath(String pathName) {}
}
