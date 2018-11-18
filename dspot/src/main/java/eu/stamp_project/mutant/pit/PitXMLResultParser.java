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

TODO: implement getPathOfMutationsCsvFile and parseAndDelete
TODO: represent methodDescription properly
TODO: look at how PirXMLResult objects actually encodes

TODO: abstract csv parser
TODO: abstract csv pitresult

TODO: abstract test?

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
                final List<PitXMLResult> results = new ArrayList<>();
                AbstractPitResult.State state;
                boolean bmethodDescription, blineNumber, bindex, bblock, bsourceFile, bmutatedClass, bmutatedMethod,
                        bmutator, bkillingTest, bdescription = false;
                boolean detected;
                int numberOfTestsRun, lineNumber, index, block;
                String sourceFile, methodDescription, mutatedClass, mutatedMethod, mutator, killingTest, description,
                        fullQualifiedNameMethod, fullQualifiedNameClass;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    if (qName.equalsIgnoreCase("mutation")) {
                        detected = Boolean.parseBoolean(attributes.getValue("detected"));
                        numberOfTestsRun = Integer.parseInt(attributes.getValue("numberOfTestsRun"));
                        try {
                            state = AbstractPitResult.State.valueOf(attributes.getValue("status"));
                        } catch (Exception e) {
                            state = AbstractPitResult.State.NO_COVERAGE;
                        }
                    }
                    if (qName.equalsIgnoreCase("sourceFile"))
                        bsourceFile = true;
                    if (qName.equalsIgnoreCase("mutatedClass"))
                        bmutatedClass = true;
                    if (qName.equalsIgnoreCase("mutatedMethod"))
                        bmutatedMethod = true;
                    if (qName.equalsIgnoreCase("methodDescription"))
                        bmethodDescription = true;
                    if (qName.equalsIgnoreCase("lineNumber"))
                        blineNumber = true;
                    if (qName.equalsIgnoreCase("mutator"))
                        bmutator = true;
                    if (qName.equalsIgnoreCase("index"))
                        bindex = true;
                    if (qName.equalsIgnoreCase("block"))
                        bblock = true;
                    if (qName.equalsIgnoreCase("killingTest"))
                        bkillingTest = true;
                    if (qName.equalsIgnoreCase("description"))
                        bdescription = true;
                }

                @Override
                public void characters(char ch[], int start, int length) {
                    if (bsourceFile) {
                        sourceFile = new String(ch, start, length);
                        bsourceFile = false;
                    }
                    if (bmutatedClass) {
                        mutatedClass = new String(ch, start, length);
                        bmutatedClass = false;
                    }
                    if (bmutatedMethod) {
                        mutatedMethod = new String(ch, start, length);
                        bmutatedMethod = false;
                    }
                    if (bmethodDescription) {
                        methodDescription = new String(ch, start, length);
                        bmethodDescription = false;
                    }
                    if (blineNumber) {
                        lineNumber = Integer.parseInt(new String(ch, start, length));
                        blineNumber = false;
                    }
                    if (bmutator) {
                        mutator = new String(ch, start, length);
                        bmutator = false;
                    }
                    if (bindex) {
                        index = Integer.parseInt(new String(ch, start, length));
                        bindex = false;
                    }
                    if (bblock) {
                        block = Integer.parseInt(new String(ch, start, length));
                        bblock = false;
                    }
                    if (bkillingTest) {
                        killingTest = new String(ch, start, length);
                        bkillingTest = false;
                    }
                    if (bdescription) {
                        description = new String(ch, start, length);
                        bdescription = false;
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    if (qName.equalsIgnoreCase("mutation")) {
                        if (killingTest.trim().equals("none")) {
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
