package eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog;

import eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.ObjectLog;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

/**
 * Created by Andrew Bwogi
 * abwogi@kth.se
 * on 26/08/19
 */
public class ArrayObservationTest {

    @Test
    public void testReferenceTypeArrayObservation() {

        /*
        These tests ensure that the observed array is accessed correctly by an appropriate name
        and type. They also ensure that an array access corresponds to an expected observed value.
         */

        testObject[] objectArray = new testObject[]{ new testObject(1, 2), null, new testObjectChild(1,2,3)};
        ObjectLog.log(objectArray, "objectArray", "test");
        Map<String, Observation> observations = ObjectLog.getObservations();
        Map observation = observations.get("test").getObservationValues();
        assertTrue(observation.containsKey("((eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.ArrayObservationTest.testObject[])objectArray)[0].getB()"));
        assertEquals(2,observation.get("((eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.ArrayObservationTest.testObject[])objectArray)[0].getB()"));
        assertTrue(observation.containsKey("(objectArray)[1]"));
        assertNull(observation.get("(objectArray)[1]"));
        assertTrue(observation.containsKey("((eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.ArrayObservationTest.testObjectChild[])objectArray)[2].getB()"));
        assertEquals(2,observation.get("((eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.ArrayObservationTest.testObjectChild[])objectArray)[2].getB()"));
        assertTrue(observation.containsKey("((eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.ArrayObservationTest.testObjectChild[])objectArray)[2].getC()"));
        assertEquals(3,observation.get("((eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.ArrayObservationTest.testObjectChild[])objectArray)[2].getC()"));

        testObject[][][] jaggedObjectArray = {{},
                null,
                {{},{null,new testObject(1, 2)},{}},
                {}};
        ObjectLog.reset();
        ObjectLog.log(jaggedObjectArray, "jaggedObjectArray", "test");
        observations = ObjectLog.getObservations();
        observation = observations.get("test").getObservationValues();
        assertTrue(observation.containsKey("(jaggedObjectArray)[1][0][0]"));
        assertNull(observation.get("(jaggedObjectArray)[1][0][0]"));
        assertTrue(observation.containsKey("(jaggedObjectArray)[2][1][0]"));
        assertNull(observation.get("(jaggedObjectArray)[2][1][0]"));
        assertTrue(observation.containsKey("((eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.ArrayObservationTest.testObject[][][])jaggedObjectArray)[2][1][1].getB()"));
        assertEquals(2,observation.get("((eu.stamp_project.dspot.assertiongenerator.assertiongenerator.methodreconstructor.observer.testwithloggenerator.objectlogsyntaxbuilder_constructs.objectlog.ArrayObservationTest.testObject[][][])jaggedObjectArray)[2][1][1].getB()"));
    }

    public class testObject {
        private int a;
        private int b;

        public testObject(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int a() {
            return this.a;
        }

        public int getB() {
            return this.b;
        }
    }

    public class testObjectChild extends testObject {
        private int c;

        public testObjectChild(int a, int b, int c) {
            super(a,b);
            this.c = c;
        }

        public int getC() {
            return this.c;
        }
    }
}
