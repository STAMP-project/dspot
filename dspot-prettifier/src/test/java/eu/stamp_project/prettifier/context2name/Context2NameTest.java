package eu.stamp_project.prettifier.context2name;

import eu.stamp_project.prettifier.Main;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class Context2NameTest {
    @Test
    public void testContext2Name() {
        String codeStr = "import spoon.Launcher;\n" +
            "\n" +
            "public class Demo {\n" +
            "    private static String str = \"str\";\n" +
            "    private final String mess = \"mess-Demo\";\n" +
            "    private final String global = \"global\";\n" +
            "\n" +
            "    private void mess(int id) {\n" +
            "        String mess = \"mess-print\";\n" +
            "        System.out.print(mess);\n" +
            "        String local = \"local\" + id;\n" +
            "        System.out.print(global + local);\n" +
            "    }\n" +
            "\n" +
            "    private void test() {\n" +
            "        String mess = \"mess-label\";\n" +
            "        System.out.print(mess);\n" +
            "        outer:\n" +
            "        for (int i = 0; i < 10; i++) {\n" +
            "            inner:\n" +
            "            for (int j = 10; j > 0; j--) {\n" +
            "                if (i != j) {\n" +
            "                    System.out.print(\"break as i\" + i + \"j\" + j);\n" +
            "                    break outer;\n" +
            "                } else {\n" +
            "                    System.out.print(\"continue as i\" + i + \"j\" + j);\n" +
            "                    continue inner;\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    private void exception() {\n" +
            "        try {\n" +
            "            throw Exception;\n" +
            "        } catch (Exception ex) {\n" +
            "            ex.printStackTrace();\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.print(Demo.str);\n" +
            "    }\n" +
            "}\n";

        CtClass ctClass = Launcher.parseClass(codeStr);
        // make sure all methods are always in order
        List<CtMethod<?>> methodListBeforeC2N = new ArrayList<>();
        methodListBeforeC2N.addAll(ctClass.getMethodsByName("mess"));
        methodListBeforeC2N.addAll(ctClass.getMethodsByName("test"));
        methodListBeforeC2N.addAll(ctClass.getMethodsByName("exception"));
        methodListBeforeC2N.addAll(ctClass.getMethodsByName("main"));
        assertThat(methodListBeforeC2N.size(), is(4));
        // then we have corresponding prettified methods
        List<CtMethod<?>> methodListAfterC2N = Main.applyContext2Name(methodListBeforeC2N);
        assertThat(methodListAfterC2N.size(), is(4));

        // the first method AKA "mess"
        assertEquals("private void mess(int id) {\n" +
            "    String mess = \"mess-print\";\n" +
            "    System.out.print(mess);\n" +
            "    String local = \"local\" + id;\n" +
            "    System.out.print(((this.global) + local));\n" +
            "}", methodListBeforeC2N.get(0).toString());
        assertEquals("private void mess(int name) {\n" +
            "    String mess = \"mess-print\";\n" +
            "    System.out.print(mess);\n" +
            "    String ex = \"local\" + name;\n" +
            "    System.out.print(((this.global) + ex));\n" +
            "}", methodListAfterC2N.get(0).toString());

        // the second method AKA "test"
        assertEquals("private void test() {\n" +
            "    String mess = \"mess-label\";\n" +
            "    System.out.print(mess);\n" +
            "    outer : for (int i = 0; i < 10; i++) {\n" +
            "        inner : for (int j = 10; j > 0; j--) {\n" +
            "            if (i != j) {\n" +
            "                System.out.print((((\"break as i\" + i) + \"j\") + j));\n" +
            "                break outer;\n" +
            "            } else {\n" +
            "                System.out.print((((\"continue as i\" + i) + \"j\") + j));\n" +
            "                continue inner;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}", methodListBeforeC2N.get(1).toString());
        assertEquals("private void test() {\n" +
            "    String mess = \"mess-label\";\n" +
            "    System.out.print(mess);\n" +
            "    tc : for (int c = 0; c < 10; c++) {\n" +
            "        result : for (int gridBagConstraints = 10; gridBagConstraints > 0; gridBagConstraints--) {\n" +
            "            if (c != gridBagConstraints) {\n" +
            "                System.out.print((((\"break as i\" + c) + \"j\") + gridBagConstraints));\n" +
            "                break tc;\n" +
            "            } else {\n" +
            "                System.out.print((((\"continue as i\" + c) + \"j\") + gridBagConstraints));\n" +
            "                continue result;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}", methodListAfterC2N.get(1).toString());

        // the third method AKA "exception"
        assertEquals("private void exception() {\n" +
            "    try {\n" +
            "        throw Exception;\n" +
            "    } catch (Exception ex) {\n" +
            "        ex.printStackTrace();\n" +
            "    }\n" +
            "}", methodListBeforeC2N.get(2).toString());
        assertEquals("private void exception() {\n" +
            "    try {\n" +
            "        throw Exception;\n" +
            "    } catch (Exception e) {\n" +
            "        e.printStackTrace();\n" +
            "    }\n" +
            "}", methodListAfterC2N.get(2).toString());

        // the fourth method AKA "main"
        assertEquals("public static void main(String[] args) {\n" +
            "    System.out.print(Demo.str);\n" +
            "}", methodListBeforeC2N.get(3).toString());
        assertEquals("public static void main(String[] i) {\n" +
            "    System.out.print(str);\n" +
            "}", methodListAfterC2N.get(3).toString());
    }
}
