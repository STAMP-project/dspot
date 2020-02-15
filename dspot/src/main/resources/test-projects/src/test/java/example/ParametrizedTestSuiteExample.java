package example;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/11/18
 */
@RunWith(Parameterized.class)
public class ParametrizedTestSuiteExample {

    private String string;

    public ParametrizedTestSuiteExample(String string) {
        this.string = string;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> strategies() {
        return Arrays.asList(
                new Object[]{
                        "abcd"
                },
                new Object[]{
                        "abcd"
                }
        );
    }

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