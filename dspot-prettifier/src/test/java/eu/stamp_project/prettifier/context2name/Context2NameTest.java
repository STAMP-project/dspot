package eu.stamp_project.prettifier.context2name;

import eu.stamp_project.prettifier.Main;
import eu.stamp_project.utils.AmplificationHelper;
import org.junit.Ignore;
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
        String codeStr = "import spoon.Launcher;" + AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            "public class Demo {" + AmplificationHelper.LINE_SEPARATOR +
            "    private static String str = \"str\";" + AmplificationHelper.LINE_SEPARATOR +
            "    private final String mess = \"mess-Demo\";" + AmplificationHelper.LINE_SEPARATOR +
            "    private final String global = \"global\";" + AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            "    private void mess(int id) {" + AmplificationHelper.LINE_SEPARATOR +
            "        String mess = \"mess-print\";" + AmplificationHelper.LINE_SEPARATOR +
            "        System.out.print(mess);" + AmplificationHelper.LINE_SEPARATOR +
            "        String local = \"local\" + id;" + AmplificationHelper.LINE_SEPARATOR +
            "        System.out.print(global + local);" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            "    private void test() {" + AmplificationHelper.LINE_SEPARATOR +
            "        String mess = \"mess-label\";" + AmplificationHelper.LINE_SEPARATOR +
            "        System.out.print(mess);" + AmplificationHelper.LINE_SEPARATOR +
            "        outer:" + AmplificationHelper.LINE_SEPARATOR +
            "        for (int i = 0; i < 10; i++) {" + AmplificationHelper.LINE_SEPARATOR +
            "            inner:" + AmplificationHelper.LINE_SEPARATOR +
            "            for (int j = 10; j > 0; j--) {" + AmplificationHelper.LINE_SEPARATOR +
            "                if (i != j) {" + AmplificationHelper.LINE_SEPARATOR +
            "                    System.out.print(\"break as i\" + i + \"j\" + j);" + AmplificationHelper.LINE_SEPARATOR +
            "                    break outer;" + AmplificationHelper.LINE_SEPARATOR +
            "                } else {" + AmplificationHelper.LINE_SEPARATOR +
            "                    System.out.print(\"continue as i\" + i + \"j\" + j);" + AmplificationHelper.LINE_SEPARATOR +
            "                    continue inner;" + AmplificationHelper.LINE_SEPARATOR +
            "                }" + AmplificationHelper.LINE_SEPARATOR +
            "            }" + AmplificationHelper.LINE_SEPARATOR +
            "        }" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            "    private void exception() {" + AmplificationHelper.LINE_SEPARATOR +
            "        try {" + AmplificationHelper.LINE_SEPARATOR +
            "            throw Exception;" + AmplificationHelper.LINE_SEPARATOR +
            "        } catch (Exception ex) {" + AmplificationHelper.LINE_SEPARATOR +
            "            ex.printStackTrace();" + AmplificationHelper.LINE_SEPARATOR +
            "        }" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            AmplificationHelper.LINE_SEPARATOR +
            "    public static void main(String[] args) {" + AmplificationHelper.LINE_SEPARATOR +
            "        System.out.print(Demo.str);" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "}" + AmplificationHelper.LINE_SEPARATOR;

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
        assertEquals("private void mess(int id) {" + AmplificationHelper.LINE_SEPARATOR +
            "    String mess = \"mess-print\";" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.print(mess);" + AmplificationHelper.LINE_SEPARATOR +
            "    String local = \"local\" + id;" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.print(((this.global) + local));" + AmplificationHelper.LINE_SEPARATOR +
            "}", methodListBeforeC2N.get(0).toString());
        assertEquals("private void mess(int name) {" + AmplificationHelper.LINE_SEPARATOR +
            "    String mess = \"mess-print\";" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.print(mess);" + AmplificationHelper.LINE_SEPARATOR +
            "    String ex = \"local\" + name;" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.print(((this.global) + ex));" + AmplificationHelper.LINE_SEPARATOR +
            "}", methodListAfterC2N.get(0).toString());

        // the second method AKA "test"
        assertEquals("private void test() {" + AmplificationHelper.LINE_SEPARATOR +
            "    String mess = \"mess-label\";" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.print(mess);" + AmplificationHelper.LINE_SEPARATOR +
            "    outer : for (int i = 0; i < 10; i++) {" + AmplificationHelper.LINE_SEPARATOR +
            "        inner : for (int j = 10; j > 0; j--) {" + AmplificationHelper.LINE_SEPARATOR +
            "            if (i != j) {" + AmplificationHelper.LINE_SEPARATOR +
            "                System.out.print((((\"break as i\" + i) + \"j\") + j));" + AmplificationHelper.LINE_SEPARATOR +
            "                break outer;" + AmplificationHelper.LINE_SEPARATOR +
            "            } else {" + AmplificationHelper.LINE_SEPARATOR +
            "                System.out.print((((\"continue as i\" + i) + \"j\") + j));" + AmplificationHelper.LINE_SEPARATOR +
            "                continue inner;" + AmplificationHelper.LINE_SEPARATOR +
            "            }" + AmplificationHelper.LINE_SEPARATOR +
            "        }" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "}", methodListBeforeC2N.get(1).toString());
        assertEquals("private void test() {" + AmplificationHelper.LINE_SEPARATOR +
            "    String mess = \"mess-label\";" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.print(mess);" + AmplificationHelper.LINE_SEPARATOR +
            "    tc : for (int c = 0; c < 10; c++) {" + AmplificationHelper.LINE_SEPARATOR +
            "        result : for (int gridBagConstraints = 10; gridBagConstraints > 0; gridBagConstraints--) {" + AmplificationHelper.LINE_SEPARATOR +
            "            if (c != gridBagConstraints) {" + AmplificationHelper.LINE_SEPARATOR +
            "                System.out.print((((\"break as i\" + c) + \"j\") + gridBagConstraints));" + AmplificationHelper.LINE_SEPARATOR +
            "                break tc;" + AmplificationHelper.LINE_SEPARATOR +
            "            } else {" + AmplificationHelper.LINE_SEPARATOR +
            "                System.out.print((((\"continue as i\" + c) + \"j\") + gridBagConstraints));" + AmplificationHelper.LINE_SEPARATOR +
            "                continue result;" + AmplificationHelper.LINE_SEPARATOR +
            "            }" + AmplificationHelper.LINE_SEPARATOR +
            "        }" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "}", methodListAfterC2N.get(1).toString());

        // the third method AKA "exception"
        assertEquals("private void exception() {" + AmplificationHelper.LINE_SEPARATOR +
            "    try {" + AmplificationHelper.LINE_SEPARATOR +
            "        throw Exception;" + AmplificationHelper.LINE_SEPARATOR +
            "    } catch (Exception ex) {" + AmplificationHelper.LINE_SEPARATOR +
            "        ex.printStackTrace();" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "}", methodListBeforeC2N.get(2).toString());
        assertEquals("private void exception() {" + AmplificationHelper.LINE_SEPARATOR +
            "    try {" + AmplificationHelper.LINE_SEPARATOR +
            "        throw Exception;" + AmplificationHelper.LINE_SEPARATOR +
            "    } catch (Exception e) {" + AmplificationHelper.LINE_SEPARATOR +
            "        e.printStackTrace();" + AmplificationHelper.LINE_SEPARATOR +
            "    }" + AmplificationHelper.LINE_SEPARATOR +
            "}", methodListAfterC2N.get(2).toString());

        // the fourth method AKA "main"
        assertEquals("public static void main(String[] args) {" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.print(Demo.str);" + AmplificationHelper.LINE_SEPARATOR +
            "}", methodListBeforeC2N.get(3).toString());
        assertEquals("public static void main(String[] i) {" + AmplificationHelper.LINE_SEPARATOR +
            "    System.out.print(str);" + AmplificationHelper.LINE_SEPARATOR +
            "}", methodListAfterC2N.get(3).toString());
    }
}
