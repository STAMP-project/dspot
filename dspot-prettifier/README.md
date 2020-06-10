# post-dspot

Post-dspot processes [DSpot](http://github.com/STAMP-project/dspot.git)'s output to make it readable.

The process has 3 steps: 

1. Minimize the test method by removing all the "useless" statements.
2. Rename the test method. This is done using [code2vec](https://github.com/tech-srl/code2vec).
3. Rename the local variable used in the the test method. This is done based on [context2name](https://github.com/rbavishi/Context2Name).

## Command Line Usage
```
java -jar /path/to/dspot-prettifier-LATEST-jar-with-dependencies.jar --apply-all-prettifiers --absolute-path-to-project-root=<path> --path-to-amplified-test-class=<path>
```

The prettifier uses DSpot in the background, you can pass all arguments that you can also pass to DSpot.

### Configure Prettifiers
To select which prettifiers should be applied to the amplified test case you can pass these options:
```
--apply-all-prettifiers
                       Apply all available prettifiers. This overrides options that turn off specific
                         prettifiers. Default value: false
--apply-general-minimizer
                       Apply the general minimizer to remove redundant assertions and inline local variables.
                         Default value: false
--apply-pit-minimizer  Apply the pit minimizer to remove assertions that do not improve the mutation score.
                         Default value: false
--rename-local-variables
                       Apply Context2Vec to give the local variables more expressive names. Default value: false
--rename-test-methods  Apply Code2Vec to give the test methods more expressive names. Default value: false
```

## Install
For __code2vec__, you need to run `install_code2vec.sh` to download repo.

## Info
For __context2name__, if you want to train one better model:

1. Prepare corpus. You could run `PGA.java` directly or make some adjustments to customize corpus.
2. Process corpus. You just need to call `fnCorpus()` of `C2N.java`, then `training.csv` and `validation.csv` get generated.
3. Train model. You just need to run `c2n_train.py` with `training.csv`, `validation.csv` and `config.json` at hand. Besides, run `pip3 install bottleneck numpy keras tensorflow` to install lib if necessary.
4. Run one demo. You just need to call `fnDemo()` of `C2N.java`.

## Demo
For __context2name__, here is some methods for demonstration:

before Context2Name
```Java
private void mess(int id) {
    String mess = "mess-print";
    System.out.print(mess);
    String local = "local" + id;
    System.out.print(((this.global) + local));
}
```
after Context2Name
```Java
private void mess(int name) {
    String mess = "mess-print";
    System.out.print(mess);
    String ex = "local" + name;
    System.out.print(((this.global) + ex));
}
```

before Context2Name
```Java
private void test() {
    String mess = "mess-label";
    System.out.print(mess);
    outer : for (int i = 0; i < 10; i++) {
        inner : for (int j = 10; j > 0; j--) {
            if (i != j) {
                System.out.print(((("break as i" + i) + "j") + j));
                break outer;
            } else {
                System.out.print(((("continue as i" + i) + "j") + j));
                continue inner;
            }
        }
    }
}
```
after Context2Name
```Java
private void test() {
    String mess = "mess-label";
    System.out.print(mess);
    tc : for (int c = 0; c < 10; c++) {
        result : for (int gridBagConstraints = 10; gridBagConstraints > 0; gridBagConstraints--) {
            if (c != gridBagConstraints) {
                System.out.print(((("break as i" + c) + "j") + gridBagConstraints));
                break tc;
            } else {
                System.out.print(((("continue as i" + c) + "j") + gridBagConstraints));
                continue result;
            }
        }
    }
}
```

before Context2Name
```Java
private void exception() {
    try {
        throw Exception;
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
```
after Context2Name
```Java
private void exception() {
    try {
        throw Exception;
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

before Context2Name
```Java
public static void main(String[] args) {
    System.out.print(Demo.str);
}
```
after Context2Name
```Java
public static void main(String[] i) {
    System.out.print(str);
}
```

If interested, here are complete code files before and after Context2Name:

before Context2Name
```Java
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
```
after Context2Name
```Java
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
```