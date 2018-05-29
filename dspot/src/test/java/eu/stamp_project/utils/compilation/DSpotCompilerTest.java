package eu.stamp_project.utils.compilation;

import eu.stamp_project.dspot.amplifier.Amplifier;
import eu.stamp_project.utils.sosiefier.InputConfiguration;
import eu.stamp_project.utils.sosiefier.InputProgram;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 3/13/17
 */
public class DSpotCompilerTest {

    @Before
    public void setUp() throws Exception {
        try {
            FileUtils.forceDelete(new File("target/dspot/tmp_test_sources/"));
        } catch (Exception ignored) {
            //ignored
        }
    }

    // TODO update with configuration
    @Test
    public void testDSpotCompiler() throws Exception {

        final InputConfiguration configuration = getConfiguration();
        final DSpotCompiler compiler = DSpotCompiler.createDSpotCompiler(configuration, "");
        final CtClass<?> aClass = getClass(compiler.getLauncher().getFactory());
        final List<CtMethod<?>> compile = TestCompiler.compileAndDiscardUncompilableMethods(compiler, aClass, "");
        assertTrue(compile.isEmpty());
        assertEquals(1, aClass.getMethods().size());

        final List<CtMethod> tests = new UncompilableAmplifier().apply(aClass.getMethods().stream().findAny().get());
        tests.forEach(aClass::addMethod);
        assertEquals(3, aClass.getMethods().size());

        final CtMethod uncompilableTest = tests.stream()
                .filter(ctMethod -> ctMethod.getSimpleName().equals("uncompilableTest"))
                .findFirst()
                .get();

        final List<CtMethod<?>> results = TestCompiler.compileAndDiscardUncompilableMethods(compiler, aClass, "");
        assertEquals(1, results.size());
        assertEquals("uncompilableTest", results.get(0).getSimpleName());
        assertEquals(uncompilableTest, results.get(0));
//        assertEquals(2, aClass.getMethods().size());
        assertEquals(3, aClass.getMethods().size());//The compile methods is now stateless: using a clone class
    }

    // quick implementation used to produce a uncompilable test case
    private class UncompilableAmplifier implements Amplifier {

        @Override
        public List<CtMethod> apply(CtMethod testMethod) {
            final CtCodeSnippetStatement snippet = testMethod.getFactory().Code().
                    createCodeSnippetStatement("UncompilableClass class = new UncompilableClass()");
            final CtMethod method = testMethod.clone();
            method.getBody().insertEnd(snippet);
            method.setSimpleName("uncompilableTest");

            final CtCodeSnippetStatement snippet1 = testMethod.getFactory().Code().createCodeSnippetStatement("String clazz = new String()");
            final CtMethod method1 = testMethod.clone();
            method1.getBody().insertEnd(snippet1);
            method1.setSimpleName("compilableTest");

            return Arrays.asList(method, method1);
        }

        @Override
        public void reset(CtType testClass) {

        }

    }

    private InputProgram getInputProgram() {
        final File tmpDir = new File("tmpDir");
        if (tmpDir.exists()) {
            try {
                FileUtils.cleanDirectory(tmpDir);
            } catch (IOException ignored) {
                //ignored
            }
        }
        final InputProgram inputProgram = new InputProgram();
        inputProgram.setProgramDir("target/dspot/trash/");
        inputProgram.setRelativeSourceCodeDir("src/main/java/");
        inputProgram.setRelativeTestSourceCodeDir("src/test/java");
        return inputProgram;
    }

    private InputConfiguration getConfiguration() {
        final InputConfiguration inputConfiguration = new InputConfiguration();
        final InputProgram inputProgram = new InputProgram();
        inputProgram.setProgramDir("target/dspot/trash/");
        inputProgram.setRelativeSourceCodeDir("src/main/java/");
        inputProgram.setRelativeTestSourceCodeDir("src/test/java");
        inputConfiguration.setAbsolutePathToProjectRoot(new File("target/dspot/trash/").getAbsolutePath());
        inputConfiguration.setInputProgram(inputProgram);
        return inputConfiguration;
    }

    private CtClass<?> getClass(Factory factory) {
        final CtClass<?> aClass = factory.Class().create("MyTestClass");
        final CtMethod<Void> method = factory.createMethod();
        method.setSimpleName("method");
        method.setType(factory.Type().VOID_PRIMITIVE);
        method.setBody(factory.createCodeSnippetStatement("System.out.println()"));
        method.addModifier(ModifierKind.PUBLIC);
        aClass.addMethod(method);
        return aClass;
    }
}
