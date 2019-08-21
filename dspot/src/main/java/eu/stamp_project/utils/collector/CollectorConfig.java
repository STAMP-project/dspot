package eu.stamp_project.utils.collector;

import eu.stamp_project.utils.options.CollectorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Used to collect output information from selector class.
public class CollectorConfig {

	/* Initialization */
	private static CollectorConfig collectorConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorConfig.class);

    public static CollectorConfig getInstance() {
    	if (collectorConfig == null ) {
    		collectorConfig = new CollectorConfig();
    	}
        return collectorConfig;
    }

    /* Variables and getters/setters */
    private static DspotInformationCollector collector = new NullCollector();

    public void setInformationCollector(String collector) {
        this.collector = CollectorEnum.valueOf(collector).getCollector();
    }

    public static DspotInformationCollector getInformationCollector() {
        return collector;
    }

    /* MongodbCollector related */
    private static String mongoUrl;

    public void setMongoUrl(String mongoUrl) {
        this.mongoUrl = mongoUrl;
    }

    public String getMongoUrl() {
        return this.mongoUrl;
    }
}
