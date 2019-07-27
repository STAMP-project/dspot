package eu.stamp_project.mongodb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import eu.stamp_project.Main;

public class MongodbManagerTest {

	@Test
	public void testMain() {
		Main.main(new String[]{
                "--clean",
                "--verbose",
                "--path-to-properties", "src/test/resources/mongo-test/dspot.properties",
                "--test-criterion", "TakeAllSelector",
                "--mongo-url","mongodb://localhost:27017",
                "--mongo-colname","AmpTestRecords",
                "--repo-slug","Dspot/mongo-test"
        });


        assertTrue(true);
	}

}