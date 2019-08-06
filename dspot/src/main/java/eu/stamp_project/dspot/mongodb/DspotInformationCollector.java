package eu.stamp_project.mongodb;

import com.martiansoftware.jsap.JSAPResult;

public interface DspotInformationCollector {
	void reportInitInformation(JSAPResult jsapConfig);
	void reportJacocotInformation(String testName, String initialCoverage,String ampCoverage,String totalCoverage);
	void reportMutantInformation(String testName,String originalKilledMutants,String newMutantKilled);
	void reportAmpTestPath(String pathName);
}
