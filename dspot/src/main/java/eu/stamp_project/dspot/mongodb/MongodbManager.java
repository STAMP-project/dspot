package eu.stamp_project.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.JSAPResult;

public class MongodbManager implements DspotInformationCollector {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongodbManager.class);
	private static MongodbManager mongodbManager;

	public static MongodbManager getInstance(){
		if (mongodbManager == null) {
			mongodbManager = new MongodbManager();
		}
		return mongodbManager;
	}

	public static void initMongodbManager () {}

	@Override
	public void reportInitInformation(JSAPResult jsapConfig){}
	@Override
	public void reportJacocotInformation(String testName, String initialCoverage,String ampCoverage,String totalCoverage){}
	@Override
	public void reportMutantInformation(String testName,String originalKilledMutants,String newMutantKilled){}
	@Override
	public void reportAmpTestPath(String pathName) {}
}
