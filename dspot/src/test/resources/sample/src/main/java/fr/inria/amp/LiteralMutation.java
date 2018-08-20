package fr.inria.amp;

public class LiteralMutation {

    int presentLitInt = 32;
    double presentLitDouble = 32D;
    String presentLitString = "MySecondStringLiteral";

    public void methodByte() {
        byte literalByte = (byte)23;
    }

    public void methodShort() {
        short literalShort = (short)23;
    }

    public void methodInteger() {
        int literalInt = 23;
    }

    public void methodLong() {
        long literalLong = 23L;
    }

    public void methodFloat() {
        float literalFloat = 23F;
    }

    public void methodDouble() {
        double literalDouble = 23D;
    }

    public void methodString() {
        String literalString = "MyStringLiteral";
        literalString = null;
        String literalString2 = null;
        literalString = getString("MyStringLiteral3");
        literalString = getString(null);
    }

    private String getString(String s) {
        return s;
    }

    public void methodBoolean() {
        boolean literalBoolean = true;
    }

    public void methodCharacter() {
        char character = 'z';
    }

    public void methodWithCharArray(char... array) {

    }

    public void methodThatClassmethodWithCharArray() {
        methodWithCharArray('a', 'b');
    }

}