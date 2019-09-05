package eu.stamp_project.utils.collector.mongodb;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import eu.stamp_project.Main;
import eu.stamp_project.utils.collector.mongodb.MongodbCollector;
import eu.stamp_project.utils.smtp.EmailSender;

import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.*;
import org.bson.Document;

public class MongodbCollectorTest {

        @Test
        public void testInfoSubmissionToMongodbPitMutantScoreSelector() {
                Main.main(new String[]{
                        "--path-to-properties", "src/test/resources/sample/sample.properties",
                        "--test-criterion", "PitMutantScoreSelector",
                        "--test", "fr.inria.sample.TestClassWithoutAssert",
                        "--path-pit-result", "src/test/resources/sample/mutations.csv",
                        "--gregor",
                        "--output-path", "target/trash",
                        "--collector","MongodbCollector",
                        "--mongo-url","mongodb://localhost:27017",
                        "--mongo-colname","AmpTestRecords",
                        "--mongo-dbname","Dspot",
                        "--repo-slug","USER/Testing",
                        "--repo-branch","master"
                });

                MongoClient mongoClient = MongodbCollector.connectToMongo("mongodb://localhost:27017");
                MongoCollection<Document> coll = MongodbCollector.getCollection("AmpTestRecords",MongodbCollector.getDatabase("Dspot",mongoClient));

                Document foundDoc = coll.find(eq("RepoSlug","USER/Testing")).projection(fields(excludeId(),exclude("Date"),exclude("executeTestParallelWithNumberProcessors"))).first();
                coll.deleteOne(foundDoc);

                Document unwanted = foundDoc.get("AmpOptions",Document.class);
                unwanted.remove("executeTestParallelWithNumberProcessors");
                foundDoc.append("AmpOptions",unwanted);

                String expectedDocStr = "Document{{RepoSlug=USER/Testing, RepoBranch=master, AmpOptions=Document{{amplifiers=[None], test-criterion=PitMutantScoreSelector, iteration=3, gregor=true, descartes=true}}, AmpResult=Document{{fr/D/inria/D/sample/D/TestClassWithoutAssert=Document{{originalKilledMutants=0, NewMutantKilled=67}}, TotalResult=Document{{totalOriginalKilledMutants=0, totalNewMutantKilled=67}}}}}}";

                assertEquals(foundDoc.toString(),expectedDocStr);
        }

        @Test
        public void testInfoSubmissionToMongodbJacocoCoverageSelector() {
                Main.main(new String[]{
                        "--path-to-properties", "src/test/resources/project-with-resources/project-with-resources.properties",
                        "--test-criterion", "JacocoCoverageSelector",
                        "--iteration", "1",
                        "--collector","MongodbCollector",
                        "--mongo-url","mongodb://localhost:27017",
                        "--mongo-colname","AmpTestRecords",
                        "--mongo-dbname","Dspot",
                        "--repo-slug","USER/Testing",
                        "--repo-branch","master"
                });
                MongoClient mongoClient = MongodbCollector.connectToMongo("mongodb://localhost:27017");
                MongoCollection<Document> coll = MongodbCollector.getCollection("AmpTestRecords",MongodbCollector.getDatabase("Dspot",mongoClient));

                Document foundDoc = coll.find(eq("RepoSlug","USER/Testing")).projection(fields(excludeId(),exclude("Date"),exclude("executeTestParallelWithNumberProcessors"))).first();
                coll.deleteOne(foundDoc);
                Document unwanted = foundDoc.get("AmpOptions",Document.class);
                unwanted.remove("executeTestParallelWithNumberProcessors");
                foundDoc.append("AmpOptions",unwanted);

                String expectedDocStr = "Document{{RepoSlug=USER/Testing, RepoBranch=master, AmpOptions=Document{{amplifiers=[None], test-criterion=JacocoCoverageSelector, iteration=1, gregor=false, descartes=true}}, AmpResult=Document{{resolver/D/ClasspathResolverTest=Document{{totalCoverage=130, initialCoverage=123, ampCoverage=123}}, textresources/D/in/D/sources/D/TestResourcesInSources=Document{{totalCoverage=130, initialCoverage=4, ampCoverage=4}}, TotalResult=Document{{totalCovAcrossAllTests=260, totalInitialCoverage=127, totalAmpCoverage=127}}}}}}";

                assertEquals(foundDoc.toString(),expectedDocStr);
        }

        @Test 
        /* Should update an existing document then have tried sending an email at the end*/
        public void testRestful() {
                Document initDoc = new Document("RepoSlug", "USER/Testing")
                                .append("RepoBranch", "master")
                                .append("State","pending")
                                .append("Email","abc@mail.com");
                Document argsDoc = new Document();
                argsDoc.append("test-criterion","PitMutantScoreSelector");
                initDoc.append("AmpOptions",argsDoc);

                MongoClient mongoClient = MongodbCollector.connectToMongo("mongodb://localhost:27017");
                MongoCollection<Document> coll = MongodbCollector.getCollection("AmpTestRecords",MongodbCollector.getDatabase("Dspot",mongoClient));
                coll.insertOne(initDoc);

                Main.main(new String[]{
                        "--path-to-properties", "src/test/resources/sample/sample.properties",
                        "--test-criterion", "PitMutantScoreSelector",
                        "--test", "fr.inria.sample.TestClassWithoutAssert",
                        "--path-pit-result", "src/test/resources/sample/mutations.csv",
                        "--gregor",
                        "--output-path", "target/trash",
                        "--collector","MongodbCollector",
                        "--mongo-url","mongodb://localhost:27017",
                        "--mongo-colname","AmpTestRecords",
                        "--mongo-dbname","Dspot",
                        "--repo-slug","USER/Testing",
                        "--repo-branch","master",
                        "--smtp-host","mail.smtpbucket.com",
                        "--smtp-port","8025",
                        "--restful"
                });

                Document foundDoc = coll.find(eq("State","recent")).projection(fields(excludeId(),exclude("Date"),exclude("executeTestParallelWithNumberProcessors"))).first();
                coll.deleteOne(foundDoc);
                Document unwanted = foundDoc.get("AmpOptions",Document.class);
                unwanted.remove("executeTestParallelWithNumberProcessors");
                foundDoc.append("AmpOptions",unwanted);

                String expectedDocStr = "Document{{RepoSlug=USER/Testing, RepoBranch=master, State=recent, Email=abc@mail.com, AmpOptions=Document{{amplifiers=[None], test-criterion=PitMutantScoreSelector, iteration=3, gregor=true, descartes=true}}, AmpResult=Document{{fr/D/inria/D/sample/D/TestClassWithoutAssert=Document{{originalKilledMutants=0, NewMutantKilled=67}}, TotalResult=Document{{totalOriginalKilledMutants=0, totalNewMutantKilled=67}}}}}}";
                assertEquals(foundDoc.toString(),expectedDocStr);

                assertTrue(EmailSender.getInstance().checkIfEmailSendedWithoutException()); // We should have failed sending the email since we did not provide a working username and password.
        }
}