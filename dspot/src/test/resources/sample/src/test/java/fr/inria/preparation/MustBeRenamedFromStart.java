package fr.inria.preparation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/08/18
 */
public class MustBeRenamedFromStart {

    public class Inner {
        public String getName() {
            return Inner.class.getTypeName();
        }
        @Override
        public int hashCode() {
            return this.getName().hashCode();
        }
    }

    @Test
    public void test() {
        final Inner inner = new Inner();
        assertEquals("fr.inria.preparation.MustBeRenamedFromStart$Inner", inner.getName());
    }

}
