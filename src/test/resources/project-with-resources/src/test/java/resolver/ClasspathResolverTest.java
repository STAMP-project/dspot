package resolver;


import org.junit.Test;

import java.io.Reader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ClasspathResolverTest {

    @Test
    public void getReaderNullRootAndResourceHasRelativePath() throws Exception {
        ClasspathResolver underTest = new ClasspathResolver();
        Reader reader = underTest.getReader("nested_partials_template.html");
        assertNotNull(reader);
    }

    @Test
    public void getReaderNullRootDoesNotFindFileWithAbsolutePath() throws Exception {
        ClasspathResolver underTest = new ClasspathResolver();
        Reader reader = underTest.getReader("/nested_partials_template.html");
        assertNull(reader);
    }

    @Test (expected = NullPointerException.class)
    public void getReaderWithRootAndNullResource() throws Exception {
        ClasspathResolver underTest = new ClasspathResolver("templates");
        underTest.getReader(null);
    }

    @Test (expected = NullPointerException.class)
    public void getReaderNullRootAndNullResourceThrowsNullPointer() throws Exception {
        ClasspathResolver underTest = new ClasspathResolver();
        underTest.getReader(null);
    }

}