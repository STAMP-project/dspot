package textresources.in.sources;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 17/10/17
 */
public class TestResourcesInSources {

    @Test
    public void testGetResources() throws Exception {
        final InputStream resourceAsStream = TestResourcesInSources.class.getResourceAsStream("resources.txt");
        assertNotNull(resourceAsStream);
        assertNotNull(ResourcesInSources.getResource());
    }
}
