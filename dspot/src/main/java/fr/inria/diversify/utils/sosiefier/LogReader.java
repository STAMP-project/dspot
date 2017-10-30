package fr.inria.diversify.utils.sosiefier;

import fr.inria.diversify.logger.KeyWord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Simon
 * Date: 30/06/16
 * Time: 10:52
 */
@Deprecated
public class LogReader {
    protected Set<LogParser> parsers;
    protected Map<Integer, String> ids;

    protected final String directory;

    public LogReader(String directory) throws IOException {
        this.directory = directory;
        parsers = new HashSet<>();
        initIds();
    }


    public void readLogs() {
        File dir = new File(directory);

        parsers.stream()
                .forEach(p -> {
                    try {
                        p.setIds(ids);
                        p.init(dir);
                    } catch (Exception e) {}
                });

        Arrays.stream(dir.listFiles())
                .filter(file -> file.isFile())
                .filter(file -> file.getName().startsWith("log"))
                .forEach(file -> {
                    try {
                        readLogFile(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    protected void readLogFile(File file) throws IOException {
        parsers.stream()
                .forEach(p -> p.newLogFile(file));

        BufferedReader br = new BufferedReader(new FileReader(file));
        String logLine = readLogLine(br);
        while (logLine != null) {
            if(!logLine.isEmpty()) {
                for(LogParser parser : parsers) {
                    parser.readLogLine(logLine);
                }
            }
            logLine = readLogLine(br);
        }
    }


    protected String readLogLine(BufferedReader br) throws IOException {
        String line = br.readLine();
        StringBuilder logLine = new StringBuilder();
        while (line != null && !line.endsWith(KeyWord.endLine)) {
            logLine.append(line);
            line = br.readLine();
        }
        if(line != null) {
            logLine.append(line);
            return logLine.substring(0, logLine.length() - 2);
        } else {
            return null;
        }
    }

    protected void initIds() throws IOException {
        ids = new HashMap<>();
        File infoFile = new File(directory + "/info");
        BufferedReader br = new BufferedReader(new FileReader(infoFile));

        String line = br.readLine();
        while (line != null) {
            String[] split = line.split(";");
            if(split[0].equals("id")) {
                Integer id = Integer.parseInt(split[1]);
                String name = split[2];

                ids.put(id, name);
            }
            line = br.readLine();
        }
    }

    public void addParser(LogParser parser) {
        parsers.add(parser);
    }

}
