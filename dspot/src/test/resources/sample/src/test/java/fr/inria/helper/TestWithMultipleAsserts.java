package fr.inria.helper;

import com.google.common.truth.Truth;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class TestWithMultipleAsserts {

    @Test
    public void test() {
        verify(null);
        assertEquals("", "");
        Truth.assertThat("").isEmpty();
        Truth.assertThat("").isEqualTo("");
        Truth.assertThat(0.0F).isNotNaN();
        System.out.println("");
        System.out.println("");
    }

    private void verify(String s) {
        assertNull(s);
    }

}