package fr.inria.infinite;

import org.junit.Test;

public class LoopTest {

    @Test(timeout = 10000L)
    public void testLoop() throws Exception {
        final Loop loop = new Loop();
        loop.infinite();
    }
}