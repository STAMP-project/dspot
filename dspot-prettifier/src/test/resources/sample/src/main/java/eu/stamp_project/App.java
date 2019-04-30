package eu.stamp_project;

public class App {

    public static void main(String[] args) {
        System.out.println("Hello World !");
    }

    private int i;

    public App(int i) {
        this.i = i;
    }

    public int getInt() {
        return i;
    }

    public void compute() {
        i = i * i / 2;
    }

    public void compute(int j) {
        i = i * j / 2;
    }

}