package eu.stamp_project.utils.json;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/03/17
 */
public class ClassTimeJSON {

    public final String fullQualifiedName;
    public final long timeInMs;

    public ClassTimeJSON(String fullQualifiedName, long timeInMs) {
        this.fullQualifiedName = fullQualifiedName;
        this.timeInMs = timeInMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            ClassTimeJSON that = (ClassTimeJSON) o;
            return fullQualifiedName != null ? fullQualifiedName.equals(that.fullQualifiedName) : that.fullQualifiedName == null;
        }
    }

    @Override
    public int hashCode() {
        return this.fullQualifiedName.hashCode();
    }
}
