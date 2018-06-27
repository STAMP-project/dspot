package eu.stamp_project.compare;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/05/17
 */
public class ObjectLogTest {

    @Before
    public void setUp() throws Exception {
        ObjectLog.reset();
    }

    private static class MyInternalClass {
        private static final ArrayList<Integer> list = new ArrayList<>();
        static {
            list.add(1);
            list.add(2);
            list.add(3);
            list.add(4);
            list.add(5);
        }
        private int a;
        private int b;
        private static Random random = new Random();

        public MyInternalClass(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public List<Integer> getObject() {
            return list.subList(0, 2);
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public int compute() {
            return 23 * (a + b) % 42;
        }
        public int random() {
            return random.nextInt();
        }
    }

    public static Integer add(Integer a, Integer b) {
        final MyInternalClass myInternalClass = new MyInternalClass(a, b);
        ObjectLog.log(myInternalClass , "myInternalClass ", "add__0");
        return myInternalClass.compute();
    }

    // TODO we should be able to specify some method, or compute pure method
    @Test
    public void test() throws Exception {
        final Integer result = ObjectLogTest.add(new Integer(3), new Integer(20));
        ObjectLogTest.add(new Integer(3), new Integer(20));
        ObjectLogTest.add(new Integer(3), new Integer(20));
        ObjectLogTest.add(new Integer(3), new Integer(20));
        assertEquals(25, result.intValue());
        assertEquals(1, ObjectLog.getObservations().size());
        final Observation add__0 = ObjectLog.getObservations().get("add__0");
        assertNotNull(add__0);
//        assertEquals(1, add__0.getNotDeterministValues().size());
        final Map<String, Object> observationValues = add__0.getObservationValues();
        assertEquals(3, observationValues.size());
//        assertEquals(25, observationValues.get("(myInternalClass ).compute()"));
        assertEquals(3, observationValues.get("(myInternalClass ).getA()"));
        assertEquals(20, observationValues.get("(myInternalClass ).getB()"));
//        assertTrue(add__0.getNotDeterministValues().contains("(myInternalClass ).random()"));
    }
}
