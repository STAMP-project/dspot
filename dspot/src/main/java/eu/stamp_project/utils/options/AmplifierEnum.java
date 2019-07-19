package eu.stamp_project.utils.options;

import eu.stamp_project.dspot.amplifier.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum AmplifierEnum {

    MethodAdd(new TestMethodCallAdder()),
    MethodRemove(new TestMethodCallRemover()),
    FastLiteralAmplifier(new FastLiteralAmplifier()),
    TestDataMutator(new FastLiteralAmplifier()),
    MethodGeneratorAmplifier(new MethodGeneratorAmplifier()),
    ReturnValueAmplifier(new ReturnValueAmplifier()),
    StringLiteralAmplifier(new StringLiteralAmplifier()),
    NumberLiteralAmplifier(new NumberLiteralAmplifier()),
    BooleanLiteralAmplifier(new BooleanLiteralAmplifier()),
    CharLiteralAmplifier(new CharLiteralAmplifier()),
    AllLiteralAmplifiers(new AllLiteralAmplifiers()),
    //ReplacementAmplifier(new ReplacementAmplifier()),
    NullifierAmplifier(new NullifierAmplifier()),
    None(null);

    public final Amplifier amplifier;

    private AmplifierEnum(Amplifier amplifier) {
        this.amplifier = amplifier;
    }

    private static Amplifier stringToAmplifier(String amplifier) {
        try {
            if ("TestDataMutator".equals(amplifier)) {
                JSAPOptions.LOGGER.warn("You are using an old name for TestDataMutator.");
                JSAPOptions.LOGGER.warn("You should use the new name: FastLiteralAmplifier.");
                JSAPOptions.LOGGER.warn("The entry TestDataMutator will be deleted very soon.");
            }
            return AmplifierEnum.valueOf(amplifier).amplifier;
        } catch (IllegalArgumentException e) {
            // should not happen since we checked values with Checker.checkEnumAndRemoveIfIncorrect
            throw new RuntimeException(e);
        }
    }

    public static List<Amplifier> buildAmplifiersFromString(List<String> amplifiersAsString) {
        if (amplifiersAsString.size() == 0 || "None".equals(amplifiersAsString.get(0))) {
            return Collections.emptyList();
        } else {
            return amplifiersAsString.stream()
                    .map(AmplifierEnum::stringToAmplifier)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }
}