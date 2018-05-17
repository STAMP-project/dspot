package eu.stamp_project.utils.json;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/03/17
 */
public class ProjectTimeJSON {

    public final Set<ClassTimeJSON> classTimes = new LinkedHashSet<>();
    public final String projectName;

    public ProjectTimeJSON(String projectName) {
        this.projectName = projectName;
    }

    public void add(ClassTimeJSON classTimeJSON) {
        if (this.classTimes.contains(classTimeJSON)) {
            this.classTimes.remove(classTimeJSON);
        }
        this.classTimes.add(classTimeJSON);
    }

}
