package fr.inria.testrunner;

public class ClassWithVisibility {

    private String summaryObjectStartText = "<";

    public int method() {
        return 1;
    }

    protected int methodp() {
        return 1;
    }

    /**
     * <p>Sets the start text to output when an <code>Object</code> is
     * output in summary mode.</p>
     *
     * <p>This is output before the size value.</p>
     *
     * <p><code>null</code> is accepted, but will be converted to
     * an empty String.</p>
     *
     * @param summaryObjectStartText  the new start of summary text
     */
    protected void setSummaryObjectStartText(String summaryObjectStartText) {
        if (summaryObjectStartText == null) {
            summaryObjectStartText = "";
        }
        this.summaryObjectStartText = summaryObjectStartText;
    }

    /**
     * <p>Gets the start text to output when an <code>Object</code> is
     * output in summary mode.</p>
     *
     * <p>This is output before the size value.</p>
     *
     * @return the current start of summary text
     */
    protected String getSummaryObjectStartText() {
        return summaryObjectStartText;
    }


}