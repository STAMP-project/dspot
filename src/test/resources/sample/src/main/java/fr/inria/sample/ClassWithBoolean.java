package fr.inria.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 10:55
 */
public class ClassWithBoolean {

    public Object getNull() {
        return null;
    }

    public boolean getTrue() {
        return true;
    }

    public boolean getFalse() {
        return false;
    }

    public Collection<String> getEmptyList() {
        return new ArrayList<String>();
    }

    public String getString() {
        return "this.is.a.string";
    }

    public char getChar() {
        return 'a';
    }

    public byte getByte() {
        return (byte)1;
    }

    public short getShort() {
        return (short)1;
    }

    public int getInt() {
        return 1;
    }

    public long getLong() {
        return (long)1L;
    }

    public float getFloat() {
        return (float)1.0f;
    }

    public double getDouble() {
        return (double)1.0D;
    }

    public Collection<String> getListWithElements() {
        final Collection<String> strings = new ArrayList<String>();
        strings.add("a");
        strings.add("b");
        return strings;
    }

    public boolean getBoolean() {
        Random r = new Random(23L);
        return r.nextBoolean();
    }
}
