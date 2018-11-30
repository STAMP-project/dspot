package eu.stamp_project.utils.pit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by Andrew Bwogi
 * abwogi@kth.se
 * on 14/11/18
 */
public class PitXMLResultParser extends AbstractParser {

    public PitXMLResultParser(){
        super("/mutations.xml");
    }

    public List<AbstractPitResult> parse(File fileResults) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            class Handler extends DefaultHandler {
                private StringBuilder stringBuilder;
                final List<AbstractPitResult> results = new ArrayList<>();
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
                    startElement = false;
                    if (qName.equalsIgnoreCase("sourceFile")) {
                        sourceFile = stringBuilder.toString();
                    } else if (qName.equalsIgnoreCase("mutatedClass")) {
                        mutatedClass = stringBuilder.toString();
                    } else if (qName.equalsIgnoreCase("mutatedMethod")) {
                        mutatedMethod = stringBuilder.toString();
                    } else if (qName.equalsIgnoreCase("methodDescription")) {
                        methodDescription = stringBuilder.toString();
                    } else if (qName.equalsIgnoreCase("lineNumber")) {
                        lineNumber = Integer.parseInt(stringBuilder.toString());
                    } else if (qName.equalsIgnoreCase("mutator")) {
                        mutator = stringBuilder.toString();
                    } else if (qName.equalsIgnoreCase("index")) {
                        index = Integer.parseInt(stringBuilder.toString());
                    } else if (qName.equalsIgnoreCase("block")) {
                        block = Integer.parseInt(stringBuilder.toString());
                    } else if (qName.equalsIgnoreCase("killingTest")) {
                        killingTest = stringBuilder.toString();
                    } else if (qName.equalsIgnoreCase("description")) {
                        description = stringBuilder.toString();
                    } else if (qName.equalsIgnoreCase("mutation")) {
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
                }

                public List<AbstractPitResult> getResults() {
                    return results;
                }
            }
            Handler handler = new Handler();
            saxParser.parse(fileResults, handler);
            return handler.getResults();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
