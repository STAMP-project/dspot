package textresources.in.sources;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 17/10/17
 */
public class ResourcesInSources {

    public static Object getResource() {
        return ResourcesInSources.class.getResourceAsStream("resources.txt");
    }

}
