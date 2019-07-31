package eu.stamp_project.prettifier.context2name;

import eu.stamp_project.prettifier.Main;
import org.junit.Ignore;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Context2NameTest {
    @Ignore // DOES NOT WORK ON TRAVIS, CANNOT FIND python3 cmd
    @Test
    public void testDemo() {
        new Context2Name().fnDemo();
    }

    @Ignore // DOES NOT WORK ON TRAVIS, CANNOT FIND python3 cmd
    @Test
    public void testMain() {
        for (String strClass: new ArrayList<>(Arrays.asList(
            "class A { void m() { System.out.println();} }",
            "class A { void m(String yeah) { System.out.println(yeah);} }")
        )){
            CtClass ctClass = Launcher.parseClass(strClass);
            Set<CtMethod<?>> methodSet = ctClass.getMethods();
            List<CtMethod<?>> methodList = Main.applyContext2Name(new ArrayList<>(methodSet));
            for(CtMethod<?> ctMethod : methodList){
                System.out.println(ctMethod.toString());
            }
        }
    }
}
