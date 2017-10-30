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
    public void getReaderWithRootAndResourceHasRelativePath() throws Exception {
        ClasspathResolver underTest = new ClasspathResolver("templates");
        Reader reader = underTest.getReader("absolute_partials_template.html");
        assertNotNull(reader);
    }

    @Test
    public void getReaderWithRootThatHasTrailingForwardSlashAndResourceHasRelativePath() throws Exception {
        ClasspathResolver underTest = new ClasspathResolver("templates/");
        Reader reader = underTest.getReader("absolute_partials_template.html");
        assertNotNull(reader);
    }

    @Test
    public void getReaderWithRootAndResourceHasAbsolutePath() throws Exception {
        ClasspathResolver underTest = new ClasspathResolver("templates");
        Reader reader = underTest.getReader("/absolute_partials_template.html");
        assertNotNull(reader);
    }

    @Test
    public void getReaderWithRootThatHasTrailingForwardSlashAndResourceHasAbsolutePath() throws Exception {
        ClasspathResolver underTest = new ClasspathResolver("templates/");
        Reader reader = underTest.getReader("/absolute_partials_template.html");
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

    @Test
    public void getReaderWithRootAndResourceHasDoubleDotRelativePath() throws Exception {
        ClasspathResolver underTest = new ClasspathResolver("templates");
        Reader reader = underTest.getReader("absolute/../absolute_partials_template.html");
        assertNotNull(reader);
    }

    @Test
    public void getReaderWithRootAndResourceHasDotRelativePath() throws Exception {
        ClasspathResolver underTest = new ClasspathResolver("templates");
        Reader reader = underTest.getReader("absolute/./nested_partials_sub.html");
        assertNotNull(reader);
    }

}