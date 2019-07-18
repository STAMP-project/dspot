package eu.stamp_project.utils.options;

import eu.stamp_project.dspot.amplifier.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum AmplifierEnum {

    MethodAdd(new TestMethodCallAdder()),
    MethodRemove(new TestMethodCallRemover()),
    FastLiteralAmplifier(new FastLiteralAmplifier()),
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