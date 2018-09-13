package eu.stamp_project.utils;

import eu.stamp_project.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.JavaOutputProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * User: Simon Date: 18/05/16 Time: 16:10
 */
public class DSpotUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSpotUtils.class);

    private static StringBuilder progress = new StringBuilder(60);

    public static void printProgress(int done, int total) {
        char[] workchars = {'|', '/', '-', '\\'};
        String format = "\r%3d%% |%s ]%c";
        int percent = (++done * 100) / total;
        int extrachars = (percent / 2) - progress.length();
        while (extrachars-- > 0) {
            progress.append('=');
        }
        System.out.printf(format, percent, progress, workchars[done % workchars.length]);
        if (done == total) {
            System.out.flush();
            System.out.println();
            progress = new StringBuilder(60);
        }
    }

    public static String[] getAllTestClasses(InputConfiguration configuration) {
        return configuration.getFactory().Class().getAll().stream()
                .filter(ctType -> ctType.getMethods().stream().anyMatch(AmplificationChecker::isTest))
                .map(CtType::getQualifiedName).toArray(String[]::new);
    }

    public static void printCtTypeToGivenDirectory(CtType<?> type, File directory) {
        Factory factory = type.getFactory();
        Environment env = factory.getEnvironment();
        env.setAutoImports(true);
        env.setCommentEnabled(InputConfiguration.get().withComment());
        JavaOutputProcessor processor = new JavaOutputProcessor(new DefaultJavaPrettyPrinter(env));
        processor.setFactory(factory);
        processor.getEnvironment().setSourceOutputDirectory(directory);
        processor.createJavaFile(type);
        env.setAutoImports(false);
    }

    public static void printAmplifiedTestClass(CtType<?> type, File directory) {
        final String pathname = directory.getAbsolutePath() + "/" + type.getQualifiedName().replaceAll("\\.", "/")
                + ".java";
        if (new File(pathname).exists()) {
            printCtTypeToGivenDirectory(addGeneratedTestToExistingClass(type, pathname), directory);
        } else {
            printCtTypeToGivenDirectory(type, directory);
        }
    }

    private static CtClass<?> addGeneratedTestToExistingClass(CtType<?> type, String pathname) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource(pathname);
        launcher.buildModel();
        final CtClass<?> existingAmplifiedTest = launcher.getFactory().Class().get(type.getQualifiedName());
        type.getMethods().stream().filter(testCase -> !existingAmplifiedTest.getMethods().contains(testCase))
                .forEach(existingAmplifiedTest::addMethod);
        return existingAmplifiedTest;
    }

    /*
    public static void printAllClasses(Factory factory, File out) {
        factory.Class().getAll().forEach(type -> printCtTypeToGivenDirectory(type, out, ));
    }
    */

    public static void addComment(CtElement element, String content, CtComment.CommentType type) {
        CtComment comment = element.getFactory().createComment(content, type);
        if (!element.getComments().contains(comment)) {
            element.addComment(comment);
        }
    }

    public static final String pathToDSpotDependencies = "target/dspot/dependencies/";

    public static final String packagePath = "eu/stamp_project/compare/";

    public static final String[] classesToCopy = new String[]{"MethodsHandler", "ObjectLog", "Observation", "Utils", "FailToObserveException"};

    public static void copyPackageFromResources() {
        final String pathToTestClassesDirectory = pathToDSpotDependencies + "/" + packagePath + "/";
        final String directory = packagePath.split("/")[packagePath.split("/").length - 1];
        try {
            FileUtils.forceMkdir(new File(pathToTestClassesDirectory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Arrays.stream(classesToCopy).forEach(file -> {
            OutputStream resStreamOut = null;
            try {
                final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(directory + "/" + file + ".class");
                resStreamOut = new FileOutputStream(pathToTestClassesDirectory + file + ".class");
                int readBytes;
                byte[] buffer = new byte[4096];
                while ((readBytes = resourceAsStream.read(buffer)) > 0) {
                    resStreamOut.write(buffer, 0, readBytes);
                }
                resStreamOut.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static final Function<String, String> shouldAddSeparator = string -> string != null ? string + (string.endsWith(AmplificationHelper.FILE_SEPARATOR) ? "" : AmplificationHelper.FILE_SEPARATOR) : string;

    public static String ctTypeToFullQualifiedName(CtType<?> testClass) {
        if (testClass.getModifiers().contains(ModifierKind.ABSTRACT)) {
            CtTypeReference<?> referenceOfSuperClass = testClass.getReference();
            return testClass.getFactory().Class().getAll().stream()
                    .filter(ctType -> referenceOfSuperClass.equals(ctType.getSuperclass()))
                    .map(CtType::getQualifiedName).collect(Collectors.joining(","));
        } else {
            return testClass.getQualifiedName();
        }
    }

    public static final String PATH_TO_EXTRA_DEPENDENCIES_TO_DSPOT_CLASSES = new File("target/dspot/dependencies/").getAbsolutePath();
}
