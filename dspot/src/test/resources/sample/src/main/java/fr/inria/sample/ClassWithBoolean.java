package fr.inria.sample;

import java.util.Random;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 10:55
 */
public class ClassWithBoolean {


    public boolean getTrue() {
        return true;
    }

    public boolean getFalse() {
        return false;
    }

    public boolean getBoolean() {
        Random r = new Random(23L);
        return r.nextBoolean();
    }
}
