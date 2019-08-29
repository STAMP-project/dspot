import spoon.Launcher;

public class Demo {
    private static String str = "str";
    private final String mess = "mess-Demo";
    private final String global = "global";

    private void mess(int id) {
        String mess = "mess-print";
        System.out.print(mess);
        String local = "local" + id;
        System.out.print(global + local);
    }

    private void test() {
        String mess = "mess-label";
        System.out.print(mess);
        outer:
        for (int i = 0; i < 10; i++) {
            inner:
            for (int j = 10; j > 0; j--) {
                if (i != j) {
                    System.out.print("break as i" + i + "j" + j);
                    break outer;
                } else {
                    System.out.print("continue as i" + i + "j" + j);
                    continue inner;
                }
            }
        }
    }

    private void exception() {
        try {
            throw Exception;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.print(Demo.str);
    }
}
