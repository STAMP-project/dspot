package resolver;

import java.io.Reader;

/**
 * Resolves mustache resources.
 *
 * @author Simon Buettner
 */
public interface MustacheResolver {

  Reader getReader(String resourceName);

}