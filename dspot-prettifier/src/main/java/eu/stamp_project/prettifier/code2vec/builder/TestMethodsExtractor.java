package eu.stamp_project.prettifier.code2vec.builder;

import eu.stamp_project.test_framework.TestFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/12/18
 */
public class TestMethodsExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMethodsExtractor.class);

    /**
     * This method returns the entire list of test methods of the given project.
     * The project is given as the path to its root directory.
     * Then, if the project is multi module, this method will recursively explore each sub modules.
     * <p>
     * The rules are as follow:
     * - it takes path that endsWith .java
     * - it takes path that contains src/test/java
     * - it does not add twice the same test class:
     * - it splits the path with src/test/java (mandatory according to the previous predicate)
     * - we obtain a full qualified name, as a path, and so can filter the test classes.
     * This avoid SpoonException when we try to build the model of two test classes that have identical full qualified name.
     *
     * @param rootDirectory the path to the root directory of the project.
     * @return the list of all test classes with only test methods according to {@link eu.stamp_project.test_framework.TestFramework#isTest(CtMethod)}.
     * @throws IOException forward the exception thrown by {@link java.nio.file.Files#walk(Path, FileVisitOption...)}
     */
    public static List<CtType<?>> extractAllTestMethodsForGivenProject(String rootDirectory) throws IOException {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        final List<String> javaTestClasses;
        javaTestClasses = Files.walk(Paths.get(rootDirectory))
                .map(Path::toString)
                .filter(path -> path.endsWith(".java"))
                .filter(path -> path.contains("src/test/java/"))
                .collect(Collectors.toList());
        final Set<String> seen = ConcurrentHashMap.newKeySet();
        javaTestClasses.forEach(
                javaTestClass -> {
                    // we do that because sometimes, there is multiple definition of the same test classes...
                    if (seen.add(javaTestClass.split("src/test/java/")[1])) {
                        launcher.addInputResource(javaTestClass);
                    }
                }
        );
        LOGGER.info("Building Spoon model...");
        launcher.buildModel();
        final List<CtType<?>> testClasses = launcher.getFactory().Class().getAll()
                .stream()
                .filter(ctType -> ctType.getMethods().stream().anyMatch(TestFramework.get()::isTest))
                .collect(Collectors.toList());
        testClasses.forEach(ctType ->
                ctType.getMethods()
                        .stream()
                        .filter(ctMethod -> !TestFramework.get().isTest(ctMethod))
                        .forEach(ctType::removeMethod)
        );
        return testClasses;
    }

}
