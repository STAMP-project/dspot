package eu.stamp_project.options;

import eu.stamp_project.dspot.amplifier.*;
import eu.stamp_project.utils.AmplificationHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum AmplifierEnum {

    MethodAdd(new TestMethodCallAdder()),
    MethodRemove(new TestMethodCallRemover()),
    TestDataMutator(new TestDataMutator()),
    MethodGeneratorAmplifier(new MethodGeneratorAmplifier()),
    ReturnValueAmplifier(new ReturnValueAmplifier()),
    StringLiteralAmplifier(new StringLiteralAmplifier()),
    NumberLiteralAmplifier(new NumberLiteralAmplifier()),
    BooleanLiteralAmplifier(new BooleanLiteralAmplifier()),
    CharLiteralAmplifier(new CharLiteralAmplifier()),
    AllLiteralAmplifiers(new AllLiteralAmplifiers()),
    ReplacementAmplifier(new ReplacementAmplifier()),
    NullifierAmplifier(new NullifierAmplifier()),
    None(null);

    public final Amplifier amplifier;

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplifierEnum.class);

    private AmplifierEnum(Amplifier amplifier) {
        this.amplifier = amplifier;
    }

    public static Amplifier stringToAmplifier(String amplifier) {
        try {
            return AmplifierEnum.valueOf(amplifier).amplifier;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Wrong values for amplifiers: {} is not recognized", amplifier);
            LOGGER.warn("Possible values are: {}", getPossibleValuesForInputAmplifier());
            LOGGER.warn("No amplifier has been added for {}", amplifier);
            return null;
        }
    }

    @NotNull
    public static String getPossibleValuesForInputAmplifier() {
        return AmplificationHelper.LINE_SEPARATOR + "\t\t - " +
                Arrays.stream(new String[]{
                        "StringLiteralAmplifier",
                        "NumberLiteralAmplifier",
                        "CharLiteralAmplifier",
                        "BooleanLiteralAmplifier",
                        "AllLiteralAmplifiers",
                        "MethodAdd",
                        "MethodRemove",
                        "TestDataMutator (deprecated)",
                        "MethodGeneratorAmplifier",
                        "ReturnValueAmplifier",
                        "ReplacementAmplifier",
                        "NullifierAmplifier",
                        "None"
                }).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR + "\t\t - "));
    }

    public static List<Amplifier> buildAmplifiersFromString(String[] amplifiersAsString) {
        if (amplifiersAsString.length == 0 || "None".equals(amplifiersAsString[0])) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(amplifiersAsString)
                    .map(AmplifierEnum::stringToAmplifier)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }
}