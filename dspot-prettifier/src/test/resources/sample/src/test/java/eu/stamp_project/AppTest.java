package eu.stamp_project;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppTest {

    @Test
    public void test1() {
        App app = new App(4);
        assertEquals(4, app.getInt());
        app.compute();
        assertEquals(8, app.getInt());
        app.compute(10);
        assertEquals(40, app.getInt());
        app.compute(3);
        assertEquals(60, app.getInt());
    }
}