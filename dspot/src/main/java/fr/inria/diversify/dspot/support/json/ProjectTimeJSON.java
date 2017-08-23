package fr.inria.diversify.dspot.support.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 22/03/17
 */
public class ProjectTimeJSON {

    public final List<ClassTimeJSON> classTimes = new ArrayList<>();
    public final String projectName;

    public ProjectTimeJSON(String projectName) {
        this.projectName = projectName;
    }

    public void add(ClassTimeJSON classTimeJSON) {
        this.classTimes.add(classTimeJSON);
    }

}
