package eu.stamp_project.mutant.pit;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by Andrew Bwogi
 * abwogi@kth.se
 * on 14/11/18
 */

/*


TODO: look at how PirXMLResult objects actually encodes

TODO: implement getPathOfMutationsCsvFile and parseAndDelete
TODO: represent methodDescription properly

TODO: abstract csv parser
TODO: abstract csv pitresult

TODO: abstract test?

TODO: use utf8?

 */
public class PitXMLResultParser extends AbstractParser {

    private static final String PATH_TO_MUTATIONS_RESULT = "/mutations.xml";

    private static File getPathOfMutationsCsvFile(String pathToDirectoryResults) {
        return getPathOfMutationsCsvFile(pathToDirectoryResults,PATH_TO_MUTATIONS_RESULT);
    }

    public static List<PitXMLResult> parseAndDelete(String pathToDirectoryResults) {
        final File fileResults = PitXMLResultParser.getPathOfMutationsCsvFile(pathToDirectoryResults);
        final List<PitXMLResult> results = PitXMLResultParser.parse(fileResults);
        try {
            FileUtils.deleteDirectory(new File(pathToDirectoryResults));
        } catch (IOException e) {
            // ignored
        }
        return results;
    }

    public static List<PitXMLResult> parse(File fileResults) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            class Handler extends DefaultHandler {
                private StringBuilder stringBuilder;
                final List<PitXMLResult> results = new ArrayList<>();
                String sourceFile, methodDescription, mutatedClass, mutatedMethod, mutator, killingTest, description,
                        fullQualifiedNameMethod, fullQualifiedNameClass;
                int numberOfTestsRun, lineNumber, index, block;
                AbstractPitResult.State state;
                boolean detected;
                boolean startElement = false;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    startElement = true;
                    stringBuilder = new StringBuilder();
                    if (qName.equalsIgnoreCase("mutation")) {
                        detected = Boolean.parseBoolean(attributes.getValue("detected"));
                        numberOfTestsRun = Integer.parseInt(attributes.getValue("numberOfTestsRun"));
                        try {
                            state = AbstractPitResult.State.valueOf(attributes.getValue("status"));
                        } catch (Exception e) {
                            state = AbstractPitResult.State.NO_COVERAGE;
                        }
                    }
                }

                @Override
                public void characters(char ch[], int start, int length) {
                    stringBuilder.append(new String(ch, start, length));
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    if (qName.equalsIgnoreCase("sourceFile"))
                        sourceFile = stringBuilder.toString();
                    if (qName.equalsIgnoreCase("mutatedClass"))
                        mutatedClass = stringBuilder.toString();
                    if (qName.equalsIgnoreCase("mutatedMethod"))
                        mutatedMethod = stringBuilder.toString();
                    if (qName.equalsIgnoreCase("methodDescription"))
                        methodDescription = stringBuilder.toString();
                    if (qName.equalsIgnoreCase("lineNumber"))
                        lineNumber = Integer.parseInt(stringBuilder.toString());
                    if (qName.equalsIgnoreCase("mutator"))
                        mutator = stringBuilder.toString();
                    if (qName.equalsIgnoreCase("index"))
                        index = Integer.parseInt(stringBuilder.toString());
                    if (qName.equalsIgnoreCase("block"))
                        block = Integer.parseInt(stringBuilder.toString());
                    if (qName.equalsIgnoreCase("killingTest"))
                        killingTest = stringBuilder.toString();
                    if (qName.equalsIgnoreCase("description"))
                        description = stringBuilder.toString();
                    if (qName.equalsIgnoreCase("mutation")) {
                        if (killingTest.trim().equals("")) {
                            fullQualifiedNameMethod = "none";
                            fullQualifiedNameClass = "none";
                        } else {
                            final String[] nameOfTheKiller = killingTest.split("\\(");
                            if (nameOfTheKiller.length > 1) {
                                fullQualifiedNameMethod = nameOfTheKiller[0];
                                fullQualifiedNameClass = nameOfTheKiller[1].substring(0, nameOfTheKiller[1].length() - 1);
                            } else {
                                fullQualifiedNameMethod = "none";
                                fullQualifiedNameClass = nameOfTheKiller[0].substring(0, nameOfTheKiller[0].length() / 2);
                            }
                        }
                        results.add(new PitXMLResult(mutatedClass, state,
                                mutator, fullQualifiedNameMethod, fullQualifiedNameClass,
                                lineNumber, mutatedMethod, methodDescription, description, index, block,
                                numberOfTestsRun, detected));
                    }
                    startElement = false;
                }

                public List<PitXMLResult> getResults() {
                    return results;
                }
            }
            Handler handler = new Handler();
            InputStream inputStream = new FileInputStream(fileResults);
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");
            saxParser.parse(is, handler);
            return handler.getResults();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
