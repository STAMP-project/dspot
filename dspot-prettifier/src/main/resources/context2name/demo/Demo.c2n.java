import spoon.Launcher;

public class Demo {

    private static String str = "str";

    private final String mess = "mess-Demo";

    private final String gridBagConstraints = "global";

    private void mess(int result) {
        String mess = "mess-print";
        System.out.print(mess);
        String context = "local" + result;
        System.out.print(gridBagConstraints + context);
    }

    private void test() {
        String mess = "mess-label";
        System.out.print(mess);
        tc: for (int c = 0; c < 10; c++) {
            ex: for (int name = 10; name > 0; name--) {
                if (c != name) {
                    System.out.print("break as i" + c + "j" + name);
                    break tc;
                } else {
                    System.out.print("continue as i" + c + "j" + name);
                    continue ex;
                }
            }
        }
    }

    private void exception() {
        try {
            throw Exception;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] i) {
        System.out.print(Demo.str);
    }
}

