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

import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.io.IOException;

public class MongodbManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongodbManager.class);
	/*Format of date for saving in Mongodb*/
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static MongodbManager single_instance = null;

	/*Empty later*/
	private static boolean dbConnectable;
	private static String mongoUrl;
	private static String dbName;
	private static String repoSlug;
	private static String colName;
	private static MongoClient mongoClient;

	/*Jacoco Selector*/
	public List<Document> jacocoSelectorDocs;

	/*PitMutantScoreSelector*/
	public List<Document> pitMutantScoreSelectorDocs;/*

	/*Argumets from input*/
	public Document argsDoc;

	/*Output paths for java files*/
	public List<String> javaPathList;

	private MongodbManager () {
		this.jacocoSelectorDocs = new ArrayList<Document>();
		this.pitMutantScoreSelectorDocs = new ArrayList<Document>();
		this.argsDoc = new Document();
		this.javaPathList = new ArrayList<String>();
		this.dbConnectable = testConnectionToDb();
	}

	public static MongodbManager getInstance() {
		if (single_instance == null) {
			single_instance = new MongodbManager();
		}
		return single_instance;
	}


	public static void initMongodbManager (String mongoUrl_ln, String dbName_ln, String colName_ln, String repoSlug_ln) {
		mongoUrl = mongoUrl_ln;
		dbName = dbName_ln;
		colName = colName_ln;
		repoSlug = repoSlug_ln;
		dbConnectable = testConnectionToDb();
	}


	private static boolean testConnectionToDb() {
		try {
			mongoClient = new MongoClient(new MongoClientURI(mongoUrl));
			mongoClient.close();
			LOGGER.warn("Mongodb connection Successful");
			return true;
		}catch (Exception e) {
			LOGGER.warn("failed to connect to mongodb, No information will be submitted at the end");
			return false;
		}
	}

	public boolean getDbConnectable() {
		return dbConnectable;
	}

	public void sendInfoToDb() {
		try {
		    mongoClient = new MongoClient(new MongoClientURI(this.mongoUrl));
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


        	Document ampFiles = new Document();
        	for (String path : javaPathList) {
        		String fileName = this.getFileNameGivenPath(path).replace(".java","");
        		String content = this.readFile(path,StandardCharsets.US_ASCII).replace(".","/D/");
        		LOGGER.warn("fileName: " + fileName);
        		LOGGER.warn("content: " + content);
        		ampFiles.append(fileName,content);
        	}
        	mainDoc.append("AmpTestFiles",ampFiles);
			coll.insertOne(mainDoc);
			mongoClient.close();
		}catch (Exception e) {
			System.out.println("failed to connect to mongodb");
		}
	}

	private static String readFile(String path, Charset encoding) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(path));
			return new String(encoded, encoding);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getCurrentDate() {
		Date date = new Date();
		return this.dateFormat.format(date);
	}

	private String getFileNameGivenPath(String path) {
		String[] bits = path.split("/");
		return bits[bits.length - 1];
	}
}
