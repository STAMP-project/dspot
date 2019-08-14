package eu.stamp_project.dspot.mongodb;

import com.martiansoftware.jsap.JSAPResult;

// Used to collect output information from selector class.
public interface DspotInformationCollector {
	// save the parsed java options
	void reportInitInformation(JSAPResult jsapConfig);
	// string reports, containing values from selectors.
	void reportSelectorInformation(String info);
	// collect each test's path
	void reportAmpTestPath(String pathName);
}
