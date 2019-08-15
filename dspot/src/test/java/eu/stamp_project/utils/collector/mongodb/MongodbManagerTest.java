package eu.stamp_project.utils.collector.mongodb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import eu.stamp_project.Main;
import eu.stamp_project.utils.collector.mongodb.MongodbManager;

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
                        "--mongo-url","mongodb://localhost:27017"
                });

        	/*Fetch submitted document*/
        	MongoClient mongoClient = MongodbManager.connectToMongo("mongodb://localhost:27017");
        	MongoCollection<Document> coll = MongodbManager.getCollection("AmpTestRecords",MongodbManager.getDatabase("Dspot",mongoClient));

        	Document foundDoc = coll.find(eq("RepoSlug","USER/Testing")).projection(fields(excludeId(),exclude("Date"),exclude("executeTestParallelWithNumberProcessors"))).first();
        	/*Remove from database*/
        	coll.deleteOne(foundDoc);

        	Document unwanted = foundDoc.get("AmpOptions",Document.class);
        	unwanted.remove("executeTestParallelWithNumberProcessors");
        	foundDoc.append("AmpOptions",unwanted);

        	String expectedDocStr = "Document{{RepoSlug=USER/Testing, RepoBranch=master, AmpOptions=Document{{amplifiers=[None], test-criterion=PitMutantScoreSelector, iteration=3, gregor=true, descartes=true}}, AmpResult=Document{{fr/D/inria/D/sample/D/TestClassWithoutAssert=Document{{OriginalKilledMutants=0, NewMutantKilled=67}}, TotalResult=Document{{totalOriginalKilledMutants=0, totalNewMutantKilled=67}}}}}}";

                assertEquals(foundDoc.toString(),expectedDocStr);
	}

	@Test
	public void testInfoSubmissionToMongodbJacocoCoverageSelector() {
                Main.main(new String[]{
                        "--path-to-properties", "src/test/resources/project-with-resources/project-with-resources.properties",
                        "--test-criterion", "JacocoCoverageSelector",
                        "--iteration", "1",
                        "--mongo-url","mongodb://localhost:27017"
                });
                MongoClient mongoClient = MongodbManager.connectToMongo("mongodb://localhost:27017");
        	MongoCollection<Document> coll = MongodbManager.getCollection("AmpTestRecords",MongodbManager.getDatabase("Dspot",mongoClient));

        	Document foundDoc = coll.find(eq("RepoSlug","USER/Testing")).projection(fields(excludeId(),exclude("Date"),exclude("executeTestParallelWithNumberProcessors"))).first();
        	coll.deleteOne(foundDoc);

        	Document unwanted = foundDoc.get("AmpOptions",Document.class);
        	unwanted.remove("executeTestParallelWithNumberProcessors");
        	foundDoc.append("AmpOptions",unwanted);

                String expectedDocStr = "Document{{RepoSlug=USER/Testing, RepoBranch=master, AmpOptions=Document{{amplifiers=[None], test-criterion=JacocoCoverageSelector, iteration=1, gregor=false, descartes=true}}, AmpResult=Document{{fr/D/inria/D/sample/D/TestClassWithoutAssert=Document{{OriginalKilledMutants=0, NewMutantKilled=67}}, resolver/D/ClasspathResolverTest=Document{{totalCoverage=130, InitialCoverage=123, AmpCoverage=123}}, textresources/D/in/D/sources/D/TestResourcesInSources=Document{{totalCoverage=130, InitialCoverage=4, AmpCoverage=4}}, TotalResult=Document{{totalOriginalKilledMutants=0, totalNewMutantKilled=67, totalCovAcrossAllTests=260, totalInitialCoverage=127, totalAmpCoverage=127}}}}}}";

		assertEquals(foundDoc.toString(),expectedDocStr);
	}
}