package fr.inria.workload;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 17/07/18
 */
public class WorkloadTest {

    public class Workload {
        private int a = 0;
        public void run(int a, int b, int c, int d, int e) {
            for (int i = 0 ; i < 10E6 ; i++) {
                a = a + i % 5;
            }
            this.a = a;
        }
        public int getA() {
            return this.a;
        }
    }

    @Test
    public void test() throws Exception {
        final Workload workload = new Workload();
        int s = 0;
        ((fr.inria.workload.WorkloadTest.Workload) workload).run(0, 0, 0, 0, s);
        assertTrue(0 != workload.getA());
        assertEquals(0, s);
    }
}
