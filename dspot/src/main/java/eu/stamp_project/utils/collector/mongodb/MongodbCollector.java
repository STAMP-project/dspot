package eu.stamp_project.utils.collector.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stamp_project.utils.collector.DspotInformationCollector;
import eu.stamp_project.utils.collector.CollectorConfig;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.*;
import org.bson.Document;

/*Parsing date*/
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.martiansoftware.jsap.JSAPResult;

// Receive data from selectors, JSAPOptions and amp testfiles paths to put to mongodb.
public class MongodbCollector implements DspotInformationCollector {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongodbCollector.class);

	/* Config objects */
	private static boolean dbConnectable;

	/* Will be assigned by JSAP options later*/
	private static final String dbName = "Dspot";
	private static final String colName = "AmpTestRecords";
	private static final String repoSlug = "USER/Testing";
	private static final String repoBranch = "master";
	private static String mongoUrl;

	/* Mongodb objects*/
	private static MongoClient mongoClient;
	private static MongoDatabase database;
	private static MongoCollection<Document> coll;

	/*Docs to be submitted to Mongodb*/
	private static List<Document> selectorDocs;
	private static List<String> javaPathList;
	private static Document argsDoc;
	private static Document totalResultDoc;

	/* Helper variables*/
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	/* Initiation*/
	public MongodbCollector () {
		this.selectorDocs = new ArrayList<Document>();
		this.javaPathList = new ArrayList<String>();
		this.argsDoc = new Document();
		this.totalResultDoc = new Document();
		this.mongoUrl = CollectorConfig.getInstance().getMongoUrl();
	}

	/*Connection related*/
	public static boolean ConnectableToMongodb() {
		try {
			mongoClient = new MongoClient(new MongoClientURI(mongoUrl));
			mongoClient.close();
			LOGGER.warn("Mongodb connection Successful");
			dbConnectable = true;
		}catch (Exception e) {
			LOGGER.warn("failed to connect to mongodb, No information will be submitted at the end");
			dbConnectable = false;
		}
		return dbConnectable;
	}

	/* Info submission related*/
	public void sendInfo() {
		try {
		    mongoClient = new MongoClient(new MongoClientURI(mongoUrl));
			database = mongoClient.getDatabase(dbName);
			coll = database.getCollection(colName);;

			/* Insert repo specific and run specific information*/
			Document mainDoc = new Document("RepoSlug", this.repoSlug)
				.append("RepoBranch",this.repoBranch)
				.append("Date",this.getCurrentDate());
        	mainDoc.append("AmpOptions",argsDoc);

        	/* Insert Amplified result statistic for each testcase*/
    		Document mergedDoc = new Document();
    		for (Document doc : selectorDocs) {
    			mergedDoc.putAll(doc);
    		}
    		mergedDoc.append("TotalResult",totalResultDoc);
    		mainDoc.append("AmpResult",mergedDoc);
			coll.insertOne(mainDoc);
			mongoClient.close();
		}catch (Exception e) {
			LOGGER.warn("failed to connect to mongodb");
		}
	}

	/*Collector related*/
	@Override
	public void reportInitInformation(JSAPResult jsapConfig){
		/* Extract JSAP options*/
        final List<String> amplifiers = new ArrayList<>(Arrays.asList(jsapConfig.getStringArray("amplifiers")));
        final String testCriterion = jsapConfig.getString("test-criterion");
        final int iteration = jsapConfig.getInt("iteration");
        final boolean gregor = jsapConfig.getBoolean("gregor");
        final boolean descartes = jsapConfig.getBoolean("descartes");
        final int executeTestParallelWithNumberProcessors =
                jsapConfig.getInt("execute-test-parallel-with-number-processors") != 0 ?
                        jsapConfig.getInt("execute-test-parallel-with-number-processors") : Runtime.getRuntime().availableProcessors();


        /* Append to doc for mongo later*/
        argsDoc.append("amplifiers",Arrays.toString(jsapConfig.getStringArray("amplifiers")));
        argsDoc.append("test-criterion",testCriterion);
        argsDoc.append("iteration",Integer.toString(iteration));
        argsDoc.append("gregor","" + gregor);
        argsDoc.append("descartes","" + descartes);
        argsDoc.append("executeTestParallelWithNumberProcessors",Integer.toString(executeTestParallelWithNumberProcessors));
	}

	@Override
	public void reportSelectorInformation(String str) {
		/* dot is not allowed in Mongodb */
		str = str.replace(".","/D/");
		LOGGER.warn(str);
		Document doc = Document.parse(str);
		String testName = doc.keySet().iterator().next();
		Document innerDoc = (Document) doc.get(testName);
		for ( String key : innerDoc.keySet() ) {
           	String totalKeyName = "total" + this.toUpperCaseFirstLetter(key);

           	/*To avoid totaltotalCoverage name.*/
           	if (key.equals("totalCoverage")) {
           		totalKeyName = "totalCovAcrossAllTests";
           	}

           	if (!totalResultDoc.containsKey(totalKeyName)) {
           		this.totalResultDoc.append(totalKeyName,innerDoc.getInteger(key));
           	} else {
           		this.totalResultDoc.append(totalKeyName, this.totalResultDoc.getInteger(totalKeyName) + innerDoc.getInteger(key));
           	}
        }
		selectorDocs.add(doc);
	}

	@Override
	public void reportAmpTestPath(String pathName) {
		javaPathList.add(pathName);
	}

	/* Helpers */
	private String getCurrentDate() {
		Date date = new Date();
		return this.dateFormat.format(date);
	}

	private String toUpperCaseFirstLetter(String input) {
		if (input.length() < 1) {
			return input;
		}
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	/* Methods used for testing Mongo*/
	public static MongoClient connectToMongo (String mongoUrl) {
		return new MongoClient(new MongoClientURI(mongoUrl));
	}

	public static MongoDatabase getDatabase (String dbName , MongoClient mongoClient) {
		return mongoClient.getDatabase(dbName);
	}

	public static MongoCollection<Document> getCollection (String colName,MongoDatabase database) {
		return database.getCollection(colName);
	}
}

