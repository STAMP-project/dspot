package eu.stamp_project.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.*;

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
/*import java.util.ArrayList;
*/
public class MongodbManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongodbManager.class);
	/*Format of date for saving in Mongodb*/
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static MongodbManager single_instance = null;

	/*Empty later*/
	public boolean dbConnectable;
	private String mongoUrl = "mongodb://localhost:27017";
	private String dbName = "Dspot";
	private String repoSlug = "travisplay";
	private String colName = "testRecords";


	/*Jacoco Selector*/
	public List<Document> jacocoSelectorDocs;

	/*PitMutantScoreSelector*/
	public List<Document> pitMutantScoreSelectorDocs;/*

	/*Argumets from input*/
	public Document argsDoc;

	private MongodbManager () {
		this.jacocoSelectorDocs = new ArrayList<Document>();
		this.pitMutantScoreSelectorDocs = new ArrayList<Document>();
		this.argsDoc = new Document();
	}

	public static MongodbManager getInstance() {
		if (single_instance == null) {
			single_instance = new MongodbManager();
		}
		return single_instance;
	}


	public void initMongodbManager (String mongoUrl, String dbName, String colName, String repoSlug) {
		this.mongoUrl = mongoUrl;
		this.dbName = dbName;
		this.colName = colName;
		this.repoSlug = repoSlug;
		this.dbConnectable = this.testConnectionToDb();
	}

	private String getCurrentDate() {
		Date date = new Date();
		return this.dateFormat.format(date);
	}

	private boolean testConnectionToDb() {
		try {
			MongoClient mongoClient = new MongoClient(new MongoClientURI(this.mongoUrl));
			mongoClient.close();
			return true;
		}catch (Exception e) {
			LOGGER.info("failed to connect to mongodb");
			return false;
		}
	}

	public void sendInfoToDb() {
		try {
		    MongoClient mongoClient = new MongoClient(new MongoClientURI(this.mongoUrl));
			MongoDatabase database = mongoClient.getDatabase(this.dbName);
			MongoCollection<Document> coll = database.getCollection(this.colName);

			Document mainDoc = new Document("RepoSlug", this.repoSlug)
				.append("Date",this.getCurrentDate());
        	mainDoc.append("AmpOptions",argsDoc);

        	if (argsDoc.get("test-criterion").equals("JacocoCoverageSelector")) {
        		Document mergedDoc = new Document();
        		for (Document doc : jacocoSelectorDocs) {
        			mergedDoc.putAll(doc);
        		}
        		mainDoc.append("AmpResult",mergedDoc);
        	}else if (argsDoc.get("test-criterion").equals("PitMutantScoreSelector")) {
        		Document mergedDoc = new Document();
        		for (Document doc : pitMutantScoreSelectorDocs) {
        			mergedDoc.putAll(doc);
        		}
        		mainDoc.append("AmpResult",mergedDoc);
        	}

			coll.insertOne(mainDoc);
			mongoClient.close();
		}catch (Exception e) {
			System.out.println("failed to connect to mongodb");
		}
	}
}
