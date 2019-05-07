package eu.stamp_project.utils;

import eu.stamp_project.utils.compilation.DSpotCompiler;
import eu.stamp_project.utils.program.InputConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
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
import java.util.Set;
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

    public static void printCtTypeToGivenDirectory(CtType<?> type, File directory) {
        DSpotUtils.printCtTypeToGivenDirectory(type, directory, false);
    }

    public static void printCtTypeToGivenDirectory(CtType<?> type, File directory, boolean autoImports) {
        Factory factory = type.getFactory();
        Environment env = factory.getEnvironment();
        env.setAutoImports(autoImports);
        env.setNoClasspath(true);
        env.setCommentEnabled(InputConfiguration.get().withComment());
        JavaOutputProcessor processor = new JavaOutputProcessor(new DefaultJavaPrettyPrinter(env));
        processor.setFactory(factory);
        processor.getEnvironment().setSourceOutputDirectory(directory);
        processor.createJavaFile(type);
        env.setAutoImports(false);
    }

    /*
        First, we print the amplified java test class with imports.
        We compile it.
        If the compilation fails, we re-print it without imports, i.e. using full qualified names.
     */
    public static void printAndCompileToCheck(CtType<?> type, File directory) {

        // get the existing amplified test class, if so
        final String regex = File.separator.equals("/") ? "/" : "\\\\";
        final String pathname =
                directory.getAbsolutePath() + File.separator +
                        type.getQualifiedName().replaceAll("\\.", regex) + ".java";
        final CtType<?> existingAmplifiedTestClass;
        if (new File(pathname).exists()) {
            existingAmplifiedTestClass = getExistingClass(type, pathname);//FIXME: analyse for optimisation (16% total execution time)
            Set<CtMethod<?>> methods = type.getMethods();
            existingAmplifiedTestClass.getMethods()
                    .stream()
                    .filter(testCase -> !methods.contains(testCase))//Optimization: extracting type.getMethods invocation.
                    .forEach(type::addMethod);
        }
        printCtTypeToGivenDirectory(type, directory, true);
        // compile
        try {
            final boolean compile = DSpotCompiler.compile(InputConfiguration.get(), //FIXME: analyse for optimisation (36% total execution time)
                    pathname,
                    InputConfiguration.get().getFullClassPathWithExtraDependencies(),
                    new File(InputConfiguration.get().getOutputDirectory() + "/binaries/")
            );
            if (!compile) {
                FileUtils.forceDelete(new File(InputConfiguration.get().getOutputDirectory() + "/binaries/"));
                LOGGER.warn("Could not compile {} with imports.", type.getQualifiedName());
                LOGGER.warn("DSpot outputs it using full qualified names.");
                LOGGER.warn("These problems can come from the fact your project use generated codes, such as Lombok annotations.");
                printCtTypeToGivenDirectory(type, directory, false); //FIXME: analyse for optimisation (13% total execution time)
            }
        } catch (Exception ignored) {
            LOGGER.warn("Couldn't compile the final amplified test class.");
            LOGGER.warn("It might be uncompilable and could require manual modification.");
        }

    }

    private static CtClass<?> getExistingClass(CtType<?> type, String pathname) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource(pathname);
        launcher.buildModel();
        return launcher.getFactory().Class().get(type.getQualifiedName());

    }

    public static void addComment(CtElement element, String content, CtComment.CommentType type) {
        CtComment comment = element.getFactory().createComment(content, type);
        if (!element.getComments().contains(comment)) {
            element.addComment(comment);
        }
    }

    private static final String PATH_TO_DSPOT_DEPENDENCIES = "target/dspot/dependencies/";

    private static final String PACKAGE_NAME = "compare";

    private static final String PACKAGE_PATH = "eu/stamp_project/" + PACKAGE_NAME + "/";

    private static final String[] DSPOT_CLASSES = new String[]{"MethodsHandler", "ObjectLog", "Observation", "Utils", "FailToObserveException"};

    public static String getAbsolutePathToDSpotDependencies() {
        return InputConfiguration.get().getAbsolutePathToProjectRoot() + PATH_TO_DSPOT_DEPENDENCIES;
    }

    public static void copyPackageFromResources() {

        final String pathToTestClassesDirectory = DSpotUtils.getAbsolutePathToDSpotDependencies() + PACKAGE_PATH;
        try {
            FileUtils.forceMkdir(new File(pathToTestClassesDirectory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Arrays.stream(DSPOT_CLASSES).forEach(file -> {
            try {
                InputStream stream = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(PACKAGE_NAME + "/" + file + ".class");
                // try this for Jenkins
                if (stream == null) {
                    stream = DSpotUtils.class.getClassLoader()
                            .getResourceAsStream(PACKAGE_NAME + "/" + file + ".class");
                }
                final OutputStream resStreamOut = new FileOutputStream(pathToTestClassesDirectory + file + ".class");

                int readBytes;
                byte[] buffer = new byte[4096];
                while ((readBytes = stream.read(buffer)) > 0) {
                    resStreamOut.write(buffer, 0, readBytes);
                }
                resStreamOut.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static final Function<String, String> shouldAddSeparator = string ->
            string != null ? string + (string.endsWith(File.separator) ? "" : File.separator) : null;

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

}
