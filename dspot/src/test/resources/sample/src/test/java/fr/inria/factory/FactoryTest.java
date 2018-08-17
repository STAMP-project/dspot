package fr.inria.factory;

import org.junit.Test;

public class FactoryTest {

    public static class aClass implements aInterface {
        private int x;
        private aClass(int x) {
            this.x = x;
        }
        private aClass(String s) {
            this.x = s.length();
        }
        public int getX() {
            return x;
        }
    }

    public interface aInterface {

    }

    public static aClass createAClass() {
        return new aClass(1);
    }

    public static aClass build(int x) {
        return new aClass(x);
    }

    @Test
    public void test() throws Exception {
        final aClass aClass = createAClass();
    }
}