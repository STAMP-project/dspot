package fr.inria.diversify.testRunner;

import fr.inria.diversify.compare.ObjectLog;
import fr.inria.diversify.dspot.TypeUtils;
import fr.inria.diversify.dspot.support.DSpotCompiler;
import fr.inria.diversify.util.FileUtils;
import fr.inria.diversify.util.PrintClassUtils;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import spoon.Launcher;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.declaration.CtMethodImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.util.stream.Collectors;

import static org.codehaus.plexus.util.FileUtils.forceDelete;

/**
 * User: Simon
 * Date: 05/04/16
 * Time: 10:28
 */
public class TestCompiler {

    public static boolean writeAndCompile(DSpotCompiler compiler, CtType classTest, boolean withLogger, String dependencies) {
        if (withLogger) {
            copyLoggerFile(compiler);
        }
        printAndDelete(compiler, classTest);
        try {
            return compiler.compile(dependencies);
        } catch (Exception e) {
            return false;
        }
    }

    public static List<CtMethod<?>> compile(DSpotCompiler compiler, CtType classTest,
                                            boolean withLogger, String dependencies) {
        if (withLogger) {
            copyLoggerFile(compiler);
        }
        printAndDelete(compiler, classTest);
        final List<CategorizedProblem> problems = compiler.compileAndGetProbs(dependencies);
        if (problems.isEmpty()) {
            return Collections.emptyList();
        } else {
            try {
                final CtClass<?> newModelCtClass = getNewModelCtClass(compiler.getSourceOutputDirectory().getAbsolutePath(),
                        classTest.getQualifiedName());
                final HashSet<CtMethod<?>> methodsToRemove = problems.stream()
                        .filter(IProblem::isError)
                        .collect(HashSet<CtMethod<?>>::new,
                                (ctMethods, categorizedProblem) -> {
                                    final Optional<CtMethod<?>> methodToRemove = newModelCtClass.getMethods().stream()
                                            .filter(ctMethod ->
                                                    ctMethod.getPosition().getSourceStart() <= categorizedProblem.getSourceStart() &&
                                                            ctMethod.getPosition().getSourceEnd() >= categorizedProblem.getSourceEnd())
                                            .findFirst();
                                    if (methodToRemove.isPresent()) {
                                        ctMethods.add(methodToRemove.get());
                                    }
                                },
                                HashSet<CtMethod<?>>::addAll);

                final List<CtMethod<?>> methods = methodsToRemove.stream()
                        .map(CtMethod::getSimpleName)
                        .map(methodName -> (CtMethod<?>) classTest.getMethodsByName(methodName).get(0))
                        .collect(Collectors.toList());

                final List<CtMethod<?>> methodToKeep = newModelCtClass.getMethods().stream()
                        .filter(ctMethod -> ctMethod.getBody().getStatements().stream()
                                .filter(statement -> !(statement instanceof CtComment) && !methodsToRemove.contains(ctMethod))
                                .findFirst()
                                .isPresent())
                        .collect(Collectors.toList());

                methodsToRemove.addAll(
                        newModelCtClass.getMethods().stream()
                                .filter(ctMethod -> !methodToKeep.contains(ctMethod))
                                .collect(Collectors.toList())
                );

                methods.forEach(classTest::removeMethod);
                methods.addAll(compile(compiler, classTest, withLogger, dependencies));
                return new ArrayList<>(methods);
            } catch (Exception e) {
                return Collections.singletonList(METHOD_CODE_RETURN);
            }
        }
    }

    public static final CtMethod<?> METHOD_CODE_RETURN = new CtMethodImpl();

    private static CtClass<?> getNewModelCtClass(String pathToSrcFolder, String fullQualifiedName) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.addInputResource(pathToSrcFolder);
        launcher.buildModel();

        return launcher.getFactory().Class().get(fullQualifiedName);
    }

    private static void printAndDelete(DSpotCompiler compiler, CtType classTest) {
        try {
            PrintClassUtils.printJavaFile(compiler.getSourceOutputDirectory(), classTest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            String pathToDotClass = compiler.getBinaryOutputDirectory().getAbsolutePath() + "/" + classTest.getQualifiedName().replaceAll("\\.", "/") + ".class";
            forceDelete(pathToDotClass);
        } catch (IOException ignored) {
            //ignored
        }
    }

    private static void copyLoggerFile(DSpotCompiler compiler) {
        try {
            String comparePackage = ObjectLog.class.getPackage().getName().replace(".", "/");
            File srcDir = new File(System.getProperty("user.dir") + "/src/main/java/" + comparePackage);

            File destDir = new File(compiler.getSourceOutputDirectory() + "/" + comparePackage);
            FileUtils.forceMkdir(destDir);

            FileUtils.copyDirectory(srcDir, destDir);

            String typeUtilsPackage = TypeUtils.class.getPackage().getName().replace(".", "/");
            File srcFile = new File(System.getProperty("user.dir") + "/src/main/java/" + typeUtilsPackage + "/TypeUtils.java");

            destDir = new File(compiler.getSourceOutputDirectory() + "/" + typeUtilsPackage);
            FileUtils.forceMkdir(destDir);

            File destFile = new File(compiler.getSourceOutputDirectory() + "/" + typeUtilsPackage + "/TypeUtils.java");
            FileUtils.copyFile(srcFile, destFile);
        } catch (FileAlreadyExistsException ignored) {
            //skip
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
