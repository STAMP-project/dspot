package eu.stamp_project.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

import com.martiansoftware.jsap.JSAPResult;

// Receive data from selectors, JSAPOptions and amp testfiles paths to put to mongodb.
public class MongodbManager implements DspotInformationCollector {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongodbManager.class);

	public MongodbManager(){}

	public void initMongodbManager () {}

	@Override
	public void reportInitInformation(JSAPResult jsapConfig){
		this.initMongodbManager();
	}

	/**
	 * parse
	 *
	 * @param JSONString, a string of json format
	 */
	@Override
	public void reportSelectorInformation(String str) {
		JSONObject jsonObject = new JSONObject(str);
	}

	@Override
	public void reportAmpTestPath(String pathName) {}
}
