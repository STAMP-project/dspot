package fr.inria.helper;

import com.google.common.truth.Truth;
import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class TestWithMultipleAsserts {

    @Test
    public void test() {
        verify(null);
        assertEquals("", "");
        String s = "";
        Truth.assertThat(s).isEmpty();
        Truth.assertThat("").isEqualTo("");
        Truth.assertThat(0.0F).isNotNaN();
        System.out.println("");
        System.out.println("");
        then(0).isInstanceOf(int.class).hasSameClassAs(0);
        notVerify();
    }

    private void notVerify() {
        // empty
    }

    private void verify(String s) {
        assertNull(s);
    }

}