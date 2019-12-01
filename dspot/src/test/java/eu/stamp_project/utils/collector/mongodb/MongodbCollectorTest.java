package eu.stamp_project.utils.collector.mongodb;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.assertEquals;

import eu.stamp_project.Main;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

import org.bson.Document;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

import javax.mail.MessagingException;

public class MongodbCollectorTest {

    private GreenMail greenMail;

    @Before
    public void startMailServer() {
        this.greenMail = new GreenMail(ServerSetupTest.SMTP);
        this.greenMail.start();
    }

    @After
    public void stopMailServer() {
        this.greenMail.stop();
    }

    @Test
    public void testInfoSubmissionToMongodbPitMutantScoreSelector() {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--test-criterion", "PitMutantScoreSelector",
                "--test", "fr.inria.sample.TestClassWithoutAssert",
                "--path-pit-result", "src/test/resources/sample/mutations.csv",
                "--gregor-mode",
                "--output-path", "target/trash",
                "--collector", "MongodbCollector",
                "--mongo-url", "mongodb://localhost:27017",
                "--mongo-colname", "AmpTestRecords",
                "--mongo-dbname", "Dspot",
                "--repo-slug", "USER/Testing",
                "--repo-branch", "master",
        });

        MongoClient mongoClient = MongodbCollector.connectToMongo("mongodb://localhost:27017");
        MongoCollection<Document> coll = MongodbCollector.getCollection("AmpTestRecords", MongodbCollector.getDatabase("Dspot", mongoClient));

        Document foundDoc = coll.find(eq("RepoSlug", "USER/Testing")).projection(fields(excludeId(), exclude("Date"), exclude("executeTestParallelWithNumberProcessors"))).first();
        coll.deleteOne(foundDoc);

        Document unwanted = foundDoc.get("AmpOptions", Document.class);
        unwanted.remove("executeTestParallelWithNumberProcessors");
        foundDoc.append("AmpOptions", unwanted);

        String expectedDocStr = "Document{{RepoSlug=USER/Testing, RepoBranch=master, AmpOptions=Document{{amplifiers=[None], test-criterion=PitMutantScoreSelector, iteration=1, gregor=true, descartes=false}}, AmpResult=Document{{fr/D/inria/D/sample/D/TestClassWithoutAssert=Document{{originalKilledMutants=0, NewMutantKilled=67}}, TotalResult=Document{{totalOriginalKilledMutants=0, totalNewMutantKilled=67}}}}}}";

        assertEquals(expectedDocStr, foundDoc.toString());
    }

    @Test
    public void testInfoSubmissionToMongodbJacocoCoverageSelector() {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/project-with-resources/",
                "--test-criterion", "JacocoCoverageSelector",
                "--iteration", "1",
                "--collector", "MongodbCollector",
                "--mongo-url", "mongodb://localhost:27017",
                "--mongo-colname", "AmpTestRecords",
                "--mongo-dbname", "Dspot",
                "--repo-slug", "USER/Testing",
                "--repo-branch", "master"
        });
        MongoClient mongoClient = MongodbCollector.connectToMongo("mongodb://localhost:27017");
        MongoCollection<Document> coll = MongodbCollector.getCollection("AmpTestRecords", MongodbCollector.getDatabase("Dspot", mongoClient));

         Document foundDoc = coll.find(eq("RepoSlug", "USER/Testing")).projection(fields(excludeId(), exclude("Date"), exclude("executeTestParallelWithNumberProcessors"))).first();
        coll.deleteOne(foundDoc);
        Document unwanted = foundDoc.get("AmpOptions", Document.class);
        unwanted.remove("executeTestParallelWithNumberProcessors");
        foundDoc.append("AmpOptions", unwanted);

        String expectedDocStr = "Document{{RepoSlug=USER/Testing, RepoBranch=master, AmpOptions=Document{{amplifiers=[None], test-criterion=JacocoCoverageSelector, iteration=1, gregor=false, descartes=true}}, AmpResult=Document{{resolver/D/ClasspathResolverTest=Document{{totalCoverage=130, initialCoverage=123, ampCoverage=123}}, textresources/D/in/D/sources/D/TestResourcesInSources=Document{{totalCoverage=130, initialCoverage=4, ampCoverage=4}}, TotalResult=Document{{totalCovAcrossAllTests=260, totalInitialCoverage=127, totalAmpCoverage=127}}}}}}";

        assertEquals(expectedDocStr, foundDoc.toString());
    }

    @Test
    /* Should update an existing document then have tried sending an email at the end*/
    public void testRestful() {
        Document initDoc = new Document("RepoSlug", "USER/Testing")
                .append("RepoBranch", "master")
                .append("State", "pending")
                .append("Email", "abc@mail.com");
        Document argsDoc = new Document();
        argsDoc.append("test-criterion", "PitMutantScoreSelector");
        initDoc.append("AmpOptions", argsDoc);

        MongoClient mongoClient = MongodbCollector.connectToMongo("mongodb://localhost:27017");
        MongoCollection<Document> coll = MongodbCollector.getCollection("AmpTestRecords", MongodbCollector.getDatabase("Dspot", mongoClient));
        coll.insertOne(initDoc);

        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--test-criterion", "PitMutantScoreSelector",
                "--test", "fr.inria.sample.TestClassWithoutAssert",
                "--path-pit-result", "src/test/resources/sample/mutations.csv",
                "--gregor-mode",
                "--output-path", "target/trash",
                "--collector", "MongodbCollector",
                "--mongo-url", "mongodb://localhost:27017",
                "--mongo-colname", "AmpTestRecords",
                "--mongo-dbname", "Dspot",
                "--repo-slug", "USER/Testing",
                "--repo-branch", "master",
                "--smtp-host", "localhost",
                "--smtp-port", "3025",
                "--restful"
        });


        Document foundDoc = coll.find(eq("State", "recent")).projection(fields(excludeId(), exclude("Date"), exclude("executeTestParallelWithNumberProcessors"))).first();
        coll.deleteOne(foundDoc);
        Document unwanted = foundDoc.get("AmpOptions", Document.class);
        unwanted.remove("executeTestParallelWithNumberProcessors");
        foundDoc.append("AmpOptions", unwanted);

        String expectedDocStr = "Document{{RepoSlug=USER/Testing, RepoBranch=master, State=recent, Email=abc@mail.com, AmpOptions=Document{{amplifiers=[None], test-criterion=PitMutantScoreSelector, iteration=1, gregor=true, descartes=false}}, AmpResult=Document{{fr/D/inria/D/sample/D/TestClassWithoutAssert=Document{{originalKilledMutants=0, NewMutantKilled=67}}, TotalResult=Document{{totalOriginalKilledMutants=0, totalNewMutantKilled=67}}}}}}";
        assertEquals(expectedDocStr, foundDoc.toString());

        /* Smtp integration test*/
        String sendedMailSubject = "";
        try {
            sendedMailSubject = greenMail.getReceivedMessages()[0].getSubject();
        } catch (MessagingException e) {
        }

        assertEquals("Amplification succeeded", sendedMailSubject);
    }
}
