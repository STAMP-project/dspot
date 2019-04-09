package eu.stamp_project.resources;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 04/02/19
 */
public class AmplifiedTest {

    public class MyObject {
        public MyInternalObject getMyInternalObject() {
            return new MyInternalObject();
        }

        public int getInt() {
            return 0;
        }
    }

    public class MyInternalObject {
        public int getInt() {
            return 0;
        }

        public MySecondInternalObject getMySecondIntegernalObject() {
            return new MySecondInternalObject();
        }
    }

    public class MySecondInternalObject extends MyObject {
        public MyObject getObject() {
            return this;
        }

        public int getSecondInt() {
            return 0;
        }
    }

    public int getInt() {
        return 0;
    }

    public Integer getInteger() {
        return 0;
    }

    @Test
    public void redundantCast() {
        final AmplifiedTest amplifiedTest = new AmplifiedTest();
        final MyObject myObject = new MyObject();

        // should be removed
        assertEquals(0, (int) ((AmplifiedTest) amplifiedTest).getInt());
        assertEquals(0, (int) amplifiedTest.getInt());
        assertEquals(0, (int) ((MyObject) myObject).getInt());
        assertEquals(0, (int) ((MyInternalObject) ((MyObject) myObject).getMyInternalObject()).getInt());
        assertEquals(0, (int) ((MySecondInternalObject) ((MyInternalObject) ((MyObject) myObject).getMyInternalObject()).getMySecondIntegernalObject()).getInt());
        assertEquals(0, (int) ((MyObject) ((MyInternalObject) ((MyObject) myObject).getMyInternalObject()).getMySecondIntegernalObject()).getInt());

        // should not be removed
        assertEquals(0, ((MySecondInternalObject) myObject.getMyInternalObject().getMySecondIntegernalObject().getObject()).getSecondInt());
    }
}
