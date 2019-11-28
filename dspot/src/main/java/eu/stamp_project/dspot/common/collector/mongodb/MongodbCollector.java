package eu.stamp_project.dspot.common.collector.mongodb;

import eu.stamp_project.utils.options.AmplifierEnum;
import eu.stamp_project.utils.options.SelectorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stamp_project.dspot.common.collector.Collector;
import eu.stamp_project.dspot.common.collector.smtp.EmailSender;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

import org.bson.Document;

/*Parsing date*/
import java.util.Arrays;
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

// Receive data from selectors, JSAPOptions and amp testfiles paths to put to mongodb.
public class MongodbCollector implements Collector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbCollector.class);

    /* Config objects */
    private String dbName;
    private String colName;
    private String repoSlug;
    private String repoBranch;
    private String mongoUrl;
    private String email;
    private boolean restful;
    private boolean dbConnectable;

    /* Mongodb objects*/
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> coll;

    /*Docs to be submitted to Mongodb*/
    private List<Document> selectorDocs;
    private List<String> javaPathList;
    private Document argsDoc;
    private Document totalResultDoc;

    /* Helper variables*/
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private EmailSender emailSender;

    /* Initiation*/
    public MongodbCollector(String mongoUrl,
                            String dbName,
                            String colName,
                            String repoSlug,
                            String repoBranch,
                            boolean restful,
                            EmailSender emailSender) {
        this.selectorDocs = new ArrayList<Document>();
        this.javaPathList = new ArrayList<String>();
        this.argsDoc = new Document();
        this.totalResultDoc = new Document();
        this.mongoUrl = mongoUrl;
        this.dbName = dbName;
        this.colName = colName;
        this.repoSlug = repoSlug;
        this.repoBranch = repoBranch;
        this.restful = restful;
        this.emailSender = emailSender;
    }

    /* Initialize a completely new document - when not restful mode*/
    public Document mainDocInit() {
        Document mainDoc = new Document("RepoSlug", this.repoSlug)
                .append("RepoBranch", this.repoBranch)
                .append("Date", this.getCurrentDate());
        mainDoc.append("AmpOptions", argsDoc);
        return mainDoc;
    }

    /*Connection related*/
    public boolean ConnectableToMongodb() {
        try {
            mongoClient = new MongoClient(new MongoClientURI(mongoUrl));
            mongoClient.close();
            LOGGER.warn("Mongodb connection Successful");
            dbConnectable = true;
        } catch (Exception e) {
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
            coll = database.getCollection(colName);
            ;
            /* Insert Amplified result statistic for each testcase*/
            Document mergedDoc = new Document();
            for (Document doc : selectorDocs) {
                mergedDoc.putAll(doc);
            }
            mergedDoc.append("TotalResult", totalResultDoc);

            /* Insert repo specific and run specific information*/
            Document mainDoc;
            if (!restful) {
                mainDoc = this.mainDocInit();
                mainDoc.append("AmpResult", mergedDoc);
                coll.insertOne(mainDoc);
            } else {
                /* if restful find a pending doc and fetch the email attached to it*/
                mainDoc = coll.find(and(eq("RepoSlug", this.repoSlug), eq("RepoBranch", this.repoBranch), eq("State", "pending"))).projection(fields(excludeId())).first();
                this.email = mainDoc.get("Email").toString();
                mainDoc.append("State", "recent");
                mainDoc.append("AmpOptions", argsDoc);
                mainDoc.append("AmpResult", mergedDoc);
                /* Also set the previous recent repo as old, update the pending doc with output amp results and state as recent */
                coll.updateOne(and(eq("RepoSlug", this.repoSlug), eq("RepoBranch", this.repoBranch), eq("State", "recent")), new Document("$set", new Document("State", "old")));
                coll.updateOne(and(eq("RepoSlug", this.repoSlug), eq("RepoBranch", this.repoBranch), eq("State", "pending")), new Document("$set", mainDoc));

                // Send output files through emails
                this.emailSender.sendEmail(this.constructMessageWithFileContents(javaPathList), "Amplification succeeded", email);
            }
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("failed to connect to mongodb");
        }
    }

    @Override
    public void reportInitInformation(List<AmplifierEnum> amplifiers,
                                      SelectorEnum testCriterion,
                                      int iteration,
                                      boolean gregor,
                                      boolean descartes,
                                      int executeTestParallelWithNumberProcessors) {
        argsDoc.append("amplifiers", Arrays.toString(amplifiers.stream().map(AmplifierEnum::toString).toArray()));
        argsDoc.append("test-criterion", testCriterion.toString());
        argsDoc.append("iteration", Integer.toString(iteration));
        argsDoc.append("gregor", "" + gregor);
        argsDoc.append("descartes", "" + descartes);
        argsDoc.append("executeTestParallelWithNumberProcessors", Integer.toString(executeTestParallelWithNumberProcessors));
    }

    @Override
    public void reportSelectorInformation(String str) {
        /* dot is not allowed in Mongodb */
        str = str.replace(".", "/D/");
        Document doc = Document.parse(str);
        String testName = doc.keySet().iterator().next();
        Document innerDoc = (Document) doc.get(testName);
        for (String key : innerDoc.keySet()) {
            String totalKeyName = "total" + this.toUpperCaseFirstLetter(key);

            /*To avoid totaltotalCoverage name.*/
            if (key.equals("totalCoverage")) {
                totalKeyName = "totalCovAcrossAllTests";
            }

            if (!totalResultDoc.containsKey(totalKeyName)) {
                this.totalResultDoc.append(totalKeyName, innerDoc.getInteger(key));
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

    // Read file according to a certain encoding
    private static String readFile(String path, Charset encoding) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String constructMessageWithFileContents(List<String> files) {
        StringBuilder messageText = new StringBuilder();
        if (javaPathList.size() > 0) {
            messageText.append("Here are your amplified tests \n\n");
            for (String file : javaPathList) {
                String[] strList = file.split("/");
                String fileName = strList[strList.length - 1];
                String content = this.readFile(file, StandardCharsets.US_ASCII);

                messageText.append(fileName + ":\n --CONTENT--START-- \n");
                messageText.append(content + "\n --CONTENT--END--\n");
            }
        } else {
            messageText.append("Amplication succeeded but no amplified tests have been found \n");
        }
        messageText.append("\n --STAMP/Dspot");
        return messageText.toString();
    }

    /* Methods used for testing Mongo*/
    public static MongoClient connectToMongo(String mongoUrl) {
        return new MongoClient(new MongoClientURI(mongoUrl));
    }

    public static MongoDatabase getDatabase(String dbName, MongoClient mongoClient) {
        return mongoClient.getDatabase(dbName);
    }

    public static MongoCollection<Document> getCollection(String colName, MongoDatabase database) {
        return database.getCollection(colName);
    }
}
