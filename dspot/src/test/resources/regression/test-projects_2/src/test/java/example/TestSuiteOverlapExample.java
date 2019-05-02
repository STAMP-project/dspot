package example;

public class TestSuiteOverlapExample {

    @org.junit.Test
    public void test1() {
        example.OverlapExample ex = new example.OverlapExample();
        java.lang.String s = "abcd";
        org.junit.Assert.assertEquals('d', ex.charAt(s, ((s.length()) - 1)));
    }

    @org.junit.Test
    public void test2() {
        example.OverlapExample ex = new example.OverlapExample();
        java.lang.String s = "abcd";
        org.junit.Assert.assertEquals('d', ex.charAt(s, ((s.length()) - 1)));
    }
}

