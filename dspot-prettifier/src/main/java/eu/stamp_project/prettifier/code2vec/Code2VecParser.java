package eu.stamp_project.prettifier.code2vec;

import eu.stamp_project.utils.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Code2VecParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(Code2VecParser.class);

    /**
     * This method parse the output of Code2Vec.
     *
     * @return the predicted test method name
     */
    public static String parse(String output) {
        LOGGER.info(output);
        final String[] splittedOutput = output.split(AmplificationHelper.LINE_SEPARATOR);
        // the predicted name with the most probability in on the second row
        final String mostProbableNameLine = splittedOutput[1];
        // we now concat each component of this name, and use CamelCase format:
        final String mostProbableNameArray = mostProbableNameLine.split("\\[")[1].split("\\]")[0];
        final String[] elements = mostProbableNameArray.split(",");
        return Arrays.stream(elements)
                .map(removeQuoteAndWhiteSpaces)
                .map(element -> removeQuoteAndWhiteSpaces.apply(elements[0]).equals(element) ? element : (Character.toUpperCase(element.charAt(0)) + element.substring(1)))
                .collect(Collectors.joining(""));
    }

    private static final Function<String, String> removeQuoteAndWhiteSpaces = string ->
            string.replaceAll("'", "").replaceAll(" ", "");

}
