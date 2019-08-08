package eu.stamp_project.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.martiansoftware.jsap.JSAPResult;

public class MongodbManager implements DspotInformationCollector {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongodbManager.class);

	public MongodbManager(){}

	public void initMongodbManager () {}

	@Override
	public void reportInitInformation(JSAPResult jsapConfig){
		this.initMongodbManager();
	}

	@Override
	public void reportSelectorInformation(String str) {}

	@Override
	public void reportAmpTestPath(String pathName) {}
}
