package eu.stamp_project.utils.collector;

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

    /* MongodbCollector related */
    private static String mongoUrl;
    private static String mongoDbname;
    private static String mongoColname;
    private static String repoSlug;
    private static String repoBranch;
    private static boolean restful;

    public void setMongoUrl(String mongoUrl) {
        this.mongoUrl = mongoUrl;
    }

    public String getMongoUrl() {
        return this.mongoUrl;
    }

    public void setMongoDbname(String mongoDbname) {
        this.mongoDbname = mongoDbname;
    }

    public String getMongoDbname() {
        return this.mongoDbname;
    }

    public void setMongoColname(String mongoColname) {
        this.mongoColname = mongoColname;
    }

    public String getMongoColname() {
        return this.mongoColname;
    }

    public void setRepoSlug(String repoSlug) {
        this.repoSlug = repoSlug;
    }

    public String getRepoSlug() {
        return this.repoSlug;
    }

    public void setRepoBranch(String repoBranch) {
        this.repoBranch = repoBranch;
    }

    public String getRepoBranch() {
        return this.repoBranch;
    }

    public void setRestful(boolean restful) {
        this.restful = restful;
    }

    public boolean getRestful() {
        return this.restful;
    }

}
