package eu.stamp_project.mongodb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import eu.stamp_project.Main;
import eu.stamp_project.mongodb.MongodbManager;

import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.*;
import org.bson.Document;

public class MongodbManagerTest {

	@Test
	public void testInfoSubmissionToMongodbPitMutantScoreSelector() {
		Main.main(new String[]{
                "--path-to-properties", "src/test/resources/sample/sample.properties",
                "--test-criterion", "PitMutantScoreSelector",
                "--test", "fr.inria.sample.TestClassWithoutAssert",
                "--path-pit-result", "src/test/resources/sample/mutations.csv",
                "--gregor",
                "--output-path", "target/trash",
                "--mongo-url","mongodb://localhost:27017",
                "--mongo-dbname","Dspot",
                "--mongo-colname","AmpTestRecords",
                "--repo-slug","Dspot/mongo-test"
        });

		/*Fetch submitted document*/
		MongoClient mongoClient = MongodbManager.connectToMongo("mongodb://localhost:27017");
		MongoCollection<Document> coll = MongodbManager.getCollection("AmpTestRecords",MongodbManager.getDatabase("Dspot",mongoClient));

		Document foundDoc = coll.find(eq("RepoSlug","Dspot/mongo-test")).projection(fields(excludeId(),exclude("Date"),exclude("AmpTestFiles"))).first();
		/*Remove from database*/
		coll.deleteOne(foundDoc);

		String expectedDocStr = "Document{{RepoSlug=Dspot/mongo-test, AmpOptions=Document{{amplifiers=[None], test-criterion=PitMutantScoreSelector, iteration=3, gregor=true, descartes=true, executeTestParallelWithNumberProcessors=4}}, AmpResult=Document{{fr/D/inria/D/sample/D/TestClassWithoutAssert=Document{{originalKilledMutants=0, NewMutantKilled=67}}}}}}";

        assertEquals(foundDoc.toString(),expectedDocStr);
	}

	@Test
	public void testInfoSubmissionToMongodbJacocoCoverageSelector() {
        Main.main(new String[]{
                "--path-to-properties", "src/test/resources/project-with-resources/project-with-resources.properties",
                "--test-criterion", "JacocoCoverageSelector",
                "--iteration", "1",
                "--mongo-url","mongodb://localhost:27017",
                "--mongo-dbname","Dspot",
                "--mongo-colname","AmpTestRecords",
                "--repo-slug","Dspot/mongo-test"
        });
        MongoClient mongoClient = MongodbManager.connectToMongo("mongodb://localhost:27017");
		MongoCollection<Document> coll = MongodbManager.getCollection("AmpTestRecords",MongodbManager.getDatabase("Dspot",mongoClient));

		Document foundDoc = coll.find(eq("RepoSlug","Dspot/mongo-test")).projection(fields(excludeId(),exclude("Date"),exclude("AmpTestFiles"))).first();
		/*Remove from database*/
		coll.deleteOne(foundDoc);
        String expectedDocStr = "Document{{RepoSlug=Dspot/mongo-test, AmpOptions=Document{{amplifiers=[None], test-criterion=JacocoCoverageSelector, iteration=1, gregor=false, descartes=true, executeTestParallelWithNumberProcessors=4}}, AmpResult=Document{{resolver/D/ClasspathResolverTest=Document{{initialCoverage=123, ampCoverage=123, totalCoverage=130}}, textresources/D/in/D/sources/D/TestResourcesInSources=Document{{initialCoverage=4, ampCoverage=4, totalCoverage=130}}}}}}";
		
		assertEquals(foundDoc.toString(),expectedDocStr);
	}
}