package eu.stamp_project.dspot.common.miscellaneous;

import eu.stamp_project.dspot.common.collector.Collector;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.report.error.Error;
import eu.stamp_project.dspot.common.report.error.ErrorEnum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.LineFilter;
import spoon.support.JavaOutputProcessor;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper.PATH_SEPARATOR;

/**
 * User: Simon Date: 18/05/16 Time: 16:10
 */
public class DSpotUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSpotUtils.class);

    private static final String JAVA_EXTENSION = ".java";

    private static StringBuilder progress = new StringBuilder(60);

    private static CommentEnum withComment;

    private static String outputDirectory;

    private static String fullClassPathWithExtraDependencies;

    private static String absolutePathToProjectRoot;

    public static void init(CommentEnum withComment,
                            String outputDirectory,
                            String fullClassPathWithExtraDependencies,
                            String absolutePathToProjectRoot) {
        DSpotUtils.withComment = withComment;
        DSpotUtils.outputDirectory = outputDirectory;
        DSpotUtils.fullClassPathWithExtraDependencies = fullClassPathWithExtraDependencies;
        DSpotUtils.absolutePathToProjectRoot = absolutePathToProjectRoot;
        DSpotUtils.copyPackageFromResources();
    }

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
        Environment env = type.getFactory().getEnvironment();
        try {
            env.setAutoImports(autoImports);
            env.setNoClasspath(true);
            env.setCommentEnabled(DSpotUtils.withComment != CommentEnum.None);
            JavaOutputProcessor processor = new JavaOutputProcessor(env.createPrettyPrinterAutoImport());
            processor.setFactory(type.getFactory());
            processor.getEnvironment().setSourceOutputDirectory(directory);
            processor.createJavaFile(type);
            env.setAutoImports(false);
        } catch (Exception e) {
            printCtTypUsingToStringToGivenDirectory(type, directory);
        }
    }

    static void printCtTypUsingToStringToGivenDirectory(CtType<?> type, File directory) {
        LOGGER.warn("Something bad happened when trying to output {} in {}", type.getQualifiedName(), directory.getAbsolutePath());
        LOGGER.warn("DSpot will now print the toString() in the given file instead of using Spoon...");
        String directoryPathname = DSpotUtils.shouldAddSeparator.apply(directory.getAbsolutePath()) +
                type.getQualifiedName().substring(0, type.getQualifiedName().length() - type.getSimpleName().length())
                        .replaceAll("\\.", "/");
        try {
            FileUtils.forceMkdir(new File(directoryPathname));
        } catch (IOException e) {
            DSpotState.GLOBAL_REPORT.addError(new Error(ErrorEnum.ERROR_PRINT_USING_TO_STRING, e));
        }
        try (FileWriter fileWriter = new FileWriter(
                DSpotUtils.shouldAddSeparator.apply(directoryPathname) + type.getSimpleName() + JAVA_EXTENSION)
        ) {
            fileWriter.write(type.toString());
        } catch (Exception e) {
            DSpotState.GLOBAL_REPORT.addError(new Error(ErrorEnum.ERROR_PRINT_USING_TO_STRING, e));
        }
    }

    /*
        First, we print the amplified java test class with imports.
        We compile it.
        If the compilation fails, we re-print it without imports, i.e. using full qualified names.
     */
    public static void printAndCompileToCheck(CtType<?> type, File directory, Collector collector) {

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
            final boolean compile = DSpotCompiler.compile(//FIXME: analyse for optimisation (36% total execution time)
                    pathname,
                    DSpotUtils.fullClassPathWithExtraDependencies,
                    new File(DSpotUtils.outputDirectory + "/binaries/")
            );
            if (!compile) {
                try {
                    FileUtils.forceDelete(new File(DSpotUtils.outputDirectory + "/binaries/"));
                } catch (Exception ignored) {

                }
                LOGGER.warn("Could not compile {} with imports.", type.getQualifiedName());
                LOGGER.warn("DSpot outputs it using full qualified names.");
                LOGGER.warn("These problems can come from the fact your project use generated codes, such as Lombok annotations.");
                printCtTypeToGivenDirectory(type, directory, false); //FIXME: analyse for optimisation (13% total execution time)
            } else {
                // FIXME should not be here, this method should just print the amplified test class
                collector.reportAmpTestPath(pathname);
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
        CtClass<Object> objectCtClass = launcher.getFactory().Class().get(type.getQualifiedName());
        if (objectCtClass == null) {
            objectCtClass = launcher.getFactory().Class().get(type.getSimpleName());
            type.getPackage().addType(objectCtClass.clone());
        }
        return objectCtClass;

    }

    public static void addComment(CtElement element, String content, CtComment.CommentType type,
                                  CommentEnum commentConcern) {
        if (commentConcern.equals(withComment) || withComment.equals(CommentEnum.All)) {
            if (element instanceof CtLiteral) {
                try {
                    CtElement parentLine = element.getParent(new LineFilter());
                    if (parentLine != null) {
                        element = parentLine;
                    }
                } catch (ParentNotInitializedException ignored) {

                }
            }

            CtComment comment = element.getFactory().createComment(content, type);
            if (!element.getComments().contains(comment)) {
                element.addComment(comment);
            }
        }
    }
    public static void removeComments(CtElement element, String suffixOfComment) {
        if (element instanceof CtLiteral) {
            try {
                CtElement parentLine = element.getParent(new LineFilter());
                if (parentLine != null) {
                    element = parentLine;
                }
            } catch (ParentNotInitializedException ignored) {

            }
        }

        List<CtComment> ampComments = element.getComments().stream()
                                             .filter(ctComment -> ctComment.getContent().startsWith(suffixOfComment))
                                             .collect(Collectors.toList());
        for (CtComment ampComment : ampComments) {
            element.removeComment(ampComment);
        }

    }

    public static final String PATH_TO_DSPOT_DEPENDENCIES = "target/dspot/dependencies/";

    private static final String PACKAGE_NAME = "compare";

    private static final String PACKAGE_PATH = "eu/stamp_project/" +
            "dspot/assertiongenerator/assertiongenerator/methodreconstructor/observer/testwithloggenerator/objectlogsyntaxbuilder_constructs" + "/";

    private static final String COMPONENTS_FOLDER = "objectlog/";

    private static final String[] DSPOT_CLASSES = new String[]{COMPONENTS_FOLDER +"MethodsHandler", "ObjectLog",
            COMPONENTS_FOLDER +"Observation", COMPONENTS_FOLDER +"ObjectLogUtils", COMPONENTS_FOLDER +"FailToObserveException"};

    public static String getAbsolutePathToDSpotDependencies() {
        return DSpotUtils.shouldAddSeparator.apply(DSpotUtils.shouldAddSeparator.apply(DSpotUtils.absolutePathToProjectRoot) + PATH_TO_DSPOT_DEPENDENCIES);
    }

    public static void copyPackageFromResources() {

        final String pathToTestClassesDirectory = DSpotUtils.getAbsolutePathToDSpotDependencies() + PACKAGE_PATH;
        try {
            FileUtils.forceMkdir(new File(pathToTestClassesDirectory + COMPONENTS_FOLDER));
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

    public static String removeProjectRootIfAbsoluteAndAddSeparator(final String prefix, String path) {
        if (new File(path).isAbsolute()) {
            path = path.substring(prefix.length());
            return DSpotUtils.shouldAddSeparator.apply(
                    path.startsWith("/") ? path.substring(1) : path
            );
        } else {
            return DSpotUtils.shouldAddSeparator.apply(path);
        }
    }

    public static final Function<String, String> shouldAddSeparator = string ->
            string != null && !string.isEmpty() ? string + (string.endsWith(File.separator) ? "" : File.separator) : string;

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

    public static String createPath(String... paths) {
        return String.join(PATH_SEPARATOR, paths);
    }

}
