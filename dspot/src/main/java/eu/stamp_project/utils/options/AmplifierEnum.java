package eu.stamp_project.utils.options;

import eu.stamp_project.dspot.amplifier.*;

import java.util.*;
import java.util.stream.Collectors;

public enum AmplifierEnum {

    MethodAdd(new MethodDuplicationAmplifier()),
    MethodDuplicationAmplifier(new MethodDuplicationAmplifier()),
    MethodRemove(new TestMethodCallRemover()),
    FastLiteralAmplifier(new FastLiteralAmplifier()),
    TestDataMutator(new FastLiteralAmplifier()),
    MethodGeneratorAmplifier(new MethodAdderOnExistingObjectsAmplifier()),
    MethodAdderOnExistingObjectsAmplifier(new MethodAdderOnExistingObjectsAmplifier()),
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

    private static final Map<String, String> deprecatedValuesToNewNames = new HashMap<>();

    static {
        deprecatedValuesToNewNames.put("MethodAdd", "MethodDuplicationAmplifier");
        deprecatedValuesToNewNames.put("TestDataMutator", "FastLiteralAmplifier");
        deprecatedValuesToNewNames.put("MethodGeneratorAmplifier", "MethodAdderOnExistingObjectsAmplifier");
    }

    private AmplifierEnum(Amplifier amplifier) {
        this.amplifier = amplifier;
    }

    private static Amplifier stringToAmplifier(String amplifier) {
        try {
            if (deprecatedValuesToNewNames.containsKey(amplifier)) {
                JSAPOptions.LOGGER.warn("You are using an old name: " + amplifier + ".");
                JSAPOptions.LOGGER.warn("You should use the new name: " + deprecatedValuesToNewNames.get(amplifier) + ".");
                JSAPOptions.LOGGER.warn("The entry " + amplifier + " will be deleted very soon.");
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