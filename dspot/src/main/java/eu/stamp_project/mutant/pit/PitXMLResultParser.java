package eu.stamp_project.mutant.pit;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;







import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by Andrew Bwogi
 * abwogi@kth.se
 * on 14/11/18
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

        /*
        try {
            final List<PitXMLResult> results = new ArrayList<>();

            File fXmlFile = new File("/Users/mkyong/staff.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            NodeList mutations = doc.getElementsByTagName("mutation");


            for (int i = 0; i < mutations.getLength(); i++) {

                Node mutation = mutations.item(i);


                AbstractPitResult.State state;


                if (mutation.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) mutation;

                    Boolean detected = Boolean.parseBoolean(element.getAttribute("detected"));
                    int numberOfTestsRun = Integer.parseInt(element.getAttribute("numberOfTestsRun"));

                    try {
                        state = AbstractPitResult.State.valueOf(element.getAttribute("status"));
                    } catch (Exception e) {
                        state = AbstractPitResult.State.NO_COVERAGE;
                    }
                    String fullQualifiedNameOfMutatedClass = element.getElementsByTagName("mutatedClass").item(0).getTextContent();
                    String fullQualifiedNameMutantOperator = element.getElementsByTagName("mutator").item(0).getTextContent();
                    String nameOfLocalisation = element.getElementsByTagName("mutatedMethod").item(0).getTextContent();
                    int lineNumber = Integer.parseInt(element.getElementsByTagName("lineNumber").item(0).getTextContent());
                    String methodDescription = element.getElementsByTagName("methodDescription").item(0).getTextContent();
                    String mutationDescription = element.getElementsByTagName("description").item(0).getTextContent();
                    int index = Integer.parseInt(element.getElementsByTagName("index").item(0).getTextContent());
                    int block = Integer.parseInt(element.getElementsByTagName("block").item(0).getTextContent());

                    // todo add killer?
                    String fullQualifiedNameMethod = element.getElementsByTagName("firstname").item(0).getTextContent();
                    String fullQualifiedNameClass = element.getElementsByTagName("firstname").item(0).getTextContent();

                    results.add(new PitXMLResult(fullQualifiedNameOfMutatedClass, state, fullQualifiedNameMutantOperator, fullQualifiedNameMethod, fullQualifiedNameClass,
                    lineNumber, nameOfLocalisation, methodDescription, mutationDescription, index, block, numberOfTestsRun, detected));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/


















        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DefaultHandler handler = new DefaultHandler() {
                final List<PitXMLResult> results = new ArrayList<>();
                AbstractPitResult.State state;
                boolean bmethodDescription, blineNumber, bindex, bblock, bsourceFile, bmutatedClass, bmutatedMethod,
                 bmutator, bkillingTest, bdescription = false;
                boolean detected;
                int numberOfTestsRun, lineNumber, index, block;
                String sourceFile, methodDescription, mutatedClass, mutatedMethod, mutator, killingTest, description;

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

                public void characters(char ch[], int start, int length) {
                    if (bsourceFile) {
                        sourceFile = new String(ch, start, length);
                        bsourceFile = false; }
                    if (bmutatedClass) {
                        mutatedClass = new String(ch, start, length);
                        bmutatedClass = false; }
                    if (bmutatedMethod) {
                        mutatedMethod = new String(ch, start, length);
                        bmutatedMethod = false; }
                    if (bmethodDescription) {
                        methodDescription = new String(ch, start, length);
                        bmethodDescription = false; }
                    if (blineNumber) {
                        lineNumber = Integer.parseInt(new String(ch, start, length));
                        blineNumber = false; }
                    if (bmutator) {
                        mutator = new String(ch, start, length);
                        bmutator = false; }
                    if (bindex) {
                        index = Integer.parseInt(new String(ch, start, length));
                        bindex = false; }
                    if (bblock) {
                        block = Integer.parseInt(new String(ch, start, length));
                        bblock = false; }
                    if (bkillingTest) {
                        killingTest = new String(ch, start, length);
                        bkillingTest = false; }
                    if (bdescription) {
                        description = new String(ch, start, length);
                        bdescription = false; }
                }

                public void endElement(String uri, String localName, String qName) {
                    if (qName.equalsIgnoreCase("mutation")) {
                        results.add(new PitXMLResult(fullQualifiedNameOfMutatedClass, state,
                                fullQualifiedNameMutantOperator, fullQualifiedNameMethod, fullQualifiedNameClass,
                                lineNumber, nameOfLocalisation, methodDescription, mutationDescription, index, block,
                                numberOfTestsRun, detected));
                    }
                }
            };

            File file = new File("c:\\file.xml");
            InputStream inputStream= new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream,"UTF-8");

            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");

            saxParser.parse(is, handler);


        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
