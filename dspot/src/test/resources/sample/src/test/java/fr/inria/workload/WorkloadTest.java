package fr.inria.workload;

import org.junit.Test;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 17/07/18
 */
public class WorkloadTest {

    public class Workload {
        private int a = 0;
        public void run(int a) {
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
        workload.run(0);
    }
}
