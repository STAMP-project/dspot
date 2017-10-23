package fr.inria.diversify.utils.sosiefier;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * User: Simon
 * Date: 30/06/16
 * Time: 11:04
 */
@Deprecated
public abstract class LogParser<T> {
    protected T result;
    protected Map<Integer, String> ids;

    public T getResult() {
        return result;
    }

    public abstract void readLogLine(String logLine);

    public abstract void init(File dir) throws IOException;

    public void setIds(Map<Integer, String> ids) {
        this.ids = ids;
    }

    public abstract void newLogFile(File file);
}
