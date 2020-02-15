

package example;


public class TestSuiteExample2 {

    private static int integer = example.TestResources.integer;

    @org.junit.Test
    public void test3() {
        example.Example ex = new example.Example();
        java.lang.String s = "abcd";
        org.junit.Assert.assertEquals('d', ex.charAt(s, ((s.length()) - 1)));
    }

    @org.junit.Test
    public void test4() {
        example.Example ex = new example.Example();
        java.lang.String s = "abcd";
        org.junit.Assert.assertEquals('d', ex.charAt(s, 12));
    }

    @org.junit.Test
    public void test7() {
        example.Example ex = new example.Example();
        org.junit.Assert.assertEquals('c', ex.charAt("abcd", 2));
    }

    @org.junit.Test
    public void test8() {
        example.Example ex = new example.Example();
        org.junit.Assert.assertEquals('b', ex.charAt("abcd", 1));
    }

    @org.junit.Test
    public void test9() {
        example.Example ex = new example.Example();
        org.junit.Assert.assertEquals('f', ex.charAt("abcdefghijklm", 5));
    }

    @org.junit.Test
    public void test2() {
        example.Example ex = new example.Example();
        org.junit.Assert.assertEquals('d', ex.charAt("abcd", 3));
    }
}

