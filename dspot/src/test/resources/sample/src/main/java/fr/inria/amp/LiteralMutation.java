package fr.inria.amp;

public class LiteralMutation {

    int presentLitInt = 32;
    double presentLitDouble = 32D;
    String presentLitString = "MySecondStringLiteral";

    public void methodByte() {
        byte literalByte = (byte)23;
    }

    public void methodNullByte() {
        Byte literalByte = null;
    }

    public void methodShort() {
        short literalShort = (short)23;
    }

    public void methodNullShort() { Short literalShort = null; }

    public void methodInteger() {
        int literalInt = 23;
    }

    public void methodNullInteger() {
        Integer literalInt = null;
    }

    public void methodLong() {
        long literalLong = 23L;
    }

    public void methodNullLong() {
        Long literalLong = null;
    }

    public void methodFloat() {
        float literalFloat = 23F;
    }

    public void methodNullFloat() {
        Float literalFloat = null;
    }

    public void methodDouble() {
        double literalDouble = 23D;
    }

    public void methodNullDouble() {
        Double literalDouble = null;
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

    public void methodNullBoolean() {
        Boolean literalBoolean = null;
    }

    public void methodCharacter() {
        char character = 'z';
    }

    public void methodNullCharacter() {
        Character character = null;
    }

    public void methodWithCharArray(char... array) {

    }

    public void methodThatClassmethodWithCharArray() {
        methodWithCharArray('a', 'b');
    }

}
