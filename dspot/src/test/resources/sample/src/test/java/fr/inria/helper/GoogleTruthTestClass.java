package fr.inria.helper;

import static com.google.common.truth.Truth.*;

import org.junit.Test;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/11/18
 */
public class GoogleTruthTestClass {


    @Test
    public void test() {
        String s = "";
        assertThat(s).isEmpty();
        assertThat("").isEqualTo("");
        assertThat("").isNotEqualTo(" ");
        assertThat(0.0F).isNotNaN();
        assertThat(true).isTrue();
    }
}
