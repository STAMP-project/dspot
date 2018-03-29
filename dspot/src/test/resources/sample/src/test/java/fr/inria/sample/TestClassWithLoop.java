package fr.inria.sample;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 29/03/18
 */
public class TestClassWithLoop {

    @Test
    public void test() throws Exception {
        int[] array = new int[]{1, 2, 3, 4, 5};
        // with bracket
        for (int i = 0; i < array.length; i++) {
            assertEquals(i+1, array[i]);
            array[i] += 1;
        }
        // without bracket
        for (int i = 0; i < array.length; i++)
            assertEquals(i+2, array[i]);
    }

    class MyClass {
        private int internal = 0;
        public int getInteger() {
            return this.internal;
        }
        public void inc() {
            this.internal++;
        }
    }

    @Test
    public void test2() throws Exception {
        List<MyClass> list = new ArrayList<>();
        list.add(new MyClass());
        list.add(new MyClass());
        list.add(new MyClass());

        for (MyClass myClass : list) {
            assertEquals(0, myClass.getInteger());
        }

        for (MyClass myClass : list) {
            myClass.inc();
        }

        for (MyClass myClass : list) {
            assertEquals(1, myClass.getInteger());
        }
    }
}
