package fr.inria.stamp.coverage;

import fr.inria.diversify.automaticbuilder.AutomaticBuilderFactory;
import fr.inria.diversify.utils.sosiefier.InputConfiguration;
import fr.inria.diversify.utils.sosiefier.InputProgram;
import fr.inria.stamp.test.runner.TestRunnerFactory;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Stream;

import static fr.inria.diversify.utils.AmplificationHelper.PATH_SEPARATOR;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 23/10/17
 */
public class MemoryClassLoaderFactory {

    /*
        Here: the factory has the same behavior than @see fr.inria.stamp.test.runner.TestRunnerFactory
            We will use the parent of the system classloader in special case
     */

    public static MemoryClassLoader createMemoryClassLoader(CtType<?> testClass,
                                                            InputConfiguration configuration) {
        final InputProgram program = configuration.getInputProgram();
        final String classesDirectory = program.getProgramDir() + "/" + program.getClassesDir();
        final String testClassesDirectory = program.getProgramDir() + "/" + program.getTestClassesDir();
        String classpath = AutomaticBuilderFactory.getAutomaticBuilder(configuration)
                .buildClasspath(program.getProgramDir()) + PATH_SEPARATOR +
                "target/dspot/dependencies/";
        ClassLoader classLoader = null;
        if (testClass != null &&
                TestRunnerFactory.containsSpecificAnnotation.test(testClass)) {
            classLoader = new URLClassLoader(
                    Arrays.stream(classpath.split(":"))
                            .map(File::new)
                            .map(File::toURI)
                            .map(uri -> {
                                try {
                                    return uri.toURL();
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .toArray(URL[]::new),
                    ClassLoader.getSystemClassLoader().getParent()
            );
        } else {
            classLoader = new URLClassLoader(
                    Arrays.stream(classpath.split(":"))
                            .map(File::new)
                            .map(File::toURI)
                            .map(uri -> {
                                try {
                                    return uri.toURL();
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .toArray(URL[]::new),
                    ClassLoader.getSystemClassLoader()
            );
        }
        final URL[] urls;
        try {
            urls = new URL[]{
                    new File(classesDirectory).toURI().toURL(),
                    new File(testClassesDirectory).toURI().toURL()
            };
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        if (configuration.getProperty("additionalClasspathElements") != null) {
            return new MemoryClassLoader(
                    Stream.concat(
                            Arrays.stream(configuration.getProperty("additionalClasspathElements").split(PATH_SEPARATOR))
                                    .map(configuration.getInputProgram().getProgramDir()::concat)
                                    .map(File::new)
                                    .map(File::toURI)
                                    .map(uri -> {
                                        try {
                                            return uri.toURL();
                                        } catch (MalformedURLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }), Arrays.stream(urls))
                            .toArray(URL[]::new), classLoader
            );
        } else {
            return new MemoryClassLoader(urls, classLoader);
        }
    }

}
