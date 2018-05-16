package eu.stamp_project.dspot.selector.json.mutant;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 2/17/17
 */
public class MutantJSON {

    private String ID;
    private int lineNumber;
    private String locationMethod;

    public MutantJSON(String ID, int lineNumber, String locationMethod) {
        this.ID = ID;
        this.lineNumber = lineNumber;
        this.locationMethod = locationMethod;
    }

    public String getID() {
        return ID;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLocationMethod() {
        return locationMethod;
    }
}
