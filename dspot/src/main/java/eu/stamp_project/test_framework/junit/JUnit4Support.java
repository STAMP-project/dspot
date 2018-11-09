package eu.stamp_project.test_framework.junit;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 07/11/18
 */
public class JUnit4Support extends JUnitSupport {

    public JUnit4Support() {
        super("org.junit.Assert");
    }

    @Override
    protected String getFullQualifiedNameOfAssertClass() {
        return null;
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationTest() {
        return "org.junit.Test";
    }

    @Override
    protected String getFullQualifiedNameOfAnnotationIgnore() {
        return "org.junit.Ignore";
    }

}
