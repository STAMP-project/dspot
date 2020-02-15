package eu.stamp_project.dspot.common.configuration.options;

import eu.stamp_project.dspot.amplifier.amplifiers.*;

public enum AmplifierEnum {

    MethodDuplicationAmplifier(new MethodDuplicationAmplifier()),
    MethodRemove(new TestMethodCallRemover()),
    FastLiteralAmplifier(new FastLiteralAmplifier()),
    MethodAdderOnExistingObjectsAmplifier(new MethodAdderOnExistingObjectsAmplifier()),
    ReturnValueAmplifier(new ReturnValueAmplifier()),
    StringLiteralAmplifier(new StringLiteralAmplifier()),
    NumberLiteralAmplifier(new NumberLiteralAmplifier()),
    BooleanLiteralAmplifier(new BooleanLiteralAmplifier()),
    CharLiteralAmplifier(new CharLiteralAmplifier()),
    AllLiteralAmplifiers(new AllLiteralAmplifiers()),
//    ReplacementAmplifier(new ReplacementAmplifier()),
    NullifierAmplifier(new NullifierAmplifier()),
    ArrayAmplifier(new ArrayLiteralAmplifier()),
    None(null);

    private final Amplifier amplifier;

    public Amplifier getAmplifier() {
        return this.amplifier;
    }

    AmplifierEnum(Amplifier amplifier) {
        this.amplifier = amplifier;
    }
}
