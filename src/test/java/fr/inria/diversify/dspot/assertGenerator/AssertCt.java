package fr.inria.diversify.dspot.assertGenerator;

import spoon.reflect.declaration.CtMethod;

import static junit.framework.Assert.assertTrue;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 11:55
 */
public class AssertCt {

    public static void assertBodyEquals(CtMethod mth1, CtMethod mth2) {
        String body1 = mth1.getBody().toString().replace("(", "").replace(")", "").trim();
        String body2 = mth2.getBody().toString().replace("(", "").replace(")", "").trim();
        assertTrue(mth1.getBody().equals(mth2.getBody()) || body1.equals(body2));
    }
}
