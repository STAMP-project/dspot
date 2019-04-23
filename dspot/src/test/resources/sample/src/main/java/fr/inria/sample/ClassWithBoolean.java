package fr.inria.sample;

import java.util.*;

/**
 * User: Simon
 * Date: 25/11/16
 * Time: 10:55
 */
public class ClassWithBoolean {

    public String getNull() {
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

    public Iterable<String> getEmptyCollectionAsIterable() {
        return new ArrayList<>();
    }

    public class MyIterable implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return null;
        }
    }

    public Iterable<String> getEmptyMyIterable() {
        return new MyIterable();
    }

    public class MyList implements Collection<String> {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<String> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(String s) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends String> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }
    }

    public Iterable<String> getEmptyMyListAsIterable() {
        return new MyList();
    }

    public boolean getBoolean() {
        Random r = new Random(23L);
        return r.nextBoolean();
    }
}
