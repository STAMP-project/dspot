package eu.stamp_project.prettifier.context2name;

import eu.stamp_project.utils.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Context2NameParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(Context2NameParser.class);

    // this list is used in order to not have two test methods with the same name.
    private List<String> predictedNames;

    public Context2NameParser() {
        this.predictedNames = new ArrayList<>();
    }

    /**
     * This method parse the output of Context2Name.
     *
     * @return the predicted test method name
     */
    public String parse(String output) {
        LOGGER.info(output);
        final String[] splittedOutput = output.split(AmplificationHelper.LINE_SEPARATOR);
        // the predicted name with the most probability in on the second row
        String predictedName = buildPredictName(splittedOutput[1]);
        return predictedName;
    }

    private String buildPredictName(String line) {
        // we now concat each component of this name, and use CamelCase format:
        final String mostProbableNameArray = line.split("\\[")[1].split("\\]")[0];
        final String[] elements = mostProbableNameArray.split(",");
        return this.findNextPredictedName(
            Arrays.stream(elements)
                .map(removeQuoteAndWhiteSpaces)
                .map(element -> removeQuoteAndWhiteSpaces.apply(elements[0]).equals(element) ? element : (Character.toUpperCase(element.charAt(0)) + element.substring(1)))
                .collect(Collectors.joining(""))
        );
    }

    private String extractDigitsAtTheEnd(String string) {
        StringBuilder digits = new StringBuilder();
        for (int i = string.length() - 1 ; i > 0; i--) {
            if (Character.isDigit(string.charAt(i))) {
                digits.insert(0, string.charAt(i));
            }
        }
        return digits.toString();
    }

    private String findNextPredictedName(String currentPredictedName) {
        if (this.predictedNames.contains(currentPredictedName)) {
            final String digits = this.extractDigitsAtTheEnd(currentPredictedName);
            final String predictedNameWithoutDigit = currentPredictedName.substring(0, currentPredictedName.length() - digits.length());
            return this.findNextPredictedName(
                    predictedNameWithoutDigit + (digits.isEmpty() ? "1" : Integer.parseInt(digits) + 1)
            );
        } else {
            this.predictedNames.add(currentPredictedName);
            return currentPredictedName;
        }
    }

    private final Function<String, String> removeQuoteAndWhiteSpaces = string ->
            string.replaceAll("'", "").replaceAll(" ", "");

}
