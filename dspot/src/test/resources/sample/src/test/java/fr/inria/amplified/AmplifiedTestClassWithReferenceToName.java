package fr.inria.amplified;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/09/18
 */
public class AmplifiedTestClassWithReferenceToName {

    @Test
    public void test() throws Exception {
        Class<?> clazz = AmplifiedTestClassWithReferenceToName.class;
        assertEquals("AmplifiedTestClassWithReferenceToName", clazz.getName());
    }
}
